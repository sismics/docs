package com.sismics.docs.core.dao.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.jpa.criteria.AuditLogCriteria;
import com.sismics.docs.core.dao.jpa.dto.AuditLogDto;
import com.sismics.docs.core.model.jpa.AuditLog;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

/**
 * Audit log DAO.
 * 
 * @author bgamard
 */
public class AuditLogDao {
    /**
     * Creates a new audit log.
     * 
     * @param auditLog Audit log
     * @return New ID
     * @throws Exception
     */
    public String create(AuditLog auditLog) {
        // Create the UUID
        auditLog.setId(UUID.randomUUID().toString());
        
        // Create the audit log
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        auditLog.setCreateDate(new Date());
        em.persist(auditLog);
        
        return auditLog.getId();
    }
    
    /**
     * Searches audit logs by criteria.
     * 
     * @param paginatedList List of audit logs (updated by side effects)
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @return List of audit logs
     * @throws Exception 
     */
    public void findByCriteria(PaginatedList<AuditLogDto> paginatedList, AuditLogCriteria criteria, SortCriteria sortCriteria) throws Exception {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        
        String baseQuery = "select l.LOG_ID_C c0, l.LOG_CREATEDATE_D c1, l.LOG_IDENTITY_C c2, l.LOG_CLASSENTITY_C c3, l.LOG_TYPE_C c4, l.LOG_MESSAGE_C c5 from T_AUDIT_LOG l ";
        List<String> queries = Lists.newArrayList();
        
        // Adds search criteria
        if (criteria.getDocumentId() != null) {
            // ACL on document is not checked here, it's assumed
            queries.add(baseQuery + " where l.LOG_IDENTITY_C = :documentId ");
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select f.FIL_ID_C from T_FILE f where f.FIL_IDDOC_C = :documentId) ");
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select c.COM_ID_C from T_COMMENT c where c.COM_IDDOC_C = :documentId) ");
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select a.ACL_ID_C from T_ACL a where a.ACL_SOURCEID_C = :documentId) ");
            parameterMap.put("documentId", criteria.getDocumentId());
        }
        
        if (criteria.getUserId() != null) {
            queries.add(baseQuery + " where l.LOG_IDENTITY_C = :userId ");
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select t.TAG_ID_C from T_TAG t where t.TAG_IDUSER_C = :userId) ");
            // Show only logs from owned documents, ACL are lost on delete
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select d.DOC_ID_C from T_DOCUMENT d where d.DOC_IDUSER_C = :userId) ");
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select c.COM_ID_C from T_COMMENT c where c.COM_IDUSER_C = :userId) ");
            parameterMap.put("userId", criteria.getUserId());
        }
        
        // Perform the search
        QueryParam queryParam = new QueryParam(Joiner.on(" union ").join(queries), parameterMap);
        List<Object[]> l = PaginatedLists.executePaginatedQuery(paginatedList, queryParam, sortCriteria);
        
        // Assemble results
        List<AuditLogDto> auditLogDtoList = new ArrayList<AuditLogDto>();
        for (Object[] o : l) {
            int i = 0;
            AuditLogDto auditLogDto = new AuditLogDto();
            auditLogDto.setId((String) o[i++]);
            auditLogDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            auditLogDto.setEntityId((String) o[i++]);
            auditLogDto.setEntityClass((String) o[i++]);
            auditLogDto.setType(AuditLogType.valueOf((String) o[i++]));
            auditLogDto.setMessage((String) o[i++]);
            auditLogDtoList.add(auditLogDto);
        }

        paginatedList.setResultList(auditLogDtoList);
    }
}
