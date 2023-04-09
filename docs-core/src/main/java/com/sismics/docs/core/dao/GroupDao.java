package com.sismics.docs.core.dao;

import com.google.common.base.Joiner;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.criteria.GroupCriteria;
import com.sismics.docs.core.dao.dto.GroupDto;
import com.sismics.docs.core.model.jpa.Group;
import com.sismics.docs.core.model.jpa.UserGroup;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.util.*;

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
     * @return Group
     */
    public Group getActiveByName(String name) {
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
     * Returns a group by ID.
     * 
     * @param id Group ID
     * @return Group
     */
    public Group getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select g from Group g where g.id = :id and g.deleteDate is null");
        q.setParameter("id", id);
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
        
        q = em.createQuery("update Acl a set a.deleteDate = :dateNow where a.targetId = :groupId and a.deleteDate is null");
        q.setParameter("groupId", groupDb.getId());
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();

        q = em.createQuery("update Group g set g.parentId = null where g.parentId = :groupId and g.deleteDate is null");
        q.setParameter("groupId", groupDb.getId());
        q.executeUpdate();

        // Create audit log
        AuditLogUtil.create(groupDb, AuditLogType.DELETE, userId);
    }
    
    /**
     * Add an user to a group.
     * 
     * @param userGroup User group
     * @return New ID
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
     * @param userId User ID
     */
    public void removeMember(String groupId, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the user group
        Query q = em.createQuery("select ug from UserGroup ug where ug.groupId = :groupId and ug.userId = :userId and ug.deleteDate is null");
        q.setParameter("groupId", groupId);
        q.setParameter("userId", userId);
        UserGroup userGroupDb = (UserGroup) q.getSingleResult();
        
        // Delete the user group
        Date dateNow = new Date();
        userGroupDb.setDeleteDate(dateNow);
    }
    
    /**
     * Returns the list of all groups.
     * 
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @return List of groups
     */
    public List<GroupDto> findByCriteria(GroupCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();
        
        StringBuilder sb = new StringBuilder("select g.GRP_ID_C as c0, g.GRP_NAME_C as c1, g.GRP_IDPARENT_C as c2, gp.GRP_NAME_C as c3, g.GRP_IDROLE_C ");
        if (criteria.getUserId() != null) {
            sb.append(" , ug.UGP_ID_C ");
        }
        sb.append(" from T_GROUP g ");
        sb.append(" left join T_GROUP gp on g.GRP_IDPARENT_C = gp.GRP_ID_C ");
        
        // Add search criterias
        if (criteria.getSearch() != null) {
            criteriaList.add("lower(g.GRP_NAME_C) like lower(:search)");
            parameterMap.put("search", "%" + criteria.getSearch() + "%");
        }
        if (criteria.getUserId() != null) {
            // Left join and post-filtering for recursive groups
            sb.append(criteria.isRecursive() ? " left " : "");
            sb.append(" join T_USER_GROUP ug on ug.UGP_IDGROUP_C = g.GRP_ID_C and ug.UGP_IDUSER_C = :userId and ug.UGP_DELETEDATE_D is null ");
            parameterMap.put("userId", criteria.getUserId());
        }
        
        criteriaList.add("g.GRP_DELETEDATE_D is null");

        sb.append(" where ");
        sb.append(Joiner.on(" and ").join(criteriaList));

        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();
        
        // Assemble results
        List<GroupDto> groupDtoList = new ArrayList<>();
        List<GroupDto> userGroupDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            GroupDto groupDto = new GroupDto()
                .setId((String) o[i++])
                .setName((String) o[i++])
                .setParentId((String) o[i++])
                .setParentName((String) o[i++])
                .setRoleId((String) o[i++]);
            groupDtoList.add(groupDto);
            if (criteria.getUserId() != null && o[i] != null) {
                userGroupDtoList.add(groupDto);
            }
        }
        
        // Post-query filtering for recursive groups
        if (criteria.getUserId() != null && criteria.isRecursive()) {
            Set<GroupDto> filteredGroupDtoSet = new HashSet<>();
            for (GroupDto userGroupDto : userGroupDtoList) {
                filteredGroupDtoSet.add(userGroupDto); // Direct group
                findGroupParentHierarchy(filteredGroupDtoSet, groupDtoList, userGroupDto, 0); // Indirect groups
            }
            groupDtoList = new ArrayList<>(filteredGroupDtoSet);
        }
        
        return groupDtoList;
    }
    
    /**
     * Recursively search group's parents.
     * 
     * @param parentGroupDtoSet Resulting parents
     * @param groupDtoList All groups
     * @param userGroupDto Reference group to search from
     * @param depth Depth
     */
    private void findGroupParentHierarchy(Set<GroupDto> parentGroupDtoSet, List<GroupDto> groupDtoList, GroupDto userGroupDto, int depth) {
        if (userGroupDto.getParentId() == null || depth == 10) { // Max depth 10 to avoid infinite loop
            return;
        }
        
        for (GroupDto groupDto : groupDtoList) {
            if (groupDto.getId().equals(userGroupDto.getParentId())) {
                parentGroupDtoSet.add(groupDto); // Add parent
                findGroupParentHierarchy(parentGroupDtoSet, groupDtoList, groupDto, depth + 1); // Find parent's parents
            }
        }
    }
    
    /**
     * Update a group.
     * 
     * @param group Group to update
     * @param userId User ID
     * @return Updated group
     */
    public Group update(Group group, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the group
        Query q = em.createQuery("select g from Group g where g.id = :id and g.deleteDate is null");
        q.setParameter("id", group.getId());
        Group groupDb = (Group) q.getSingleResult();
        
        // Update the group
        groupDb.setName(group.getName());
        groupDb.setParentId(group.getParentId());
        
        // Create audit log
        AuditLogUtil.create(groupDb, AuditLogType.UPDATE, userId);
        
        return groupDb;
    }
}

