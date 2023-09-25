package com.sismics.docs.rest.util;

import com.sismics.docs.core.dao.dto.TagDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tag utilities.
 *
 * @author bgamard
 */
public class TagUtil {
    /**
     * Recursively find children of a tag.
     *
     * @param parentTagDto Parent tag
     * @param allTagDtoList List of all tags
     * @return Children tags
     */
    public static List<TagDto> findChildren(TagDto parentTagDto, List<TagDto> allTagDtoList) {
        List<TagDto> childrenTagDtoList = new ArrayList<>();

        for (TagDto tagDto : allTagDtoList) {
            if (parentTagDto.getId().equals(tagDto.getParentId())) {
                childrenTagDtoList.add(tagDto);
                childrenTagDtoList.addAll(findChildren(tagDto, allTagDtoList));
            }
        }

        return childrenTagDtoList;
    }

    /**
     * Find tags by name (start with, ignore case).
     *
     * @param name Name
     * @param allTagDtoList List of all tags
     * @return List of filtered tags
     */
    public static List<TagDto> findByName(String name, List<TagDto> allTagDtoList) {
        if (name.isEmpty()) {
            return Collections.emptyList();
        }
        List<TagDto> tagDtoList = new ArrayList<>();
        name = name.toLowerCase();
        for (TagDto tagDto : allTagDtoList) {
            if (tagDto.getName().toLowerCase().startsWith(name)) {
                tagDtoList.add(tagDto);
            }
        }
        return tagDtoList;
    }
}
