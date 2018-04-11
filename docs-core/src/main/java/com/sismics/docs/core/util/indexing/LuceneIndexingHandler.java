package com.sismics.docs.core.util.indexing;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.ConfigDao;
import com.sismics.docs.core.dao.criteria.DocumentCriteria;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.model.jpa.Config;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.ClasspathScanner;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.*;

/**
 * Lucene indexing handler.
 * Documents and files are indexed in Lucene, but only used for fulltext search.
 * Other search criteria are still using the database.
 *
 * @author bgamard
 */
@ClasspathScanner.Priority(100)
public class LuceneIndexingHandler implements IndexingHandler {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LuceneIndexingHandler.class);

    /**
     * Lucene directory.
     */
    private Directory directory;

    /**
     * Directory reader.
     */
    private DirectoryReader directoryReader;

    /**
     * Index writer.
     */
    private IndexWriter indexWriter;

    @Override
    public boolean accept() {
        // Embedded Lucene can always start
        return true;
    }

    @Override
    public void startUp() throws Exception {
        ConfigDao configDao = new ConfigDao();
        Config luceneStorageConfig = configDao.getById(ConfigType.LUCENE_DIRECTORY_STORAGE);
        String luceneStorage = luceneStorageConfig == null ? null : luceneStorageConfig.getValue();

        // RAM directory storage by default
        if (luceneStorage == null || luceneStorage.equals("RAM")) {
            directory = new RAMDirectory();
            log.info("Using RAM Lucene storage");
        } else if (luceneStorage.equals("FILE")) {
            Path luceneDirectory = DirectoryUtil.getLuceneDirectory();
            log.info("Using file Lucene storage: {}", luceneDirectory);
            directory = new SimpleFSDirectory(luceneDirectory, NoLockFactory.INSTANCE);
        }

        // Create an index writer
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        config.setCommitOnClose(true);
        config.setMergeScheduler(new SerialMergeScheduler());
        indexWriter = new IndexWriter(directory, config);

        // Check index version and rebuild it if necessary
        if (DirectoryReader.indexExists(directory)) {
            log.info("Checking index health and version");
            try (CheckIndex checkIndex = new CheckIndex(directory)) {
                CheckIndex.Status status = checkIndex.checkIndex();
                if (status.clean) {
                    for (CheckIndex.Status.SegmentInfoStatus segmentInfo : status.segmentInfos) {
                        if (!segmentInfo.version.onOrAfter(Version.LATEST)) {
                            throw new Exception("Index is old (" + segmentInfo.version + ")");
                        }
                    }
                } else {
                    throw new Exception("Index is dirty");
                }
            }
        }
    }

    @Override
    public void shutDown() {
        if (directoryReader != null) {
            try {
                directoryReader.close();
            } catch (IOException e) {
                log.error("Error closing the index reader", e);
            }
        }
        if (indexWriter != null) {
            try {
                indexWriter.close();
            } catch (IOException e) {
                log.error("Error closing the index writer, index may be corrupt", e);
            }
        }
        if (directory != null) {
            try {
                directory.close();
            } catch (IOException e) {
                log.error("Error closing Lucene index", e);
            }
        }
    }

    @Override
    public void rebuildIndex(final List<Document> documentList, final List<File> fileList) {
        handle(indexWriter -> {
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

    @Override
    public void createDocument(final Document document) {
        handle(indexWriter -> {
            org.apache.lucene.document.Document luceneDocument = getDocumentFromDocument(document);
            indexWriter.addDocument(luceneDocument);
        });
    }

    @Override
    public void createFile(final File file) {
        handle(indexWriter -> {
            org.apache.lucene.document.Document luceneDocument = getDocumentFromFile(file);
            indexWriter.addDocument(luceneDocument);
        });
    }

    @Override
    public void updateFile(final File file) {
        handle(indexWriter -> {
            org.apache.lucene.document.Document luceneDocument = getDocumentFromFile(file);
            indexWriter.updateDocument(new Term("id", file.getId()), luceneDocument);
        });
    }

    @Override
    public void updateDocument(final Document document) {
        handle(indexWriter -> {
            org.apache.lucene.document.Document luceneDocument = getDocumentFromDocument(document);
            indexWriter.updateDocument(new Term("id", document.getId()), luceneDocument);
        });
    }

    @Override
    public void deleteDocument(final String id) {
        handle(indexWriter -> indexWriter.deleteDocuments(new Term("id", id)));
    }

    @Override
    public void createAcl(String sourceId, PermType perm, String targetId) {
        // Lucene does not index ACLs
    }

    @Override
    public void deleteAcl(String sourceId, PermType perm, String targetId) {
        // Lucene does not index ACLs
    }

    @Override
    public void findByCriteria(PaginatedList<DocumentDto> paginatedList, DocumentCriteria criteria, SortCriteria sortCriteria) throws Exception {
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();

        StringBuilder sb = new StringBuilder("select distinct d.DOC_ID_C c0, d.DOC_TITLE_C c1, d.DOC_DESCRIPTION_C c2, d.DOC_CREATEDATE_D c3, d.DOC_LANGUAGE_C c4, ");
        sb.append(" s.count c5, ");
        sb.append(" f.count c6, ");
        sb.append(" rs2.RTP_ID_C c7, rs2.RTP_NAME_C, d.DOC_UPDATEDATE_D c8 ");
        sb.append(" from T_DOCUMENT d ");
        sb.append(" left join (SELECT count(s.SHA_ID_C) count, ac.ACL_SOURCEID_C " +
                "   FROM T_SHARE s, T_ACL ac " +
                "   WHERE ac.ACL_TARGETID_C = s.SHA_ID_C AND ac.ACL_DELETEDATE_D IS NULL AND " +
                "         s.SHA_DELETEDATE_D IS NULL group by ac.ACL_SOURCEID_C) s on s.ACL_SOURCEID_C = d.DOC_ID_C " +
                "  left join (SELECT count(f.FIL_ID_C) count, f.FIL_IDDOC_C " +
                "   FROM T_FILE f " +
                "   WHERE f.FIL_DELETEDATE_D IS NULL group by f.FIL_IDDOC_C) f on f.FIL_IDDOC_C = d.DOC_ID_C ");
        sb.append(" left join (select rs.*, rs3.idDocument " +
                "from T_ROUTE_STEP rs " +
                "join (select r.RTE_IDDOCUMENT_C idDocument, rs.RTP_IDROUTE_C idRoute, min(rs.RTP_ORDER_N) minOrder from T_ROUTE_STEP rs join T_ROUTE r on r.RTE_ID_C = rs.RTP_IDROUTE_C and r.RTE_DELETEDATE_D is null where rs.RTP_DELETEDATE_D is null and rs.RTP_ENDDATE_D is null group by rs.RTP_IDROUTE_C, r.RTE_IDDOCUMENT_C) rs3 on rs.RTP_IDROUTE_C = rs3.idRoute and rs.RTP_ORDER_N = rs3.minOrder " +
                "where rs.RTP_IDTARGET_C in (:targetIdList)) rs2 on rs2.idDocument = d.DOC_ID_C ");

        // Add search criterias
        if (criteria.getTargetIdList() != null) {
            // Read permission is enough for searching
            sb.append(" left join T_ACL a on a.ACL_TARGETID_C in (:targetIdList) and a.ACL_SOURCEID_C = d.DOC_ID_C and a.ACL_PERM_C = 'READ' and a.ACL_DELETEDATE_D is null ");
            sb.append(" left join T_DOCUMENT_TAG dta on dta.DOT_IDDOCUMENT_C = d.DOC_ID_C and dta.DOT_DELETEDATE_D is null ");
            sb.append(" left join T_ACL a2 on a2.ACL_TARGETID_C in (:targetIdList) and a2.ACL_SOURCEID_C = dta.DOT_IDTAG_C and a2.ACL_PERM_C = 'READ' and a2.ACL_DELETEDATE_D is null ");
            criteriaList.add("(a.ACL_ID_C is not null or a2.ACL_ID_C is not null)");
            parameterMap.put("targetIdList", criteria.getTargetIdList());
        }
        if (!Strings.isNullOrEmpty(criteria.getSearch()) || !Strings.isNullOrEmpty(criteria.getFullSearch())) {
            Set<String> documentIdList = search(criteria.getSearch(), criteria.getFullSearch());
            if (documentIdList.isEmpty()) {
                // If the search doesn't find any document, the request should return nothing
                documentIdList.add(UUID.randomUUID().toString());
            }
            criteriaList.add("d.DOC_ID_C in :documentIdList");
            parameterMap.put("documentIdList", documentIdList);
        }
        if (criteria.getCreateDateMin() != null) {
            criteriaList.add("d.DOC_CREATEDATE_D >= :createDateMin");
            parameterMap.put("createDateMin", criteria.getCreateDateMin());
        }
        if (criteria.getCreateDateMax() != null) {
            criteriaList.add("d.DOC_CREATEDATE_D <= :createDateMax");
            parameterMap.put("createDateMax", criteria.getCreateDateMax());
        }
        if (criteria.getUpdateDateMin() != null) {
            criteriaList.add("d.DOC_UPDATEDATE_D >= :updateDateMin");
            parameterMap.put("updateDateMin", criteria.getUpdateDateMin());
        }
        if (criteria.getUpdateDateMax() != null) {
            criteriaList.add("d.DOC_UPDATEDATE_D <= :updateDateMax");
            parameterMap.put("updateDateMax", criteria.getUpdateDateMax());
        }
        if (criteria.getTagIdList() != null && !criteria.getTagIdList().isEmpty()) {
            int index = 0;
            for (List<String> tagIdList : criteria.getTagIdList()) {
                List<String> tagCriteriaList = Lists.newArrayList();
                for (String tagId : tagIdList) {
                    sb.append(String.format("left join T_DOCUMENT_TAG dt%d on dt%d.DOT_IDDOCUMENT_C = d.DOC_ID_C and dt%d.DOT_IDTAG_C = :tagId%d and dt%d.DOT_DELETEDATE_D is null ", index, index, index, index, index));
                    parameterMap.put("tagId" + index, tagId);
                    tagCriteriaList.add(String.format("dt%d.DOT_ID_C is not null", index));
                    index++;
                }
                criteriaList.add("(" + Joiner.on(" OR ").join(tagCriteriaList) + ")");
            }
        }
        if (criteria.getShared() != null && criteria.getShared()) {
            criteriaList.add("s.count > 0");
        }
        if (criteria.getLanguage() != null) {
            criteriaList.add("d.DOC_LANGUAGE_C = :language");
            parameterMap.put("language", criteria.getLanguage());
        }
        if (criteria.getCreatorId() != null) {
            criteriaList.add("d.DOC_IDUSER_C = :creatorId");
            parameterMap.put("creatorId", criteria.getCreatorId());
        }
        if (criteria.getActiveRoute() != null && criteria.getActiveRoute()) {
            criteriaList.add("rs2.RTP_ID_C is not null");
        }

        criteriaList.add("d.DOC_DELETEDATE_D is null");

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }

        // Perform the search
        QueryParam queryParam = new QueryParam(sb.toString(), parameterMap);
        List<Object[]> l = PaginatedLists.executePaginatedQuery(paginatedList, queryParam, sortCriteria);

        // Assemble results
        List<DocumentDto> documentDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            DocumentDto documentDto = new DocumentDto();
            documentDto.setId((String) o[i++]);
            documentDto.setTitle((String) o[i++]);
            documentDto.setDescription((String) o[i++]);
            documentDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            documentDto.setLanguage((String) o[i++]);
            Number shareCount = (Number) o[i++];
            documentDto.setShared(shareCount != null && shareCount.intValue() > 0);
            Number fileCount = (Number) o[i++];
            documentDto.setFileCount(fileCount == null ? 0 : fileCount.intValue());
            documentDto.setActiveRoute(o[i++] != null);
            documentDto.setCurrentStepName((String) o[i++]);
            documentDto.setUpdateTimestamp(((Timestamp) o[i]).getTime());
            documentDtoList.add(documentDto);
        }

        paginatedList.setResultList(documentDtoList);
    }

    /**
     * Fulltext search in files and documents.
     *
     * @param searchQuery Search query on metadatas
     * @param fullSearchQuery Search query on all fields
     * @return List of document IDs
     * @throws Exception e
     */
    private Set<String> search(String searchQuery, String fullSearchQuery) throws Exception {
        // Escape query and add quotes so QueryParser generate a PhraseQuery
        searchQuery = "\"" + QueryParserUtil.escape(searchQuery + " " + fullSearchQuery) + "\"";
        fullSearchQuery = "\"" + QueryParserUtil.escape(fullSearchQuery) + "\"";

        // Build search query
        StandardQueryParser qpHelper = new StandardQueryParser(new StandardAnalyzer());
        qpHelper.setPhraseSlop(100); // PhraseQuery add terms

        // Search on documents and files
        BooleanQuery query = new BooleanQuery.Builder()
                .add(qpHelper.parse(searchQuery, "title"), BooleanClause.Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "description"), BooleanClause.Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "subject"), BooleanClause.Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "identifier"), BooleanClause.Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "publisher"), BooleanClause.Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "format"), BooleanClause.Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "source"), BooleanClause.Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "type"), BooleanClause.Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "coverage"), BooleanClause.Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "rights"), BooleanClause.Occur.SHOULD)
                .add(qpHelper.parse(searchQuery, "filename"), BooleanClause.Occur.SHOULD)
                .add(qpHelper.parse(fullSearchQuery, "content"), BooleanClause.Occur.SHOULD)
                .build();

        // Search
        DirectoryReader directoryReader = getDirectoryReader();
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
            if (documentId != null) {
                documentIdList.add(documentId);
            }
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
        if (file.getName() != null) {
            luceneDocument.add(new TextField("filename", file.getName(), Field.Store.NO));
        }
        if (file.getDocumentId() != null) {
            luceneDocument.add(new StringField("document_id", file.getDocumentId(), Field.Store.YES));
        }
        if (file.getContent() != null) {
            luceneDocument.add(new TextField("content", file.getContent(), Field.Store.NO));
        }

        return luceneDocument;
    }

    /**
     * Returns a valid directory reader.
     * Take care of reopening the reader if the index has changed
     * and closing the previous one.
     *
     * @return the directoryReader
     */
    private DirectoryReader getDirectoryReader() {
        if (directoryReader == null) {
            try {
                if (!DirectoryReader.indexExists(directory)) {
                    return null;
                }
                directoryReader = DirectoryReader.open(directory);
            } catch (IOException e) {
                log.error("Error creating the directory reader", e);
            }
        } else {
            try {
                DirectoryReader newReader = DirectoryReader.openIfChanged(directoryReader);
                if (newReader != null) {
                    directoryReader.close();
                    directoryReader = newReader;
                }
            } catch (IOException e) {
                log.error("Error while reopening the directory reader", e);
            }
        }
        return directoryReader;
    }

    /**
     * Encapsulate a process into a Lucene context.
     *
     * @param runnable Runnable
     */
    private void handle(LuceneRunnable runnable) {
        try {
            runnable.run(indexWriter);
        } catch (Exception e) {
            log.error("Error in running index writing", e);
        }

        try {
            indexWriter.commit();
        } catch (IOException e) {
            log.error("Cannot commit index writer", e);
        }
    }

    /**
     * Lucene runnable.
     *
     * @author bgamard
     */
    private interface LuceneRunnable {
        /**
         * Code to run in a Lucene context.
         *
         * @param indexWriter Index writer
         * @throws Exception e
         */
        void run(IndexWriter indexWriter) throws Exception;
    }
}
