package com.sismics.docs.core.util.action;

import com.google.common.collect.Sets;
import com.sismics.docs.core.dao.TagDao;
import com.sismics.docs.core.dao.criteria.TagCriteria;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.dao.dto.TagDto;

import jakarta.json.JsonObject;
import java.util.List;
import java.util.Set;

/**
 * Action to add a tag.
 *
 * @author bgamard
 */
public class AddTagAction extends TagAction {
    @Override
    public void execute(DocumentDto documentDto, JsonObject action) {
        if (action.getString("tag") == null) {
            return;
        }

        TagDao tagDao = new TagDao();
        List<TagDto> tagAddDtoList = tagDao.findByCriteria(new TagCriteria().setId(action.getString("tag")), null);
        if (tagAddDtoList.isEmpty()) {
            // The tag has been deleted since the route model creation
            return;
        }

        List<TagDto> tagDtoList = tagDao.findByCriteria(new TagCriteria().setDocumentId(documentDto.getId()), null);
        Set<String> tagIdSet = Sets.newHashSet(tagAddDtoList.get(0).getId());
        for (TagDto tagDto : tagDtoList) {
            tagIdSet.add(tagDto.getId());
        }

        tagDao.updateTagList(documentDto.getId(), tagIdSet);
    }
}
