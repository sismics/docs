package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.MetadataType;
import com.sismics.docs.core.dao.dto.DocumentMetadataDto;
import com.sismics.docs.core.model.jpa.DocumentMetadata;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Document metadata DAO.
 * 
 * @author bgamard
 */
public class DocumentMetadataDao {
    /**
     * Creates a new document metadata.
     *
     * @param documentMetadata Document metadata
     * @return New ID
     */
    public String create(DocumentMetadata documentMetadata) {
        // Create the UUID
        documentMetadata.setId(UUID.randomUUID().toString());

        // Create the document metadata
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(documentMetadata);

        return documentMetadata.getId();
    }

    /**
     * Updates a document metadata.
     *
     * @param documentMetadata Document metadata
     * @return Updated document metadata
     */
    public DocumentMetadata update(DocumentMetadata documentMetadata) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the document metadata
        Query q = em.createQuery("select u from DocumentMetadata u where u.id = :id");
        q.setParameter("id", documentMetadata.getId());
        DocumentMetadata documentMetadataDb = (DocumentMetadata) q.getSingleResult();

        // Update the document metadata
        documentMetadataDb.setValue(documentMetadata.getValue());

        return documentMetadata;
    }

    /**
     * Returns the list of all metadata values on a document.
     *
     * @param documentId Document ID
     * @return List of metadata
     */
    @SuppressWarnings("unchecked")
    public List<DocumentMetadataDto> getByDocumentId(String documentId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select dm.DME_ID_C, dm.DME_IDDOCUMENT_C, dm.DME_IDMETADATA_C, dm.DME_VALUE_C, m.MET_TYPE_C");
        sb.append(" from T_DOCUMENT_METADATA dm, T_METADATA m ");
        sb.append(" where dm.DME_IDMETADATA_C = m.MET_ID_C and dm.DME_IDDOCUMENT_C = :documentId and m.MET_DELETEDATE_D is null");

        // Perform the search
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("documentId", documentId);
        List<Object[]> l = q.getResultList();

        // Assemble results
        List<DocumentMetadataDto> dtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            DocumentMetadataDto dto = new DocumentMetadataDto();
            dto.setId((String) o[i++]);
            dto.setDocumentId((String) o[i++]);
            dto.setMetadataId((String) o[i++]);
            dto.setValue((String) o[i++]);
            dto.setType(MetadataType.valueOf((String) o[i]));
            dtoList.add(dto);
        }
        return dtoList;
    }
}
