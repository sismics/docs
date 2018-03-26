package com.sismics.docs.core.util;

import com.google.common.collect.Lists;
import com.sismics.docs.core.constant.AclTargetType;
import com.sismics.docs.core.constant.AclType;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.dao.jpa.criteria.UserCriteria;
import com.sismics.docs.core.dao.jpa.dto.RouteStepDto;
import com.sismics.docs.core.dao.jpa.dto.UserDto;
import com.sismics.docs.core.event.RouteStepValidateEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.Acl;
import com.sismics.docs.core.model.jpa.Document;

import java.util.List;

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

    /**
     * Send an email when a route step is validated.
     *
     * @param documentId Document ID
     * @param routeStepDto Route step DTO
     */
    public static void sendRouteStepEmail(String documentId, RouteStepDto routeStepDto) {
        DocumentDao documentDao = new DocumentDao();
        Document document = documentDao.getById(documentId);

        List<UserDto> userDtoList = Lists.newArrayList();
                UserDao userDao = new UserDao();
        switch (AclTargetType.valueOf(routeStepDto.getTargetType())) {
            case USER:
                userDtoList.addAll(userDao.findByCriteria(new UserCriteria().setUserId(routeStepDto.getTargetId()), null));
                break;
            case GROUP:
                userDtoList.addAll(userDao.findByCriteria(new UserCriteria().setGroupId(routeStepDto.getTargetId()), null));
                break;
        }

        // Fire route step validate events
        for (UserDto userDto : userDtoList) {
            RouteStepValidateEvent routeStepValidateEvent = new RouteStepValidateEvent();
            routeStepValidateEvent.setUser(userDto);
            routeStepValidateEvent.setDocument(document);
            AppContext.getInstance().getMailEventBus().post(routeStepValidateEvent);
        }
    }
}
