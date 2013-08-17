package com.sismics.docs.core.dao.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.LuceneUtil;
import com.sismics.docs.core.util.LuceneUtil.LuceneRunnable;

/**
 * Lucene DAO.
 * 
 * @author bgamard
 */
public class LuceneDao {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LuceneDao.class);

    /**
     * Destroy and rebuild index.
     * 
     * @param fileList
     */
    public void rebuildIndex(final List<Document> documentList, final List<File> fileList) {
        LuceneUtil.handle(new LuceneRunnable() {
            @Override
            public void run(IndexWriter indexWriter) throws Exception {
                // Empty index
                indexWriter.deleteAll();
                
                // Add all documents
                Map<String, Document> documentMap = new HashMap<>();
                for (Document document : documentList) {
                    org.apache.lucene.document.Document luceneDocument = getDocumentFromDocument(document);
                    indexWriter.addDocument(luceneDocument);
                    documentMap.put(document.getId(), document);
                }
                
                // Add all files
                for (File file : fileList) {
                    org.apache.lucene.document.Document luceneDocument = getDocumentFromFile(file, documentMap.get(file.getDocumentId()));
                    indexWriter.addDocument(luceneDocument);
                }
            }
        });
    }
    
    /**
     * Add document to the index.
     * 
     * @param document Document to add
     */
    public void createDocument(final Document document) {
        LuceneUtil.handle(new LuceneRunnable() {
            @Override
            public void run(IndexWriter indexWriter) throws Exception {
                org.apache.lucene.document.Document luceneDocument = getDocumentFromDocument(document);
                indexWriter.addDocument(luceneDocument);
            }
        });
    }
    
    /**
     * Add file to the index.
     * 
     * @param file File to add
     * @param document Document linked to the file
     */
    public void createFile(final File file, final Document document) {
        LuceneUtil.handle(new LuceneRunnable() {
            @Override
            public void run(IndexWriter indexWriter) throws Exception {
                org.apache.lucene.document.Document luceneDocument = getDocumentFromFile(file, document);
                indexWriter.addDocument(luceneDocument);
            }
        });
    }
    
    /**
     * Update document index.
     * 
     * @param document Updated document
     */
    public void updateDocument(final Document document) {
        LuceneUtil.handle(new LuceneRunnable() {
            @Override
            public void run(IndexWriter indexWriter) throws Exception {
                org.apache.lucene.document.Document luceneDocument = getDocumentFromDocument(document);
                indexWriter.updateDocument(new Term("id", document.getId()), luceneDocument);
            }
        });
    }
    
    /**
     * Update file index.
     * 
     * @param file Updated file
     * @param document Document linked to the file
     */
    public void updateFile(final File file, final Document document) {
        LuceneUtil.handle(new LuceneRunnable() {
            @Override
            public void run(IndexWriter indexWriter) throws Exception {
                org.apache.lucene.document.Document luceneDocument = getDocumentFromFile(file, document);
                indexWriter.updateDocument(new Term("id", file.getId()), luceneDocument);
            }
        });
    }
    
    /**
     * Delete document from the index.
     * 
     * @param id Document ID to delete
     */
    public void deleteDocument(final String id) {
        LuceneUtil.handle(new LuceneRunnable() {
            @Override
            public void run(IndexWriter indexWriter) throws Exception {
                indexWriter.deleteDocuments(new Term("id", id));
            }
        });
    }
    
    /**
     * Search files.
     * 
     * @param userId User ID to filter on
     * @param searchQuery Search query
     * @return List of document IDs
     * @throws Exception
     */
    public Set<String> search(String userId, String searchQuery) throws Exception {
        // Escape query and add quotes so QueryParser generate a PhraseQuery
        searchQuery = "\"" + QueryParserUtil.escape(searchQuery) + "\"";
        
        // Build search query
        StandardQueryParser qpHelper = new StandardQueryParser(new DocsStandardAnalyzer(Version.LUCENE_42));
        qpHelper.setPhraseSlop(100000); // PhraseQuery add terms
        
        // Search on documents and files
        BooleanQuery query = new BooleanQuery();
        query.add(qpHelper.parse(searchQuery, "content"), Occur.SHOULD);
        query.add(qpHelper.parse(searchQuery, "title"), Occur.SHOULD);
        query.add(qpHelper.parse(searchQuery, "description"), Occur.SHOULD);
        
        // Filter on provided user ID
        List<Term> terms = new ArrayList<Term>();
        if (userId != null) {
            terms.add(new Term("user_id", userId));
        }
        TermsFilter userFilter = new TermsFilter(terms);
        
        // Search
        Set<String> documentIdList = new HashSet<String>();
        if (!DirectoryReader.indexExists(AppContext.getInstance().getLuceneDirectory())) {
            log.warn("Lucene directory not yet initialized");
            return documentIdList;
        }
        IndexReader reader = DirectoryReader.open(AppContext.getInstance().getLuceneDirectory());
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs topDocs = searcher.search(query, userFilter, Integer.MAX_VALUE);
        ScoreDoc[] docs = topDocs.scoreDocs;
        
        // Extract document IDs
        for (int i = 0; i < docs.length; i++) {
            org.apache.lucene.document.Document document = searcher.doc(docs[i].doc);
            String type = document.get("type");
            String documentId = null;
            if (type.equals("document")) {
                documentId = document.get("id");
            } else if (type.equals("file")) {
                documentId = document.get("document_id");
            }
            documentIdList.add(documentId);
        }
        
        reader.close();
        
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
        luceneDocument.add(new StringField("user_id", document.getUserId(), Field.Store.YES));
        luceneDocument.add(new StringField("type", "document", Field.Store.YES));
        if (document.getTitle() != null) {
            luceneDocument.add(new TextField("title", document.getTitle(), Field.Store.NO));
        }
        if (document.getDescription() != null) {
            luceneDocument.add(new TextField("description", document.getDescription(), Field.Store.NO));
        }
        
        return luceneDocument;
    }
    
    /**
     * Build Lucene document from file.
     * 
     * @param file File
     * @param document Document linked to the file
     * @return Document
     */
    private org.apache.lucene.document.Document getDocumentFromFile(File file, Document document) {
        org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
        luceneDocument.add(new StringField("id", file.getId(), Field.Store.YES));
        luceneDocument.add(new StringField("user_id", document.getUserId(), Field.Store.YES));
        luceneDocument.add(new StringField("type", "file", Field.Store.YES));
        luceneDocument.add(new StringField("document_id", file.getDocumentId(), Field.Store.YES));
        if (file.getContent() != null) {
            luceneDocument.add(new TextField("content", file.getContent(), Field.Store.NO));
        }
        
        return luceneDocument;
    }
}
