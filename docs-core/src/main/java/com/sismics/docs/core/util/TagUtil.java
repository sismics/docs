package com.sismics.docs.core.util;

import com.google.common.collect.Lists;
import com.sismics.docs.core.dao.jpa.dto.TagDto;

import java.util.List;

/**
 * Tag utilities.
 *
 * @author bgamard
 */
public class TagUtil {
    /**
     * Recursively find children of a tags.
     *
     * @param parentTagDto Parent tag
     * @param allTagDtoList List of all tags
     * @return Children tags
     */
    public static List<TagDto> findChildren(TagDto parentTagDto, List<TagDto> allTagDtoList) {
        List<TagDto> childrenTagDtoList = Lists.newArrayList();

        for (TagDto tagDto : allTagDtoList) {
            if (parentTagDto.getId().equals(tagDto.getParentId())) {
                childrenTagDtoList.add(tagDto);
                childrenTagDtoList.addAll(findChildren(tagDto, allTagDtoList));
            }
        }

        return childrenTagDtoList;
    }

    /**
     * Find tags by name (start with).
     *
     * @param name Name
     * @param allTagDtoList List of all tags
     * @return List of filtered tags
     */
    public static List<TagDto> findByName(String name, List<TagDto> allTagDtoList) {
        List<TagDto> tagDtoList = Lists.newArrayList();
        if (name == null || name.isEmpty()) {
            return tagDtoList;
        }
        name = name.toLowerCase();
        for (TagDto tagDto : allTagDtoList) {
            if (tagDto.getName().toLowerCase().startsWith(name)) {
                tagDtoList.add(tagDto);
            }
        }
        return tagDtoList;
    }
}
