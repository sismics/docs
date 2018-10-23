package com.sismics.docs.core.dao;

import com.google.common.collect.Sets;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Set;

/**
 * Role base functions DAO.
 * 
 * @author jtremeaux
 */
public class RoleBaseFunctionDao {
    /**
     * Find the set of base functions of a role.
     * 
     * @param roleIdSet Set of role ID
     * @return Set of base functions
     */
    @SuppressWarnings("unchecked")
    public Set<String> findByRoleId(Set<String> roleIdSet) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select rbf.RBF_IDBASEFUNCTION_C from T_ROLE_BASE_FUNCTION rbf, T_ROLE r");
        sb.append(" where rbf.RBF_IDROLE_C in (:roleIdSet) and rbf.RBF_DELETEDATE_D is null");
        sb.append(" and r.ROL_ID_C = rbf.RBF_IDROLE_C and r.ROL_DELETEDATE_D is null");
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("roleIdSet", roleIdSet);
        return Sets.newHashSet(q.getResultList());
    }
}
