package com.sismics.docs.core.util.action;

import com.sismics.docs.core.dao.TagDao;
import com.sismics.docs.core.dao.criteria.TagCriteria;
import com.sismics.docs.core.dao.dto.TagDto;

import javax.json.JsonObject;
import java.util.List;

/**
 * Abstract class for tag actions.
 *
 * @author bgamard
 */
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
