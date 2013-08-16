package com.sismics.docs.core.dao.lucene;

import java.util.ArrayList;
import java.util.List;

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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;

import com.sismics.docs.core.model.context.AppContext;
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
     * Destroy and rebuild index.
     * 
     * @param fileList
     */
    public void rebuildIndex(final List<File> fileList) {
        LuceneUtil.handle(new LuceneRunnable() {
            @Override
            public void run(IndexWriter indexWriter) throws Exception {
                // Empty index
                indexWriter.deleteAll();
                
                // Add all files
                for (File file : fileList) {
                    org.apache.lucene.document.Document document = getDocumentFromFile(file);
                    indexWriter.addDocument(document);
                }
            }
        });
    }

    
    /**
     * Add files to the index.
     * 
     * @param fileList
     */
    public void create(final List<File> fileList) {
        LuceneUtil.handle(new LuceneRunnable() {
            @Override
            public void run(IndexWriter indexWriter) throws Exception {
                // Add all files
                for (File file : fileList) {
                    org.apache.lucene.document.Document document = getDocumentFromFile(file);
                    indexWriter.addDocument(document);
                }
            }
        });
    }
    
    /**
     * Update index.
     * 
     * @param fileList File list
     */
    public void update(final List<File> fileList) {
        LuceneUtil.handle(new LuceneRunnable() {
            @Override
            public void run(IndexWriter indexWriter) throws Exception {
                // Update all files
                for (File file : fileList) {
                    org.apache.lucene.document.Document document = getDocumentFromFile(file);
                    indexWriter.updateDocument(new Term("id", file.getId()), document);
                }
            }
        });
    }

    /**
     * Search files.
     * 
     * @param paginatedList
     * @param feedList
     * @param searchQuery
     * @return List of file IDs
     * @throws Exception
     */
    public List<String> search(String userId, String searchQuery, int limit) throws Exception {
        // Escape query and add quotes so QueryParser generate a PhraseQuery
        searchQuery = "\"" + QueryParserUtil.escape(searchQuery) + "\"";
        
        // Build search query
        StandardQueryParser qpHelper = new StandardQueryParser(new DocsStandardAnalyzer(Version.LUCENE_42));
        qpHelper.setPhraseSlop(100000); // PhraseQuery add terms
        Query contentQuery = qpHelper.parse(searchQuery, "content");
        
        // Search on file content
        BooleanQuery query = new BooleanQuery();
        query.add(contentQuery, Occur.SHOULD);
        
        // Filter on provided user ID
        List<Term> terms = new ArrayList<Term>();
        terms.add(new Term("user_id", userId));
        TermsFilter feedsFilter = new TermsFilter(terms);
        
        // Search
        IndexReader reader = DirectoryReader.open(AppContext.getInstance().getLuceneDirectory());
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs topDocs = searcher.search(query, feedsFilter, limit);
        ScoreDoc[] docs = topDocs.scoreDocs;
        
        // Extract file IDs
        List<String> fileIdList = new ArrayList<String>();
        for (int i = 0; i < docs.length; i++) {
            String id = searcher.doc(docs[i].doc).get("id");
            fileIdList.add(id);
        }
        
        return fileIdList;
    }
    
    /**
     * Build Lucene document from file.
     * 
     * @param file File
     * @return Document
     */
    private org.apache.lucene.document.Document getDocumentFromFile(File file) {
        // Building document
        org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
        document.add(new StringField("id", file.getId(), Field.Store.YES));
        document.add(new TextField("content", file.getContent(), Field.Store.NO));
        
        return document;
    }
}
