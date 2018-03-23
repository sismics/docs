package com.sismics.docs.core.dao.lucene;

import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.LuceneUtil;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lucene DAO.
 * 
 * @author bgamard
 */
public class LuceneDao {
    /**
     * Destroy and rebuild index.
     * 
     * @param fileList List of files
     */
    public void rebuildIndex(final List<Document> documentList, final List<File> fileList) {
        LuceneUtil.handle(indexWriter -> {
            // Empty index
            indexWriter.deleteAll();

            // Add all documents
            for (Document document : documentList) {
                org.apache.lucene.document.Document luceneDocument = getDocumentFromDocument(document);
                indexWriter.addDocument(luceneDocument);
            }

            // Add all files
            for (File file : fileList) {
                org.apache.lucene.document.Document luceneDocument = getDocumentFromFile(file);
                indexWriter.addDocument(luceneDocument);
            }
        });
    }
    
    /**
     * Add document to the index.
     * 
     * @param document Document to add
     */
    public void createDocument(final Document document) {
        LuceneUtil.handle(indexWriter -> {
            org.apache.lucene.document.Document luceneDocument = getDocumentFromDocument(document);
            indexWriter.addDocument(luceneDocument);
        });
    }
    
    /**
     * Add file to the index.
     * 
     * @param file File to add
     */
    public void createFile(final File file) {
        LuceneUtil.handle(indexWriter -> {
            org.apache.lucene.document.Document luceneDocument = getDocumentFromFile(file);
            indexWriter.addDocument(luceneDocument);
        });
    }

    /**
     * Update document index.
     * 
     * @param document Updated document
     */
    public void updateDocument(final Document document) {
        LuceneUtil.handle(indexWriter -> {
            org.apache.lucene.document.Document luceneDocument = getDocumentFromDocument(document);
            indexWriter.updateDocument(new Term("id", document.getId()), luceneDocument);
        });
    }
    
    /**
     * Delete document from the index.
     * 
     * @param id Document ID to delete
     */
    public void deleteDocument(final String id) {
        LuceneUtil.handle(indexWriter -> indexWriter.deleteDocuments(new Term("id", id)));
    }
    
    /**
     * Search files.
     * 
     * @param searchQuery Search query on title and description
     * @param fullSearchQuery Search query on all fields
     * @return List of document IDs
     * @throws Exception e
     */
    public Set<String> search(String searchQuery, String fullSearchQuery) throws Exception {
        // Escape query and add quotes so QueryParser generate a PhraseQuery
        searchQuery = "\"" + QueryParserUtil.escape(searchQuery + " " + fullSearchQuery) + "\"";
        fullSearchQuery = "\"" + QueryParserUtil.escape(fullSearchQuery) + "\"";
        
        // Build search query
        StandardQueryParser qpHelper = new StandardQueryParser(new StandardAnalyzer());
        qpHelper.setPhraseSlop(100000); // PhraseQuery add terms
        
        // Search on documents and files
        BooleanQuery query = new BooleanQuery.Builder()
                .add(qpHelper.parse(searchQuery, "title"), Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "description"), Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "subject"), Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "identifier"), Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "publisher"), Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "format"), Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "source"), Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "type"), Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "coverage"), Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "rights"), Occur.SHOULD)
                .add(qpHelper.parse(fullSearchQuery, "content"), Occur.SHOULD)
                .build();
        
        // Search
        DirectoryReader directoryReader = AppContext.getInstance().getIndexingService().getDirectoryReader();
        Set<String> documentIdList = new HashSet<>();
        if (directoryReader == null) {
            // The directory reader is not yet initialized (probably because there is nothing indexed)
            return documentIdList;
        }
        IndexSearcher searcher = new IndexSearcher(directoryReader);
        TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
        ScoreDoc[] docs = topDocs.scoreDocs;
        
        // Extract document IDs
        for (ScoreDoc doc : docs) {
            org.apache.lucene.document.Document document = searcher.doc(doc.doc);
            String type = document.get("doctype");
            String documentId = null;
            if (type.equals("document")) {
                documentId = document.get("id");
            } else if (type.equals("file")) {
                documentId = document.get("document_id");
            }
            documentIdList.add(documentId);
        }
        
        return documentIdList;
    }
    
    /**
     * Build Lucene document from database document.
     * 
     * @param document Document
     * @return Document
     */
    private org.apache.lucene.document.Document getDocumentFromDocument(Document document) {
        org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
        luceneDocument.add(new StringField("id", document.getId(), Field.Store.YES));
        luceneDocument.add(new StringField("doctype", "document", Field.Store.YES));
        luceneDocument.add(new TextField("title", document.getTitle(), Field.Store.NO));
        if (document.getDescription() != null) {
            luceneDocument.add(new TextField("description", document.getDescription(), Field.Store.NO));
        }
        if (document.getSubject() != null) {
            luceneDocument.add(new TextField("subject", document.getSubject(), Field.Store.NO));
        }
        if (document.getIdentifier() != null) {
            luceneDocument.add(new TextField("identifier", document.getIdentifier(), Field.Store.NO));
        }
        if (document.getPublisher() != null) {
            luceneDocument.add(new TextField("publisher", document.getPublisher(), Field.Store.NO));
        }
        if (document.getFormat() != null) {
            luceneDocument.add(new TextField("format", document.getFormat(), Field.Store.NO));
        }
        if (document.getSource() != null) {
            luceneDocument.add(new TextField("source", document.getSource(), Field.Store.NO));
        }
        if (document.getType() != null) {
            luceneDocument.add(new TextField("type", document.getType(), Field.Store.NO));
        }
        if (document.getCoverage() != null) {
            luceneDocument.add(new TextField("coverage", document.getCoverage(), Field.Store.NO));
        }
        if (document.getRights() != null) {
            luceneDocument.add(new TextField("rights", document.getRights(), Field.Store.NO));
        }
        
        return luceneDocument;
    }
    
    /**
     * Build Lucene document from file.
     * 
     * @param file File
     * @return Document
     */
    private org.apache.lucene.document.Document getDocumentFromFile(File file) {
        org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
        luceneDocument.add(new StringField("id", file.getId(), Field.Store.YES));
        luceneDocument.add(new StringField("doctype", "file", Field.Store.YES));
        luceneDocument.add(new StringField("document_id", file.getDocumentId(), Field.Store.YES));
        if (file.getContent() != null) {
            luceneDocument.add(new TextField("content", file.getContent(), Field.Store.NO));
        }
        
        return luceneDocument;
    }
}
