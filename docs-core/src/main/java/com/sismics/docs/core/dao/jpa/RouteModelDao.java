package com.sismics.docs.core.dao.jpa;

import com.google.common.base.Joiner;
import com.sismics.docs.core.dao.jpa.criteria.RouteModelCriteria;
import com.sismics.docs.core.dao.jpa.dto.RouteModelDto;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Route model DAO.
 * 
 * @author bgamard
 */
public class RouteModelDao {
    /**
     * Returns the list of all route models.
     *
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @return List of route models
     */
    public List<RouteModelDto> findByCriteria(RouteModelCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        List<String> criteriaList = new ArrayList<>();

        StringBuilder sb = new StringBuilder("select rm.RTM_ID_C c0, rm.RTM_NAME_C c1, rm.RTM_CREATEDATE_D c2");
        sb.append(" from T_ROUTE_MODEL rm ");

        // Add search criterias
        criteriaList.add("rm.RTM_DELETEDATE_D is null");

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }

        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();

        // Assemble results
        List<RouteModelDto> dtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            RouteModelDto dto = new RouteModelDto();
            dto.setId((String) o[i++]);
            dto.setName((String) o[i++]);
            dto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            dtoList.add(dto);
        }
        return dtoList;
    }
}
