package com.sismics.rest.util;

import com.sismics.docs.core.constant.AclType;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.AclDao;
import com.sismics.docs.core.dao.dto.AclDto;
import com.sismics.util.JsonUtil;

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
        List<AclDto> aclDtoList = aclDao.getBySourceId(sourceId, AclType.USER);
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
