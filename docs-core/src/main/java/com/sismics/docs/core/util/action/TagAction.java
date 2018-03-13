package com.sismics.docs.core.util.action;

import com.sismics.docs.core.dao.jpa.TagDao;
import com.sismics.docs.core.dao.jpa.criteria.TagCriteria;
import com.sismics.docs.core.dao.jpa.dto.TagDto;

import javax.json.JsonObject;
import java.util.List;

public abstract class TagAction implements Action {
    @Override
    public void validate(JsonObject action) throws Exception {
        TagDao tagDao = new TagDao();
        String tagId = action.getString("tag");
        if (tagId == null) {
            throw new Exception("step.transitions.actions.tag is required");
        }
        List<TagDto> tagDtoList = tagDao.findByCriteria(new TagCriteria().setId(tagId), null);
        if (tagDtoList.size() != 1) {
            throw new Exception(tagId + " is not a valid tag");
        }
    }
}
