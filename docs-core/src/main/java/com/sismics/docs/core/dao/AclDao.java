package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.AclTargetType;
import com.sismics.docs.core.constant.AclType;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.dto.AclDto;
import com.sismics.docs.core.model.jpa.Acl;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.docs.core.util.SecurityUtil;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * ACL DAO.
 *
 * @author bgamard
 */
public class AclDao {
    /**
     * Creates a new ACL.
     *
     * @param acl ACL
     * @param userId User ID
     * @return New ID
     */
    public String create(Acl acl, String userId) {
        // Create the UUID
        acl.setId(UUID.randomUUID().toString());

        // Create the ACL
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(acl);

        // Create audit log
        AuditLogUtil.create(acl, AuditLogType.CREATE, userId);

        return acl.getId();
    }

    /**
     * Search ACLs by target ID.
     *
     * @param targetId Target ID
     * @return ACL list
     */
    @SuppressWarnings("unchecked")
    public List<Acl> getByTargetId(String targetId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select a from Acl a where a.targetId = :targetId and a.deleteDate is null");
        q.setParameter("targetId", targetId);

        return q.getResultList();
    }

    /**
     * Search ACLs by source ID.
     *
     * @param sourceId Source ID
     * @return ACL DTO list
     */
    @SuppressWarnings("unchecked")
    public List<AclDto> getBySourceId(String sourceId, AclType type) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select a.ACL_ID_C, a.ACL_PERM_C, a.ACL_TARGETID_C, ")
                .append(" u.USE_USERNAME_C, s.SHA_ID_C, s.SHA_NAME_C, g.GRP_NAME_C ")
                .append(" from T_ACL a ")
                .append(" left join T_USER u on u.USE_ID_C = a.ACL_TARGETID_C ")
                .append(" left join T_SHARE s on s.SHA_ID_C = a.ACL_TARGETID_C ")
                .append(" left join T_GROUP g on g.GRP_ID_C = a.ACL_TARGETID_C ")
                .append(" where a.ACL_DELETEDATE_D is null and a.ACL_SOURCEID_C = :sourceId ");
        if (type != null) {
            sb.append(" and a.ACL_TYPE_C = :type");
        }

        // Perform the query
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("sourceId", sourceId);
        if (type != null) {
            q.setParameter("type", type.name());
        }
        List<Object[]> l = q.getResultList();

        // Assemble results
        List<AclDto> aclDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            AclDto aclDto = new AclDto();
            aclDto.setId((String) o[i++]);
            aclDto.setPerm(PermType.valueOf((String) o[i++]));
            aclDto.setTargetId((String) o[i++]);
            String userName = (String) o[i++];
            String shareId = (String) o[i++];
            String shareName = (String) o[i++];
            String groupName = (String) o[i];
            if (userName != null) {
                aclDto.setTargetName(userName);
                aclDto.setTargetType(AclTargetType.USER.name());
            }
            if (shareId != null) { // Use ID because share name is nullable
                aclDto.setTargetName(shareName);
                aclDto.setTargetType(AclTargetType.SHARE.name());
            }
            if (groupName != null) {
                aclDto.setTargetName(groupName);
                aclDto.setTargetType(AclTargetType.GROUP.name());
            }
            aclDtoList.add(aclDto);
        }
        return aclDtoList;
    }

    /**
     * Check if a source is accessible to a target.
     *
     * @param sourceId ACL source entity ID
     * @param perm Necessary permission
     * @param targetIdList List of targets
     * @return True if the document is accessible
     */
    public boolean checkPermission(String sourceId, PermType perm, List<String> targetIdList) {
        if (SecurityUtil.skipAclCheck(targetIdList)) {
            return true;
        }
        if (targetIdList.isEmpty()) {
            return false;
        }

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select a.ACL_ID_C from T_ACL a ");
        sb.append(" where a.ACL_TARGETID_C in (:targetIdList) and a.ACL_SOURCEID_C = :sourceId and a.ACL_PERM_C = :perm and a.ACL_DELETEDATE_D is null ");
        sb.append(" union all ");
        sb.append(" select a.ACL_ID_C from T_ACL a, T_DOCUMENT_TAG dt, T_DOCUMENT d ");
        sb.append(" where a.ACL_SOURCEID_C = dt.DOT_IDTAG_C and dt.DOT_IDDOCUMENT_C = d.DOC_ID_C and dt.DOT_DELETEDATE_D is null ");
        sb.append(" and d.DOC_ID_C = :sourceId and d.DOC_DELETEDATE_D is null ");
        sb.append(" and a.ACL_TARGETID_C in (:targetIdList) and a.ACL_PERM_C = :perm and a.ACL_DELETEDATE_D is null ");
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("sourceId", sourceId);
        q.setParameter("perm", perm.name());
        q.setParameter("targetIdList", targetIdList);

        // We have a matching permission
        return q.getResultList().size() > 0;
    }

    /**
     * Delete an ACL.
     *
     * @param sourceId Source ID
     * @param perm Permission
     * @param targetId Target ID
     * @param userId User ID
     * @param type Type
     */
    @SuppressWarnings("unchecked")
    public void delete(String sourceId, PermType perm, String targetId, String userId, AclType type) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Create audit log
        Query q = em.createQuery("from Acl a where a.sourceId = :sourceId and a.perm = :perm and a.targetId = :targetId and a.type = :type and a.deleteDate is null");
        q.setParameter("sourceId", sourceId);
        q.setParameter("perm", perm);
        q.setParameter("targetId", targetId);
        q.setParameter("type", type);
        List<Acl> aclList = q.getResultList();
        for (Acl acl : aclList) {
            AuditLogUtil.create(acl, AuditLogType.DELETE, userId);
        }

        // Soft delete the ACLs
        q = em.createQuery("update Acl a set a.deleteDate = :dateNow where a.sourceId = :sourceId and a.perm = :perm and a.targetId = :targetId and a.type = :type and a.deleteDate is null");
        q.setParameter("sourceId", sourceId);
        q.setParameter("perm", perm);
        q.setParameter("targetId", targetId);
        q.setParameter("type", type);
        q.setParameter("dateNow", new Date());
        q.executeUpdate();
    }
}