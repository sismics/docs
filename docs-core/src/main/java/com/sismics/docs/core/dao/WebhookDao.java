package com.sismics.docs.core.dao;

import com.google.common.base.Joiner;
import com.sismics.docs.core.dao.criteria.WebhookCriteria;
import com.sismics.docs.core.dao.dto.WebhookDto;
import com.sismics.docs.core.model.jpa.Webhook;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;

/**
 * Webhook DAO.
 * 
 * @author bgamard
 */
public class WebhookDao {
    /**
     * Returns the list of all webhooks.
     * 
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @return List of webhooks
     */
    public List<WebhookDto> findByCriteria(WebhookCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();
        
        StringBuilder sb = new StringBuilder("select w.WHK_ID_C as c0, w.WHK_EVENT_C as c1, w.WHK_URL_C as c2, w.WHK_CREATEDATE_D as c3 ");
        sb.append(" from T_WEBHOOK w ");

        // Add search criterias
        if (criteria.getEvent() != null) {
            criteriaList.add("w.WHK_EVENT_C = :event");
            parameterMap.put("event", criteria.getEvent().name());
        }
        criteriaList.add("w.WHK_DELETEDATE_D is null");

        sb.append(" where ");
        sb.append(Joiner.on(" and ").join(criteriaList));

        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();
        
        // Assemble results
        List<WebhookDto> webhookDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            WebhookDto webhookDto = new WebhookDto()
                .setId((String) o[i++])
                .setEvent((String) o[i++])
                .setUrl((String) o[i++])
                .setCreateTimestamp(((Timestamp) o[i]).getTime());
            webhookDtoList.add(webhookDto);
        }
        
        return webhookDtoList;
    }

    /**
     * Returns a webhook by ID.
     *
     * @param id Webhook ID
     * @return Webhook
     */
    public Webhook getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select w from Webhook w where w.id = :id and w.deleteDate is null");
        q.setParameter("id", id);
        try {
            return (Webhook) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Creates a new webhook.
     *
     * @param webhook Webhook
     * @return New ID
     */
    public String create(Webhook webhook) {
        // Create the UUID
        webhook.setId(UUID.randomUUID().toString());

        // Create the webhook
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        webhook.setCreateDate(new Date());
        em.persist(webhook);

        return webhook.getId();
    }

    /**
     * Deletes a webhook.
     *
     * @param webhookId Webhook ID
     */
    public void delete(String webhookId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the group
        Query q = em.createQuery("select w from Webhook w where w.id = :id and w.deleteDate is null");
        q.setParameter("id", webhookId);
        Webhook webhookDb = (Webhook) q.getSingleResult();

        // Delete the group
        Date dateNow = new Date();
        webhookDb.setDeleteDate(dateNow);
    }
}

