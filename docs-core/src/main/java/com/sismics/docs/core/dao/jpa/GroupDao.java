package com.sismics.docs.core.dao.jpa;

import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.model.jpa.Group;
import com.sismics.docs.core.model.jpa.UserGroup;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;

/**
 * Group DAO.
 * 
 * @author bgamard
 */
public class GroupDao {
    /**
     * Returns a group by name.
     * 
     * @param name Name
     * @return Tag
     */
    public Group getByName(String name) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select g from Group g where g.name = :name and g.deleteDate is null");
        q.setParameter("name", name);
        try {
            return (Group) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Creates a new group.
     * 
     * @param group Group
     * @param userId User ID
     * @return New ID
     * @throws Exception
     */
    public String create(Group group, String userId) {
        // Create the UUID
        group.setId(UUID.randomUUID().toString());
        
        // Create the group
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(group);
        
        // Create audit log
        AuditLogUtil.create(group, AuditLogType.CREATE, userId);
        
        return group.getId();
    }
    
    /**
     * Deletes a group.
     * 
     * @param groupId Group ID
     * @param userId User ID
     */
    public void delete(String groupId, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the group
        Query q = em.createQuery("select g from Group g where g.id = :id and g.deleteDate is null");
        q.setParameter("id", groupId);
        Group groupDb = (Group) q.getSingleResult();
        
        // Delete the group
        Date dateNow = new Date();
        groupDb.setDeleteDate(dateNow);
        
        // Delete linked data
        q = em.createQuery("update UserGroup ug set ug.deleteDate = :dateNow where ug.groupId = :groupId and ug.deleteDate is not null");
        q.setParameter("dateNow", dateNow);
        q.setParameter("groupId", groupId);
        q.executeUpdate();

        // Create audit log
        AuditLogUtil.create(groupDb, AuditLogType.DELETE, userId);
    }
    
    /**
     * Add an user to a group.
     * 
     * @param group Group
     * @return New ID
     * @throws Exception
     */
    public String addMember(UserGroup userGroup) {
        // Create the UUID
        userGroup.setId(UUID.randomUUID().toString());
        
        // Create the user group
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(userGroup);
        
        return userGroup.getId();
    }
    
    /**
     * Remove an user from a group.
     * 
     * @param groupId Group ID
     */
    public void removeMember(String userGroupId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the user group
        Query q = em.createQuery("select ug from UserGroup ug where ug.id = :id and ug.deleteDate is null");
        q.setParameter("id", userGroupId);
        UserGroup userGroupDb = (UserGroup) q.getSingleResult();
        
        // Delete the user group
        Date dateNow = new Date();
        userGroupDb.setDeleteDate(dateNow);
    }
}

