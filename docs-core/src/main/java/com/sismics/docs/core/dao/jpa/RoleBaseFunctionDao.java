package com.sismics.docs.core.dao.jpa;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.common.collect.Sets;
import com.sismics.util.context.ThreadLocalContext;

/**
 * Role base functions DAO.
 * 
 * @author jtremeaux
 */
public class RoleBaseFunctionDao {
    /**
     * Find the set of base functions of a role.
     * 
     * @param roleId Role ID
     * @return Set of base functions
     */
    @SuppressWarnings("unchecked")
    public Set<String> findByRoleId(String roleId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select rbf.RBF_IDBASEFUNCTION_C from T_ROLE_BASE_FUNCTION rbf, T_ROLE r");
        sb.append(" where rbf.RBF_IDROLE_C = :roleId and rbf.RBF_DELETEDATE_D is null");
        sb.append(" and r.ROL_ID_C = rbf.RBF_IDROLE_C and r.ROL_DELETEDATE_D is null");
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("roleId", roleId);
        return Sets.newHashSet(q.getResultList());
    }
}
