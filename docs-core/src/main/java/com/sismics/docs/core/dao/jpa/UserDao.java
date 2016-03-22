package com.sismics.docs.core.dao.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.mindrot.jbcrypt.BCrypt;

import com.google.common.base.Joiner;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.jpa.criteria.UserCriteria;
import com.sismics.docs.core.dao.jpa.dto.UserDto;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

/**
 * User DAO.
 * 
 * @author jtremeaux
 */
public class UserDao {
    /**
     * Authenticates an user.
     * 
     * @param username User login
     * @param password User password
     * @return The authenticated user or null
     */
    public User authenticate(String username, String password) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null");
        q.setParameter("username", username);
        try {
            User user = (User) q.getSingleResult();
            if (!BCrypt.checkpw(password, user.getPassword())) {
                return null;
            }
            return user;
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Creates a new user.
     * 
     * @param user User to create
     * @param userId User ID
     * @return User ID
     * @throws Exception
     */
    public String create(User user, String userId) throws Exception {
        // Create the user UUID
        user.setId(UUID.randomUUID().toString());
        
        // Checks for user unicity
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null");
        q.setParameter("username", user.getUsername());
        List<?> l = q.getResultList();
        if (l.size() > 0) {
            throw new Exception("AlreadyExistingUsername");
        }
        
        // Create the user
        user.setCreateDate(new Date());
        user.setPassword(hashPassword(user.getPassword()));
        em.persist(user);
        
        // Create audit log
        AuditLogUtil.create(user, AuditLogType.CREATE, userId);
        
        return user.getId();
    }
    
    /**
     * Updates a user.
     * 
     * @param user User to update
     * @param userId User ID
     * @return Updated user
     */
    public User update(User user, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the user
        Query q = em.createQuery("select u from User u where u.id = :id and u.deleteDate is null");
        q.setParameter("id", user.getId());
        User userFromDb = (User) q.getSingleResult();

        // Update the user (except password)
        userFromDb.setEmail(user.getEmail());
        userFromDb.setStorageQuota(user.getStorageQuota());
        userFromDb.setStorageCurrent(user.getStorageCurrent());
        userFromDb.setTotpKey(user.getTotpKey());
        
        // Create audit log
        AuditLogUtil.create(userFromDb, AuditLogType.UPDATE, userId);
        
        return user;
    }
    
    /**
     * Updates a user's quota.
     * 
     * @param user User to update
     * @return Updated user
     */
    public User updateQuota(User user) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the user
        Query q = em.createQuery("select u from User u where u.id = :id and u.deleteDate is null");
        q.setParameter("id", user.getId());
        User userFromDb = (User) q.getSingleResult();

        // Update the user
        userFromDb.setStorageQuota(user.getStorageQuota());
        
        return user;
    }
    
    /**
     * Update the user password.
     * 
     * @param user User to update
     * @param userId User ID
     * @return Updated user
     */
    public User updatePassword(User user, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the user
        Query q = em.createQuery("select u from User u where u.id = :id and u.deleteDate is null");
        q.setParameter("id", user.getId());
        User userFromDb = (User) q.getSingleResult();

        // Update the user
        userFromDb.setPassword(hashPassword(user.getPassword()));
        
        // Create audit log
        AuditLogUtil.create(userFromDb, AuditLogType.UPDATE, userId);
        
        return user;
    }

    /**
     * Gets a user by its ID.
     * 
     * @param id User ID
     * @return User
     */
    public User getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(User.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Gets an active user by its username.
     * 
     * @param username User's username
     * @return User
     */
    public User getActiveByUsername(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null");
            q.setParameter("username", username);
            return (User) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Gets an active user by its password recovery token.
     * 
     * @param passwordResetKey Password recovery token
     * @return User
     */
    public User getActiveByPasswordResetKey(String passwordResetKey) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select u from User u where u.passwordResetKey = :passwordResetKey and u.deleteDate is null");
            q.setParameter("passwordResetKey", passwordResetKey);
            return (User) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Deletes a user.
     * 
     * @param username User's username
     * @param userId User ID
     */
    public void delete(String username, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the user
        Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null");
        q.setParameter("username", username);
        User userFromDb = (User) q.getSingleResult();
        
        // Delete the user
        Date dateNow = new Date();
        userFromDb.setDeleteDate(dateNow);

        // Delete linked data
        q = em.createQuery("delete from AuthenticationToken at where at.userId = :userId");
        q.setParameter("userId", userFromDb.getId());
        q.executeUpdate();
        
        q = em.createQuery("update Document d set d.deleteDate = :dateNow where d.userId = :userId and d.deleteDate is null");
        q.setParameter("userId", userFromDb.getId());
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
        
        q = em.createQuery("update File f set f.deleteDate = :dateNow where f.userId = :userId and f.deleteDate is null");
        q.setParameter("userId", userFromDb.getId());
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
        
        q = em.createQuery("update Acl a set a.deleteDate = :dateNow where a.targetId = :userId and a.deleteDate is null");
        q.setParameter("userId", userFromDb.getId());
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
        
        q = em.createQuery("update Comment c set c.deleteDate = :dateNow where c.userId = :userId and c.deleteDate is null");
        q.setParameter("userId", userFromDb.getId());
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
        
        // Create audit log
        AuditLogUtil.create(userFromDb, AuditLogType.DELETE, userId);
    }

    /**
     * Hash the user's password.
     * 
     * @param password Clear password
     * @return Hashed password
     */
    protected String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    /**
     * Returns the list of all users.
     * 
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @return List of users
     */
    public List<UserDto> findByCriteria(UserCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        List<String> criteriaList = new ArrayList<String>();
        
        StringBuilder sb = new StringBuilder("select u.USE_ID_C as c0, u.USE_USERNAME_C as c1, u.USE_EMAIL_C as c2, u.USE_CREATEDATE_D as c3, u.USE_STORAGECURRENT_N as c4, u.USE_STORAGEQUOTA_N as c5");
        sb.append(" from T_USER u ");
        
        // Add search criterias
        if (criteria.getSearch() != null) {
            criteriaList.add("lower(u.USE_USERNAME_C) like lower(:search)");
            parameterMap.put("search", "%" + criteria.getSearch() + "%");
        }
        
        if (criteria.getGroupId() != null) {
            sb.append(" join T_USER_GROUP ug on ug.UGP_IDUSER_C = u.USE_ID_C and ug.UGP_IDGROUP_C = :groupId and ug.UGP_DELETEDATE_D is null ");
            parameterMap.put("groupId", criteria.getGroupId());
        }
        
        criteriaList.add("u.USE_DELETEDATE_D is null");
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }
        
        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();
        
        // Assemble results
        List<UserDto> userDtoList = new ArrayList<UserDto>();
        for (Object[] o : l) {
            int i = 0;
            UserDto userDto = new UserDto();
            userDto.setId((String) o[i++]);
            userDto.setUsername((String) o[i++]);
            userDto.setEmail((String) o[i++]);
            userDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            userDto.setStorageCurrent(((Number) o[i++]).longValue());
            userDto.setStorageQuota(((Number) o[i++]).longValue());
            userDtoList.add(userDto);
        }
        return userDtoList;
    }
}
