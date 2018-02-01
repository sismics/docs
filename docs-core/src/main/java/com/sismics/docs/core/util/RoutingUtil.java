package com.sismics.docs.core.util;

import com.sismics.docs.core.constant.AclType;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.docs.core.dao.jpa.dto.RouteStepDto;
import com.sismics.docs.core.model.jpa.Acl;

/**
 * Routing utilities.
 *
 * @author bgamard
 */
public class RoutingUtil {
    /**
     * Update routing ACLs according to the current route step.
     *
     * @param sourceId Source ID
     * @param currentStep Current route step
     * @param previousStep Previous route step
     * @param userId User ID
     */
    public static void updateAcl(String sourceId, RouteStepDto currentStep, RouteStepDto previousStep, String userId) {
        AclDao aclDao = new AclDao();

        if (previousStep != null) {
            // Remove the previous ACL
            aclDao.delete(sourceId, PermType.READ, previousStep.getTargetId(), userId, AclType.ROUTING);
        }

        if (currentStep != null) {
            // Create a temporary READ ACL
            Acl acl = new Acl();
            acl.setPerm(PermType.READ);
            acl.setType(AclType.ROUTING);
            acl.setSourceId(sourceId);
            acl.setTargetId(currentStep.getTargetId());
            aclDao.create(acl, userId);
        }
    }
}
