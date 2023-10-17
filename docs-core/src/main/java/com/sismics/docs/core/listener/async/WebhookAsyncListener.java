package com.sismics.docs.core.listener.async;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.constant.WebhookEvent;
import com.sismics.docs.core.dao.WebhookDao;
import com.sismics.docs.core.dao.criteria.WebhookCriteria;
import com.sismics.docs.core.dao.dto.WebhookDto;
import com.sismics.docs.core.event.*;
import com.sismics.docs.core.util.TransactionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Listener for triggering webhooks.
 * 
 * @author bgamard
 */
public class WebhookAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(WebhookAsyncListener.class);

    /**
     * OkHttp client.
     */
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Subscribe
    @AllowConcurrentEvents
    public void on(final DocumentCreatedAsyncEvent event) {
        triggerWebhook(WebhookEvent.DOCUMENT_CREATED, event.getDocumentId());
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(final DocumentUpdatedAsyncEvent event) {
        triggerWebhook(WebhookEvent.DOCUMENT_UPDATED, event.getDocumentId());
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(final DocumentDeletedAsyncEvent event) {
        triggerWebhook(WebhookEvent.DOCUMENT_DELETED, event.getDocumentId());
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(final FileCreatedAsyncEvent event) {
        triggerWebhook(WebhookEvent.FILE_CREATED, event.getFileId());
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(final FileUpdatedAsyncEvent event) {
        triggerWebhook(WebhookEvent.FILE_UPDATED, event.getFileId());
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(final FileDeletedAsyncEvent event) {
        triggerWebhook(WebhookEvent.FILE_DELETED, event.getFileId());
    }

    /**
     * Trigger the webhooks for the specified event.
     *
     * @param event Event
     * @param id ID
     */
    private void triggerWebhook(WebhookEvent event, String id) {
        List<String> webhookUrlList = Lists.newArrayList();

        TransactionUtil.handle(() -> {
            WebhookDao webhookDao = new WebhookDao();
            List<WebhookDto> webhookDtoList = webhookDao.findByCriteria(new WebhookCriteria().setEvent(event), null);
            for (WebhookDto webhookDto : webhookDtoList) {
                webhookUrlList.add(webhookDto.getUrl());
            }
        });

        RequestBody body = RequestBody.create("{\"event\": \"" + event.name() + "\", \"id\": \"" + id + "\"}", JSON);

        for (String webhookUrl : webhookUrlList) {
            Request request = new Request.Builder()
                    .url(webhookUrl)
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                log.info("Successfully called the webhook at: " + webhookUrl + " - " + response.code());
            } catch (IOException e) {
                log.error("Error calling the webhook at: " + webhookUrl, e);
            }
        }
    }
}
