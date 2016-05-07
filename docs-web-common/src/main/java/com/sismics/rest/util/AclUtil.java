package com.sismics.rest.util;

import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.docs.core.dao.jpa.dto.AclDto;
import com.sismics.security.IPrincipal;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.List;

/**
 * Acl utilities.
 *
 * @author bgamard
 */
public class AclUtil {
    /**
     * Add ACLs to a JSON response.
     *
     * @param json JSON
     * @param sourceId Source ID
     * @param targetIdList List of target ID
     */
    public static void addAcls(JsonObjectBuilder json, String sourceId, List<String> targetIdList) {
        AclDao aclDao = new AclDao();
        List<AclDto> aclDtoList = aclDao.getBySourceId(sourceId);
        JsonArrayBuilder aclList = Json.createArrayBuilder();
        for (AclDto aclDto : aclDtoList) {
            aclList.add(Json.createObjectBuilder()
                    .add("perm", aclDto.getPerm().name())
                    .add("id", aclDto.getTargetId())
                    .add("name", JsonUtil.nullable(aclDto.getTargetName()))
                    .add("type", aclDto.getTargetType()));
        }
        json.add("acls", aclList)
                .add("writable", aclDao.checkPermission(sourceId, PermType.WRITE, targetIdList));
    }
}
