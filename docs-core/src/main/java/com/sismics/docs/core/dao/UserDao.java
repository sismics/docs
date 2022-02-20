package com.sismics.docs.core.dao;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import at.favre.lib.crypto.bcrypt.BCrypt;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.criteria.UserCriteria;
import com.sismics.docs.core.dao.dto.UserDto;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.docs.core.util.EncryptionUtil;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;

/**
 * User DAO.
 * 
 * @author jtremeaux
 */
public class UserDao {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

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
            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
            if (!result.verified || user.getDisableDate() != null) {
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
     * @throws Exception e
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
        user.setPrivateKey(EncryptionUtil.generatePrivateKey());
        user.setStorageCurrent(0L);
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
        User userDb = (User) q.getSingleResult();

        // Update the user (except password)
        userDb.setEmail(user.getEmail());
        userDb.setStorageQuota(user.getStorageQuota());
        userDb.setStorageCurrent(user.getStorageCurrent());
        userDb.setTotpKey(user.getTotpKey());
        userDb.setDisableDate(user.getDisableDate());

        // Create audit log
        AuditLogUtil.create(userDb, AuditLogType.UPDATE, userId);
        
        return user;
    }
    
    /**
     * Updates a user's quota.
     * 
     * @param user User to update
     */
    public void updateQuota(User user) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the user
        Query q = em.createQuery("select u from User u where u.id = :id and u.deleteDate is null");
        q.setParameter("id", user.getId());
        User userDb = (User) q.getSingleResult();

        // Update the user
        userDb.setStorageCurrent(user.getStorageCurrent());
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
        User userDb = (User) q.getSingleResult();

        // Update the user
        userDb.setPassword(hashPassword(user.getPassword()));
        
        // Create audit log
        AuditLogUtil.create(userDb, AuditLogType.UPDATE, userId);
        
        return user;
    }

    /**
     * Update the hashed password silently.
     *
     * @param user User to update
     * @return Updated user
     */
    public User updateHashedPassword(User user) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the user
        Query q = em.createQuery("select u from User u where u.id = :id and u.deleteDate is null");
        q.setParameter("id", user.getId());
        User userDb = (User) q.getSingleResult();

        // Update the user
        userDb.setPassword(user.getPassword());

        return user;
    }

    /**
     * Update the onboarding status.
     *
     * @param user User to update
     * @return Updated user
     */
    public User updateOnboarding(User user) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the user
        Query q = em.createQuery("select u from User u where u.id = :id and u.deleteDate is null");
        q.setParameter("id", user.getId());
        User userDb = (User) q.getSingleResult();

        // Update the user
        userDb.setOnboarding(user.isOnboarding());

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
        User userDb = (User) q.getSingleResult();
        
        // Delete the user
        Date dateNow = new Date();
        userDb.setDeleteDate(dateNow);

        // Delete linked data
        q = em.createQuery("delete from AuthenticationToken at where at.userId = :userId");
        q.setParameter("userId", userDb.getId());
        q.executeUpdate();
        
        q = em.createQuery("update Document d set d.deleteDate = :dateNow where d.userId = :userId and d.deleteDate is null");
        q.setParameter("userId", userDb.getId());
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
        
        q = em.createQuery("update File f set f.deleteDate = :dateNow where f.userId = :userId and f.deleteDate is null");
        q.setParameter("userId", userDb.getId());
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
        
        q = em.createQuery("update Acl a set a.deleteDate = :dateNow where a.targetId = :userId and a.deleteDate is null");
        q.setParameter("userId", userDb.getId());
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
        
        q = em.createQuery("update Comment c set c.deleteDate = :dateNow where c.userId = :userId and c.deleteDate is null");
        q.setParameter("userId", userDb.getId());
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
        
        // Create audit log
        AuditLogUtil.create(userDb, AuditLogType.DELETE, userId);
    }

    /**
     * Hash the user's password.
     * 
     * @param password Clear password
     * @return Hashed password
     */
    private String hashPassword(String password) {
        int bcryptWork = Constants.DEFAULT_BCRYPT_WORK;
        String envBcryptWork = System.getenv(Constants.BCRYPT_WORK_ENV);
        if (!Strings.isNullOrEmpty(envBcryptWork)) {
            try {
                int envBcryptWorkInt = Integer.parseInt(envBcryptWork);
                if (envBcryptWorkInt >= 4 && envBcryptWorkInt <= 31) {
                    bcryptWork = envBcryptWorkInt;
                } else {
                    log.warn(Constants.BCRYPT_WORK_ENV + " needs to be in range 4...31. Falling back to " + Constants.DEFAULT_BCRYPT_WORK + ".");
                }
            } catch (NumberFormatException e) {
                log.warn(Constants.BCRYPT_WORK_ENV + " needs to be a number in range 4...31. Falling back to " + Constants.DEFAULT_BCRYPT_WORK + ".");
            }
        }
        return BCrypt.withDefaults().hashToString(bcryptWork, password.toCharArray());
    }
    
    /**
     * Returns the list of all users.
     * 
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @return List of users
     */
    public List<UserDto> findByCriteria(UserCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();
        
        StringBuilder sb = new StringBuilder("select u.USE_ID_C as c0, u.USE_USERNAME_C as c1, u.USE_EMAIL_C as c2, u.USE_CREATEDATE_D as c3, u.USE_STORAGECURRENT_N as c4, u.USE_STORAGEQUOTA_N as c5, u.USE_TOTPKEY_C as c6, u.USE_DISABLEDATE_D as c7");
        sb.append(" from T_USER u ");
        
        // Add search criterias
        if (criteria.getSearch() != null) {
            criteriaList.add("lower(u.USE_USERNAME_C) like lower(:search)");
            parameterMap.put("search", "%" + criteria.getSearch() + "%");
        }
        if (criteria.getUserId() != null) {
            criteriaList.add("u.USE_ID_C = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }
        if (criteria.getUserName() != null) {
            criteriaList.add("u.USE_USERNAME_C = :userName");
            parameterMap.put("userName", criteria.getUserName());
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
        List<UserDto> userDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            UserDto userDto = new UserDto();
            userDto.setId((String) o[i++]);
            userDto.setUsername((String) o[i++]);
            userDto.setEmail((String) o[i++]);
            userDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            userDto.setStorageCurrent(((Number) o[i++]).longValue());
            userDto.setStorageQuota(((Number) o[i++]).longValue());
            userDto.setTotpKey((String) o[i++]);
            if (o[i] != null) {
                userDto.setDisableTimestamp(((Timestamp) o[i]).getTime());
            }
            userDtoList.add(userDto);
        }
        return userDtoList;
    }

    /**
     * Returns the global storage used by all users.
     *
     * @return Current global storage
     */
    public long getGlobalStorageCurrent() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createNativeQuery("select sum(u.USE_STORAGECURRENT_N) from T_USER u where u.USE_DELETEDATE_D is null");
        return ((Number) query.getSingleResult()).longValue();
    }

    /**
     * Returns the number of active users.
     *
     * @return Number of active users
     */
    public long getActiveUserCount() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createNativeQuery("select count(u.USE_ID_C) from T_USER u where u.USE_DELETEDATE_D is null and (u.USE_DISABLEDATE_D is null or u.USE_DISABLEDATE_D >= :fromDate and u.USE_DISABLEDATE_D < :toDate)");
        DateTime fromDate = DateTime.now().minusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
        DateTime toDate = fromDate.plusMonths(1);
        query.setParameter("fromDate", fromDate.toDate());
        query.setParameter("toDate", toDate.toDate());
        return ((Number) query.getSingleResult()).longValue();
    }
}
