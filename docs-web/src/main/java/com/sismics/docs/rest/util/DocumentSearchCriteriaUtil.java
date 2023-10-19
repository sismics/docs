package com.sismics.docs.rest.util;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.criteria.DocumentCriteria;
import com.sismics.docs.core.dao.dto.TagDto;
import com.sismics.docs.core.model.jpa.User;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DocumentSearchCriteriaUtil {
    private static final DateTimeParser YEAR_PARSER = DateTimeFormat.forPattern("yyyy").getParser();
    private static final DateTimeParser MONTH_PARSER = DateTimeFormat.forPattern("yyyy-MM").getParser();
    private static final DateTimeParser DAY_PARSER = DateTimeFormat.forPattern("yyyy-MM-dd").getParser();
    private static final DateTimeParser[] DATE_PARSERS = new DateTimeParser[]{
            YEAR_PARSER,
            MONTH_PARSER,
            DAY_PARSER};

    private static final DateTimeFormatter YEAR_FORMATTER = new DateTimeFormatter(null, YEAR_PARSER);
    private static final DateTimeFormatter MONTH_FORMATTER = new DateTimeFormatter(null, MONTH_PARSER);
    private static final DateTimeFormatter DAY_FORMATTER = new DateTimeFormatter(null, DAY_PARSER);
    private static final DateTimeFormatter DATES_FORMATTER = new DateTimeFormatterBuilder().append(null, DATE_PARSERS).toFormatter();

    private static final String PARAMETER_WITH_MULTIPLE_VALUES_SEPARATOR = ",";
    private static final String WORKFLOW_ME = "me";

    /**
     * Parse a query according to the specified syntax, eg.:
     * tag:assurance tag:other before:2012 after:2011-09 shared:yes lang:fra thing
     *
     * @param search        Search query
     * @param allTagDtoList List of tags
     * @return DocumentCriteria
     */
    public static DocumentCriteria parseSearchQuery(String search, List<TagDto> allTagDtoList) {
        DocumentCriteria documentCriteria = new DocumentCriteria();
        if (Strings.isNullOrEmpty(search)) {
            return documentCriteria;
        }

        String[] criteriaList = search.split(" +");
        List<String> simpleQuery = new ArrayList<>();
        List<String> fullQuery = new ArrayList<>();
        for (String criteria : criteriaList) {
            String[] params = criteria.split(":");
            if (params.length != 2 || Strings.isNullOrEmpty(params[0]) || Strings.isNullOrEmpty(params[1])) {
                // This is not a special criteria, do a fulltext search on it
                fullQuery.add(criteria);
                continue;
            }
            String paramName = params[0];
            String paramValue = params[1];

            switch (paramName) {
                case "tag":
                case "!tag":
                    parseTagCriteria(documentCriteria, paramValue, allTagDtoList, paramName.startsWith("!"));
                    break;
                case "after":
                case "before":
                case "uafter":
                case "ubefore":
                    parseDateCriteria(documentCriteria, paramValue, DATES_FORMATTER, paramName.startsWith("u"), paramName.endsWith("before"));
                    break;
                case "uat":
                case "at":
                    parseDateAtCriteria(documentCriteria, paramValue, params[0].startsWith("u"));
                    break;
                case "shared":
                    documentCriteria.setShared(paramValue.equals("yes"));
                    break;
                case "lang":
                    parseLangCriteria(documentCriteria, paramValue);
                    break;
                case "mime":
                    documentCriteria.setMimeType(paramValue);
                    break;
                case "by":
                    parseByCriteria(documentCriteria, paramValue);
                    break;
                case "workflow":
                    documentCriteria.setActiveRoute(paramValue.equals(WORKFLOW_ME));
                    break;
                case "simple":
                    simpleQuery.add(paramValue);
                    break;
                case "full":
                    fullQuery.add(paramValue);
                    break;
                case "title":
                    documentCriteria.getTitleList().add(paramValue);
                    break;
                default:
                    fullQuery.add(criteria);
                    break;
            }
        }

        documentCriteria.setSimpleSearch(Joiner.on(" ").join(simpleQuery));
        documentCriteria.setFullSearch(Joiner.on(" ").join(fullQuery));
        return documentCriteria;
    }


    /**
     * Fill the document criteria with various possible parameters
     *
     * @param documentCriteria    structure to be filled
     * @param searchBy            author
     * @param searchCreatedAfter  creation moment after
     * @param searchCreatedBefore creation moment before
     * @param searchFull          full search
     * @param searchLang          lang
     * @param searchMime          mime type
     * @param searchShared        share state
     * @param searchSimple        search in
     * @param searchTag           tags or parent tags
     * @param searchNotTag        tags or parent tags to ignore
     * @param searchTitle         title
     * @param searchUpdatedAfter  update moment after
     * @param searchUpdatedBefore update moment before
     * @param searchWorkflow      exiting workflow
     * @param allTagDtoList       list of existing tags
     */
    public static void addHttpSearchParams(
            DocumentCriteria documentCriteria,
            String searchBy,
            String searchCreatedAfter,
            String searchCreatedBefore,
            String searchFull,
            String searchLang,
            String searchMime,
            Boolean searchShared,
            String searchSimple,
            String searchTag,
            String searchNotTag,
            String searchTitle,
            String searchUpdatedAfter,
            String searchUpdatedBefore,
            String searchWorkflow,
            List<TagDto> allTagDtoList
    ) {
        if (searchBy != null) {
            parseByCriteria(documentCriteria, searchBy);
        }
        if (searchCreatedAfter != null) {
            parseDateCriteria(documentCriteria, searchCreatedAfter, DAY_FORMATTER, false, false);
        }
        if (searchCreatedBefore != null) {
            parseDateCriteria(documentCriteria, searchCreatedBefore, DAY_FORMATTER, false, true);
        }
        if (searchFull != null) {
            documentCriteria.setFullSearch(Joiner.on(" ").join(searchFull.split(PARAMETER_WITH_MULTIPLE_VALUES_SEPARATOR)));
        }
        if (searchLang != null) {
            parseLangCriteria(documentCriteria, searchLang);
        }
        if (searchMime != null) {
            documentCriteria.setMimeType(searchMime);
        }
        if ((searchShared != null) && searchShared) {
            documentCriteria.setShared(true);
        }
        if (searchSimple != null) {
            documentCriteria.setSimpleSearch(Joiner.on(" ").join(searchSimple.split(PARAMETER_WITH_MULTIPLE_VALUES_SEPARATOR)));
        }
        if (searchTitle != null) {
            documentCriteria.getTitleList().addAll(Arrays.asList(searchTitle.split(PARAMETER_WITH_MULTIPLE_VALUES_SEPARATOR)));
        }
        if (searchTag != null) {
            for (String tag : searchTag.split(PARAMETER_WITH_MULTIPLE_VALUES_SEPARATOR)) {
                parseTagCriteria(documentCriteria, tag, allTagDtoList, false);
            }
        }
        if (searchNotTag != null) {
            for (String tag : searchNotTag.split(PARAMETER_WITH_MULTIPLE_VALUES_SEPARATOR)) {
                parseTagCriteria(documentCriteria, tag, allTagDtoList, true);
            }
        }
        if (searchUpdatedAfter != null) {
            parseDateCriteria(documentCriteria, searchUpdatedAfter, DAY_FORMATTER, true, false);
        }
        if (searchUpdatedBefore != null) {
            parseDateCriteria(documentCriteria, searchUpdatedBefore, DAY_FORMATTER, true, true);
        }
        if ((WORKFLOW_ME.equals(searchWorkflow))) {
            documentCriteria.setActiveRoute(true);
        }
    }

    private static void parseDateCriteria(DocumentCriteria documentCriteria, String value, DateTimeFormatter formatter, boolean isUpdated, boolean isBefore) {
        try {
            DateTime date = formatter.parseDateTime(value);
            if (isBefore) {
                if (isUpdated) {
                    documentCriteria.setUpdateDateMax(date.toDate());
                } else {
                    documentCriteria.setCreateDateMax(date.toDate());
                }
            } else {
                if (isUpdated) {
                    documentCriteria.setUpdateDateMin(date.toDate());
                } else {
                    documentCriteria.setCreateDateMin(date.toDate());
                }
            }
        } catch (IllegalArgumentException e) {
            // Invalid date, returns no documents
            documentCriteria.setCreateDateMin(new Date(0));
            documentCriteria.setCreateDateMax(new Date(0));
        }
    }

    private static void parseDateAtCriteria(DocumentCriteria documentCriteria, String value, boolean isUpdated) {
        try {
            switch (value.length()) {
                case 10: {
                    DateTime date = DATES_FORMATTER.parseDateTime(value);
                    if (isUpdated) {
                        documentCriteria.setUpdateDateMin(date.toDate());
                        documentCriteria.setUpdateDateMax(date.plusDays(1).minusSeconds(1).toDate());
                    } else {
                        documentCriteria.setCreateDateMin(date.toDate());
                        documentCriteria.setCreateDateMax(date.plusDays(1).minusSeconds(1).toDate());
                    }
                    break;
                }
                case 7: {
                    DateTime date = MONTH_FORMATTER.parseDateTime(value);
                    if (isUpdated) {
                        documentCriteria.setUpdateDateMin(date.toDate());
                        documentCriteria.setUpdateDateMax(date.plusMonths(1).minusSeconds(1).toDate());
                    } else {
                        documentCriteria.setCreateDateMin(date.toDate());
                        documentCriteria.setCreateDateMax(date.plusMonths(1).minusSeconds(1).toDate());
                    }
                    break;
                }
                case 4: {
                    DateTime date = YEAR_FORMATTER.parseDateTime(value);
                    if (isUpdated) {
                        documentCriteria.setUpdateDateMin(date.toDate());
                        documentCriteria.setUpdateDateMax(date.plusYears(1).minusSeconds(1).toDate());
                    } else {
                        documentCriteria.setCreateDateMin(date.toDate());
                        documentCriteria.setCreateDateMax(date.plusYears(1).minusSeconds(1).toDate());
                    }
                    break;
                }
                default: {
                    // Invalid format, returns no documents
                    documentCriteria.setCreateDateMin(new Date(0));
                    documentCriteria.setCreateDateMax(new Date(0));
                }
            }
        } catch (IllegalArgumentException e) {
            // Invalid date, returns no documents
            documentCriteria.setCreateDateMin(new Date(0));
            documentCriteria.setCreateDateMax(new Date(0));
        }
    }

    private static void parseTagCriteria(DocumentCriteria documentCriteria, String value, List<TagDto> allTagDtoList, boolean exclusion) {
        List<TagDto> tagDtoList = TagUtil.findByName(value, allTagDtoList);
        if (tagDtoList.isEmpty()) {
            // No tag found, the request must return nothing
            documentCriteria.getTagIdList().add(Lists.newArrayList(UUID.randomUUID().toString()));
        } else {
            List<String> tagIdList = Lists.newArrayList();
            for (TagDto tagDto : tagDtoList) {
                tagIdList.add(tagDto.getId());
                List<TagDto> childrenTagDtoList = TagUtil.findChildren(tagDto, allTagDtoList);
                for (TagDto childrenTagDto : childrenTagDtoList) {
                    tagIdList.add(childrenTagDto.getId());
                }
            }
            if (exclusion) {
                documentCriteria.getExcludedTagIdList().add(tagIdList);
            } else {
                documentCriteria.getTagIdList().add(tagIdList);
            }
        }
    }

    private static void parseLangCriteria(DocumentCriteria documentCriteria, String value) {
        // New language criteria
        if (Constants.SUPPORTED_LANGUAGES.contains(value)) {
            documentCriteria.setLanguage(value);
        } else {
            // Unsupported language, returns no documents
            documentCriteria.setLanguage(UUID.randomUUID().toString());
        }
    }

    private static void parseByCriteria(DocumentCriteria documentCriteria, String value) {
        User user = new UserDao().getActiveByUsername(value);
        if (user == null) {
            // This user doesn't exist, return nothing
            documentCriteria.setCreatorId(UUID.randomUUID().toString());
        } else {
            // This user exists, search its documents
            documentCriteria.setCreatorId(user.getId());
        }
    }
}
