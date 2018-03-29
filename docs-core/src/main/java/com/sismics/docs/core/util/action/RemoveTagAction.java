package com.sismics.docs.core.util.action;

import com.google.common.collect.Sets;
import com.sismics.docs.core.dao.TagDao;
import com.sismics.docs.core.dao.criteria.TagCriteria;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.dao.dto.TagDto;

import javax.json.JsonObject;
import java.util.List;
import java.util.Set;

/**
 * Action to remove a tag.
 *
 * @author bgamard
 */
public class RemoveTagAction extends TagAction {
    @Override
    public void execute(DocumentDto documentDto, JsonObject action) {
        if (action.getString("tag") == null) {
            return;
        }


        String tagId = action.getString("tag");
        TagDao tagDao = new TagDao();
        List<TagDto> tagDtoList = tagDao.findByCriteria(new TagCriteria().setDocumentId(documentDto.getId()), null);
        Set<String> tagIdSet = Sets.newHashSet();
        for (TagDto tagDto : tagDtoList) {
            tagIdSet.add(tagDto.getId());
        }
        tagIdSet.remove(tagId);

        tagDao.updateTagList(documentDto.getId(), tagIdSet);
    }
}
