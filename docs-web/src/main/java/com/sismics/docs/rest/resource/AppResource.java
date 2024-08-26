package com.sismics.docs.rest.resource;

import com.google.common.base.Strings;
import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.ConfigDao;
import com.sismics.docs.core.dao.DocumentDao;
import com.sismics.docs.core.dao.FileDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.event.RebuildIndexAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.Config;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.service.InboxService;
import com.sismics.docs.core.util.ConfigUtil;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.JsonUtil;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.log4j.LogCriteria;
import com.sismics.util.log4j.LogEntry;
import com.sismics.util.log4j.MemoryAppender;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.*;

/**
 * General app REST resource.
 *
 * @author jtremeaux
 */
@Path("/app")
public class AppResource extends BaseResource {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AppResource.class);

    /**
     * Returns information about the application.
     *
     * @api {get} /app Get application information
     * @apiName GetApp
     * @apiGroup App
     * @apiSuccess {String} current_version API current version
     * @apiSuccess {String} min_version API minimum version
     * @apiSuccess {Boolean} guest_login True if guest login is enabled
     * @apiSuccess {String} default_language Default platform language
     * @apiSuccess {Number} queued_tasks Number of queued tasks waiting to be processed
     * @apiSuccess {String} total_memory Allocated JVM memory (in bytes)
     * @apiSuccess {String} free_memory Free JVM memory (in bytes)
     * @apiSuccess {String} document_count Number of documents
     * @apiSuccess {String} active_user_count Number of active users
     * @apiSuccess {String} global_storage_current Global storage currently used (in bytes)
     * @apiSuccess {String} global_storage_quota Maximum global storage (in bytes)
     * @apiPermission none
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @GET
    public Response info() {
        ResourceBundle configBundle = ConfigUtil.getConfigBundle();
        String currentVersion = configBundle.getString("api.current_version");
        String minVersion = configBundle.getString("api.min_version");
        Boolean guestLogin = ConfigUtil.getConfigBooleanValue(ConfigType.GUEST_LOGIN);
        Boolean ocrEnabled = ConfigUtil.getConfigBooleanValue(ConfigType.OCR_ENABLED, true);
        String defaultLanguage = ConfigUtil.getConfigStringValue(ConfigType.DEFAULT_LANGUAGE);
        UserDao userDao = new UserDao();
        DocumentDao documentDao = new DocumentDao();
        String globalQuotaStr = System.getenv(Constants.GLOBAL_QUOTA_ENV);
        long globalQuota = 0;
        if (!Strings.isNullOrEmpty(globalQuotaStr)) {
            globalQuota = Long.valueOf(globalQuotaStr);
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("current_version", currentVersion.replace("-SNAPSHOT", ""))
                .add("min_version", minVersion)
                .add("guest_login", guestLogin)
                .add("ocr_enabled", ocrEnabled)
                .add("default_language", defaultLanguage)
                .add("queued_tasks", AppContext.getInstance().getQueuedTaskCount())
                .add("total_memory", Runtime.getRuntime().totalMemory())
                .add("free_memory", Runtime.getRuntime().freeMemory())
                .add("document_count", documentDao.getDocumentCount())
                .add("active_user_count", userDao.getActiveUserCount())
                .add("global_storage_current", userDao.getGlobalStorageCurrent());
        if (globalQuota > 0) {
            response.add("global_storage_quota", globalQuota);
        }

        return Response.ok().entity(response.build()).build();
    }

    /**
     * Enable/disable guest login.
     *
     * @api {post} /app/guest_login Enable/disable guest login
     * @apiName PostAppGuestLogin
     * @apiGroup App
     * @apiParam {Boolean} enabled If true, enable guest login
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param enabled If true, enable guest login
     * @return Response
     */
    @POST
    @Path("guest_login")
    public Response guestLogin(@FormParam("enabled") Boolean enabled) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        ConfigDao configDao = new ConfigDao();
        configDao.update(ConfigType.GUEST_LOGIN, enabled.toString());

        return Response.ok().build();
    }

    /**
     * Enable/disable OCR.
     *
     * @api {post} /app/ocr Enable/disable OCR
     * @apiName PostAppOcr
     * @apiGroup App
     * @apiParam {Boolean} enabled If true, enable OCR
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param enabled If true, enable OCR
     * @return Response
     */
    @POST
    @Path("ocr")
    public Response ocr(@FormParam("enabled") Boolean enabled) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        ConfigDao configDao = new ConfigDao();
        configDao.update(ConfigType.OCR_ENABLED, enabled.toString());

        return Response.ok().build();
    }

    /**
     * General application configuration.
     *
     * @api {post} /app/config General application configuration
     * @apiName PostAppConfig
     * @apiGroup App
     * @apiParam {String} default_language Default language
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param defaultLanguage Default language
     * @return Response
     */
    @POST
    @Path("config")
    public Response config(@FormParam("default_language") String defaultLanguage) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        ValidationUtil.validateRequired(defaultLanguage, "default_language");
        if (!Constants.SUPPORTED_LANGUAGES.contains(defaultLanguage)) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} is not a supported language", defaultLanguage));
        }

        ConfigDao configDao = new ConfigDao();
        configDao.update(ConfigType.DEFAULT_LANGUAGE, defaultLanguage);

        return Response.ok().build();
    }

    /**
     * Get the SMTP server configuration.
     *
     * @api {get} /app/config_smtp Get the SMTP server configuration
     * @apiName GetAppConfigSmtp
     * @apiGroup App
     * @apiSuccess {String} hostname SMTP hostname
     * @apiSuccess {String} port SMTP port
     * @apiSuccess {String} username SMTP username
     * @apiSuccess {String} password SMTP password
     * @apiSuccess {String} from From address
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @GET
    @Path("config_smtp")
    public Response getConfigSmtp() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        ConfigDao configDao = new ConfigDao();
        Config hostnameConfig = configDao.getById(ConfigType.SMTP_HOSTNAME);
        Config portConfig = configDao.getById(ConfigType.SMTP_PORT);
        Config usernameConfig = configDao.getById(ConfigType.SMTP_USERNAME);
        Config passwordConfig = configDao.getById(ConfigType.SMTP_PASSWORD);
        Config fromConfig = configDao.getById(ConfigType.SMTP_FROM);
        JsonObjectBuilder response = Json.createObjectBuilder();
        if (Strings.isNullOrEmpty(System.getenv(Constants.SMTP_HOSTNAME_ENV))) {
            if (hostnameConfig == null) {
                response.addNull("hostname");
            } else {
                response.add("hostname", hostnameConfig.getValue());
            }
        }
        if (Strings.isNullOrEmpty(System.getenv(Constants.SMTP_PORT_ENV))) {
            if (portConfig == null) {
                response.addNull("port");
            } else {
                response.add("port", Integer.valueOf(portConfig.getValue()));
            }
        }
        if (Strings.isNullOrEmpty(System.getenv(Constants.SMTP_USERNAME_ENV))) {
            if (usernameConfig == null) {
                response.addNull("username");
            } else {
                response.add("username", usernameConfig.getValue());
            }
        }
        if (Strings.isNullOrEmpty(System.getenv(Constants.SMTP_PASSWORD_ENV))) {
            if (passwordConfig == null) {
                response.addNull("password");
            } else {
                response.add("password", passwordConfig.getValue());
            }
        }
        if (fromConfig == null) {
            response.addNull("from");
        } else {
            response.add("from", fromConfig.getValue());
        }

        return Response.ok().entity(response.build()).build();
    }

    /**
     * Configure the SMTP server.
     *
     * @api {post} /app/config_smtp Configure the SMTP server
     * @apiName PostAppConfigSmtp
     * @apiGroup App
     * @apiParam {String} hostname SMTP hostname
     * @apiParam {Integer} port SMTP port
     * @apiParam {String} username SMTP username
     * @apiParam {String} password SMTP password
     * @apiParam {String} from From address
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param hostname SMTP hostname
     * @param portStr SMTP port
     * @param username SMTP username
     * @param password SMTP password
     * @param from From address
     * @return Response
     */
    @POST
    @Path("config_smtp")
    public Response configSmtp(@FormParam("hostname") String hostname,
                               @FormParam("port") String portStr,
                               @FormParam("username") String username,
                               @FormParam("password") String password,
                               @FormParam("from") String from) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        if (!Strings.isNullOrEmpty(portStr)) {
            ValidationUtil.validateInteger(portStr, "port");
        }

        // Just update the changed configuration
        ConfigDao configDao = new ConfigDao();
        if (!Strings.isNullOrEmpty(hostname)) {
            configDao.update(ConfigType.SMTP_HOSTNAME, hostname);
        }
        if (!Strings.isNullOrEmpty(portStr)) {
            configDao.update(ConfigType.SMTP_PORT, portStr);
        }
        if (!Strings.isNullOrEmpty(username)) {
            configDao.update(ConfigType.SMTP_USERNAME, username);
        }
        if (!Strings.isNullOrEmpty(password)) {
            configDao.update(ConfigType.SMTP_PASSWORD, password);
        }
        if (!Strings.isNullOrEmpty(from)) {
            configDao.update(ConfigType.SMTP_FROM, from);
        }

        return Response.ok().build();
    }

    /**
     * Get the inbox configuration.
     *
     * @api {get} /app/config_inbox Get the inbox scanning configuration
     * @apiName GetAppConfigInbox
     * @apiGroup App
     * @apiSuccess {Boolean} enabled True if the inbox scanning is enabled
     * @apiSuccess {String} hostname IMAP hostname
     * @apiSuccess {String} port IMAP port
     * @apiSuccess {String} username IMAP username
     * @apiSuccess {String} password IMAP password
     * @apiSuccess {String} folder IMAP folder
     * @apiSuccess {String} tag Tag for created documents
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @GET
    @Path("config_inbox")
    public Response getConfigInbox() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        ConfigDao configDao = new ConfigDao();
        Boolean enabled = ConfigUtil.getConfigBooleanValue(ConfigType.INBOX_ENABLED);
        Boolean autoTags = ConfigUtil.getConfigBooleanValue(ConfigType.INBOX_AUTOMATIC_TAGS);
        Boolean deleteImported = ConfigUtil.getConfigBooleanValue(ConfigType.INBOX_DELETE_IMPORTED);
        Config hostnameConfig = configDao.getById(ConfigType.INBOX_HOSTNAME);
        Config portConfig = configDao.getById(ConfigType.INBOX_PORT);
        Boolean starttls = ConfigUtil.getConfigBooleanValue(ConfigType.INBOX_STARTTLS);
        Config usernameConfig = configDao.getById(ConfigType.INBOX_USERNAME);
        Config passwordConfig = configDao.getById(ConfigType.INBOX_PASSWORD);
        Config folderConfig = configDao.getById(ConfigType.INBOX_FOLDER);
        Config tagConfig = configDao.getById(ConfigType.INBOX_TAG);
        JsonObjectBuilder response = Json.createObjectBuilder();

        response.add("enabled", enabled);
        response.add("autoTagsEnabled", autoTags);
        response.add("deleteImported", deleteImported);
        if (hostnameConfig == null) {
            response.addNull("hostname");
        } else {
            response.add("hostname", hostnameConfig.getValue());
        }
        if (portConfig == null) {
            response.addNull("port");
        } else {
            response.add("port", Integer.valueOf(portConfig.getValue()));
        }
        response.add("starttls", starttls);
        if (usernameConfig == null) {
            response.addNull("username");
        } else {
            response.add("username", usernameConfig.getValue());
        }
        if (passwordConfig == null) {
            response.addNull("password");
        } else {
            response.add("password", passwordConfig.getValue());
        }
        if (folderConfig == null) {
            response.addNull("folder");
        } else {
            response.add("folder", folderConfig.getValue());
        }
        if (tagConfig == null) {
            response.addNull("tag");
        } else {
            response.add("tag", tagConfig.getValue());
        }

        // Informations about the last synchronization
        InboxService inboxService = AppContext.getInstance().getInboxService();
        JsonObjectBuilder lastSync = Json.createObjectBuilder();
        if (inboxService.getLastSyncDate() == null) {
            lastSync.addNull("date");
        } else {
            lastSync.add("date", inboxService.getLastSyncDate().getTime());
        }
        lastSync.add("error", JsonUtil.nullable(inboxService.getLastSyncError()));
        lastSync.add("count", inboxService.getLastSyncMessageCount());
        response.add("last_sync", lastSync);

        return Response.ok().entity(response.build()).build();
    }

    /**
     * Configure the inbox.
     *
     * @api {post} /app/config_inbox Configure the inbox scanning
     * @apiName PostAppConfigInbox
     * @apiGroup App
     * @apiParam {Boolean} enabled True if the inbox scanning is enabled
     * @apiParam {Boolean} autoTagsEnabled If true automatically add tags to document (prefixed by #)
     * @apiParam {Boolean} deleteImported If true delete message from mailbox after import
     * @apiParam {String} hostname IMAP hostname
     * @apiParam {Integer} port IMAP port
     * @apiParam {String} username IMAP username
     * @apiParam {String} password IMAP password
     * @apiParam {String} folder IMAP folder
     * @apiParam {String} tag Tag for created documents
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param enabled True if the inbox scanning is enabled
     * @param hostname IMAP hostname
     * @param portStr IMAP port
     * @param username IMAP username
     * @param password IMAP password
     * @param folder IMAP folder
     * @param tag Tag for created documents
     * @return Response
     */
    @POST
    @Path("config_inbox")
    public Response configInbox(@FormParam("enabled") Boolean enabled,
                                @FormParam("autoTagsEnabled") Boolean autoTagsEnabled,
                                @FormParam("deleteImported") Boolean deleteImported,
                                @FormParam("hostname") String hostname,
                                @FormParam("port") String portStr,
                                @FormParam("starttls") Boolean starttls,
                                @FormParam("username") String username,
                                @FormParam("password") String password,
                                @FormParam("folder") String folder,
                                @FormParam("tag") String tag) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        ValidationUtil.validateRequired(enabled, "enabled");
        ValidationUtil.validateRequired(autoTagsEnabled, "autoTagsEnabled");
        ValidationUtil.validateRequired(deleteImported, "deleteImported");
        if (!Strings.isNullOrEmpty(portStr)) {
            ValidationUtil.validateInteger(portStr, "port");
        }
        ValidationUtil.validateRequired(starttls, "starttls");

        // Just update the changed configuration
        ConfigDao configDao = new ConfigDao();
        configDao.update(ConfigType.INBOX_ENABLED, enabled.toString());
        configDao.update(ConfigType.INBOX_AUTOMATIC_TAGS, autoTagsEnabled.toString());
        configDao.update(ConfigType.INBOX_DELETE_IMPORTED, deleteImported.toString());
        if (!Strings.isNullOrEmpty(hostname)) {
            configDao.update(ConfigType.INBOX_HOSTNAME, hostname);
        }
        if (!Strings.isNullOrEmpty(portStr)) {
            configDao.update(ConfigType.INBOX_PORT, portStr);
        }
        configDao.update(ConfigType.INBOX_STARTTLS, starttls.toString());
        if (!Strings.isNullOrEmpty(username)) {
            configDao.update(ConfigType.INBOX_USERNAME, username);
        }
        if (!Strings.isNullOrEmpty(password)) {
            configDao.update(ConfigType.INBOX_PASSWORD, password);
        }
        if (!Strings.isNullOrEmpty(folder)) {
            configDao.update(ConfigType.INBOX_FOLDER, folder);
        }
        if (!Strings.isNullOrEmpty(tag)) {
            configDao.update(ConfigType.INBOX_TAG, tag);
        }

        return Response.ok().build();
    }

    /**
     * Test the inbox.
     *
     * @api {post} /app/test_inbox Test the inbox scanning
     * @apiName PostAppTestInbox
     * @apiGroup App
     * @apiSuccess {Number} Number of unread emails in the inbox
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @POST
    @Path("test_inbox")
    public Response testInbox() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        return Response.ok().entity(Json.createObjectBuilder()
                .add("count", AppContext.getInstance().getInboxService().testInbox())
                .build()).build();
    }

    /**
     * Retrieve the application logs.
     *
     * @api {get} /app/log Get application logs
     * @apiName GetAppLog
     * @apiGroup App
     * @apiParam {String="FATAL","ERROR","WARN","INFO","DEBUG"} level Minimum log level
     * @apiParam {String} tag Filter on this logger tag
     * @apiParam {String} message Filter on this message
     * @apiParam {Number} limit Total number of logs to return
     * @apiParam {Number} offset Start at this index
     * @apiSuccess {String} total Total number of logs
     * @apiSuccess {Object[]} logs List of logs
     * @apiSuccess {String} logs.date Date
     * @apiSuccess {String} logs.level Level
     * @apiSuccess {String} logs.tag Tag
     * @apiSuccess {String} logs.message Message
     * @apiError (client) ForbiddenError Access denied
     * @apiError (server) ServerError MEMORY appender not configured
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param minLevel Filter on logging level
     * @param tag Filter on logger name / tag
     * @param message Filter on message
     * @param limit Page limit
     * @param offset Page offset
     * @return Response
     */
    @GET
    @Path("log")
    public Response log(
            @QueryParam("level") String minLevel,
            @QueryParam("tag") String tag,
            @QueryParam("message") String message,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Get the memory appender
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getRootLogger();
        Appender appender = logger.getAppender("MEMORY");
        if (!(appender instanceof MemoryAppender)) {
            throw new ServerException("ServerError", "MEMORY appender not configured");
        }
        MemoryAppender memoryAppender = (MemoryAppender) appender;

        // Find the logs
        LogCriteria logCriteria = new LogCriteria()
                .setMinLevel(Level.toLevel(StringUtils.stripToNull(minLevel)))
                .setTag(StringUtils.stripToNull(tag))
                .setMessage(StringUtils.stripToNull(message));

        PaginatedList<LogEntry> paginatedList = PaginatedLists.create(limit, offset);
        memoryAppender.find(logCriteria, paginatedList);
        JsonArrayBuilder logs = Json.createArrayBuilder();
        for (LogEntry logEntry : paginatedList.getResultList()) {
            logs.add(Json.createObjectBuilder()
                    .add("date", logEntry.getTimestamp())
                    .add("level", logEntry.getLevel().toString())
                    .add("tag", logEntry.getTag())
                    .add("message", logEntry.getMessage()));
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("total", paginatedList.getResultCount())
                .add("logs", logs);

        return Response.ok().entity(response.build()).build();
    }

    /**
     * Destroy and rebuild the search index.
     *
     * @api {post} /app/batch/reindex Rebuild the search index
     * @apiName PostAppBatchReindex
     * @apiGroup App
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (server) IndexingError Error rebuilding the index
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @POST
    @Path("batch/reindex")
    public Response batchReindex() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        RebuildIndexAsyncEvent rebuildIndexAsyncEvent = new RebuildIndexAsyncEvent();
        ThreadLocalContext.get().addAsyncEvent(rebuildIndexAsyncEvent);

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Clean storage.
     *
     * @api {post} /app/batch/clean_storage Clean the file and DB storage
     * @apiName PostAppBatchCleanStorage
     * @apiGroup App
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (server) FileError Error deleting orphan files
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @POST
    @Path("batch/clean_storage")
    public Response batchCleanStorage() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Get all files
        FileDao fileDao = new FileDao();
        List<File> fileList = fileDao.findAll(0, Integer.MAX_VALUE);
        Map<String, File> fileMap = new HashMap<>();
        for (File file : fileList) {
            fileMap.put(file.getId(), file);
        }
        log.info("Checking {} files", fileMap.size());

        // Check if each stored file is valid
        try (DirectoryStream<java.nio.file.Path> storedFileList = Files.newDirectoryStream(DirectoryUtil.getStorageDirectory())) {
            for (java.nio.file.Path storedFile : storedFileList) {
                String fileName = storedFile.getFileName().toString();
                String[] fileNameArray = fileName.split("_");
                if (!fileMap.containsKey(fileNameArray[0])) {
                    log.info("Deleting orphan files at this location: {}", storedFile);
                    Files.delete(storedFile);
                }
            }
        } catch (IOException e) {
            throw new ServerException("FileError", "Error deleting orphan files", e);
        }

        // Hard delete orphan audit logs
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("delete from T_AUDIT_LOG al where al.LOG_ID_C in (select al.LOG_ID_C from T_AUDIT_LOG al ");
        sb.append(" left join T_DOCUMENT d on d.DOC_ID_C = al.LOG_IDENTITY_C and d.DOC_DELETEDATE_D is null ");
        sb.append(" left join T_ACL a on a.ACL_ID_C = al.LOG_IDENTITY_C and a.ACL_DELETEDATE_D is null ");
        sb.append(" left join T_COMMENT c on c.COM_ID_C = al.LOG_IDENTITY_C and c.COM_DELETEDATE_D is null ");
        sb.append(" left join T_FILE f on f.FIL_ID_C = al.LOG_IDENTITY_C and f.FIL_DELETEDATE_D is null ");
        sb.append(" left join T_TAG t on t.TAG_ID_C = al.LOG_IDENTITY_C and t.TAG_DELETEDATE_D is null ");
        sb.append(" left join T_USER u on u.USE_ID_C = al.LOG_IDENTITY_C and u.USE_DELETEDATE_D is null ");
        sb.append(" left join T_GROUP g on g.GRP_ID_C = al.LOG_IDENTITY_C and g.GRP_DELETEDATE_D is null ");
        sb.append(" where d.DOC_ID_C is null and a.ACL_ID_C is null and c.COM_ID_C is null and f.FIL_ID_C is null and t.TAG_ID_C is null and u.USE_ID_C is null and g.GRP_ID_C is null)");
        Query q = em.createNativeQuery(sb.toString());
        log.info("Deleting {} orphan audit logs", q.executeUpdate());

        // Soft delete orphan ACLs
        sb = new StringBuilder("update T_ACL a set ACL_DELETEDATE_D = :dateNow where a.ACL_ID_C in (select a.ACL_ID_C from T_ACL a ");
        sb.append(" left join T_SHARE s on s.SHA_ID_C = a.ACL_TARGETID_C ");
        sb.append(" left join T_USER u on u.USE_ID_C = a.ACL_TARGETID_C ");
        sb.append(" left join T_GROUP g on g.GRP_ID_C = a.ACL_TARGETID_C ");
        sb.append(" left join T_DOCUMENT d on d.DOC_ID_C = a.ACL_SOURCEID_C ");
        sb.append(" left join T_TAG t on t.TAG_ID_C = a.ACL_SOURCEID_C ");
        sb.append(" where s.SHA_ID_C is null and u.USE_ID_C is null and g.GRP_ID_C is null or d.DOC_ID_C is null and t.TAG_ID_C is null)");
        q = em.createNativeQuery(sb.toString());
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan ACLs", q.executeUpdate());

        // Soft delete orphan comments
        q = em.createNativeQuery("update T_COMMENT set COM_DELETEDATE_D = :dateNow where COM_ID_C in (select c.COM_ID_C from T_COMMENT c left join T_DOCUMENT d on d.DOC_ID_C = c.COM_IDDOC_C and d.DOC_DELETEDATE_D is null where d.DOC_ID_C is null)");
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan comments", q.executeUpdate());

        // Soft delete orphan document tag links
        q = em.createNativeQuery("update T_DOCUMENT_TAG set DOT_DELETEDATE_D = :dateNow where DOT_ID_C in (select dt.DOT_ID_C from T_DOCUMENT_TAG dt left join T_DOCUMENT d on dt.DOT_IDDOCUMENT_C = d.DOC_ID_C and d.DOC_DELETEDATE_D is null left join T_TAG t on t.TAG_ID_C = dt.DOT_IDTAG_C and t.TAG_DELETEDATE_D is null where d.DOC_ID_C is null or t.TAG_ID_C is null)");
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan document tag links", q.executeUpdate());

        // Soft delete orphan shares
        q = em.createNativeQuery("update T_SHARE set SHA_DELETEDATE_D = :dateNow where SHA_ID_C in (select s.SHA_ID_C from T_SHARE s left join T_ACL a on a.ACL_TARGETID_C = s.SHA_ID_C and a.ACL_DELETEDATE_D is null where a.ACL_ID_C is null)");
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan shares", q.executeUpdate());

        // Soft delete orphan tags
        q = em.createNativeQuery("update T_TAG set TAG_DELETEDATE_D = :dateNow where TAG_ID_C in (select t.TAG_ID_C from T_TAG t left join T_USER u on u.USE_ID_C = t.TAG_IDUSER_C and u.USE_DELETEDATE_D is null where u.USE_ID_C is null)");
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan tags", q.executeUpdate());

        // Soft delete orphan documents
        q = em.createNativeQuery("update T_DOCUMENT set DOC_DELETEDATE_D = :dateNow where DOC_ID_C in (select d.DOC_ID_C from T_DOCUMENT d left join T_USER u on u.USE_ID_C = d.DOC_IDUSER_C and u.USE_DELETEDATE_D is null where u.USE_ID_C is null)");
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan documents", q.executeUpdate());

        // Soft delete orphan files
        q = em.createNativeQuery("update T_FILE set FIL_DELETEDATE_D = :dateNow where FIL_ID_C in (select f.FIL_ID_C from T_FILE f left join T_USER u on u.USE_ID_C = f.FIL_IDUSER_C and u.USE_DELETEDATE_D is null where u.USE_ID_C is null)");
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan files", q.executeUpdate());

        // Hard delete softly deleted data
        log.info("Deleting {} soft deleted document tag links", em.createQuery("delete DocumentTag where deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted ACLs", em.createQuery("delete Acl where deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted shares", em.createQuery("delete Share where deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted tags", em.createQuery("delete Tag where deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted comments", em.createQuery("delete Comment where deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted files", em.createQuery("delete File where deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted documents", em.createQuery("delete Document where deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted users", em.createQuery("delete User where deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted groups", em.createQuery("delete Group where deleteDate is not null").executeUpdate());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Get the LDAP authentication configuration.
     *
     * @api {get} /app/config_ldap Get the LDAP authentication configuration
     * @apiName GetAppConfigLdap
     * @apiGroup App
     * @apiSuccess {Boolean} enabled LDAP authentication enabled
     * @apiSuccess {String} host LDAP server host
     * @apiSuccess {Integer} port LDAP server port
     * @apiSuccess {String} admin_dn Admin DN
     * @apiSuccess {String} admin_password Admin password
     * @apiSuccess {String} base_dn Base DN
     * @apiSuccess {String} filter LDAP filter
     * @apiSuccess {String} default_email LDAP default email
     * @apiSuccess {Integer} default_storage LDAP default storage
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.9.0
     *
     * @return Response
     */
    @GET
    @Path("config_ldap")
    public Response getConfigLdap() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        ConfigDao configDao = new ConfigDao();
        Config enabled = configDao.getById(ConfigType.LDAP_ENABLED);

        JsonObjectBuilder response = Json.createObjectBuilder();
        if (enabled != null && Boolean.parseBoolean(enabled.getValue())) {
            // LDAP enabled
            response.add("enabled", true)
                    .add("host", ConfigUtil.getConfigStringValue(ConfigType.LDAP_HOST))
                    .add("port", ConfigUtil.getConfigIntegerValue(ConfigType.LDAP_PORT))
                    .add("usessl", ConfigUtil.getConfigBooleanValue(ConfigType.LDAP_USESSL))
                    .add("admin_dn", ConfigUtil.getConfigStringValue(ConfigType.LDAP_ADMIN_DN))
                    .add("admin_password", ConfigUtil.getConfigStringValue(ConfigType.LDAP_ADMIN_PASSWORD))
                    .add("base_dn", ConfigUtil.getConfigStringValue(ConfigType.LDAP_BASE_DN))
                    .add("filter", ConfigUtil.getConfigStringValue(ConfigType.LDAP_FILTER))
                    .add("default_email", ConfigUtil.getConfigStringValue(ConfigType.LDAP_DEFAULT_EMAIL))
                    .add("default_storage", ConfigUtil.getConfigLongValue(ConfigType.LDAP_DEFAULT_STORAGE));
        } else {
            // LDAP disabled
            response.add("enabled", false);
        }

        return Response.ok().entity(response.build()).build();
    }

    /**
     * Configure the LDAP authentication.
     *
     * @api {post} /app/config_ldap Configure the LDAP authentication
     * @apiName PostAppConfigLdap
     * @apiGroup App
     * @apiParam {Boolean} enabled LDAP authentication enabled
     * @apiParam {String} host LDAP server host
     * @apiParam {Integer} port LDAP server port
     * @apiParam {Boolean} use SSL (ldaps)
     * @apiParam {String} admin_dn Admin DN
     * @apiParam {String} admin_password Admin password
     * @apiParam {String} base_dn Base DN
     * @apiParam {String} filter LDAP filter
     * @apiParam {String} default_email LDAP default email
     * @apiParam {Integer} default_storage LDAP default storage
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiPermission admin
     * @apiVersion 1.9.0
     *
     * @param enabled LDAP authentication enabled
     * @param host LDAP server host
     * @param portStr LDAP server port
     * @param usessl LDAP use SSL (ldaps)
     * @param adminDn Admin DN
     * @param adminPassword Admin password
     * @param baseDn Base DN
     * @param filter LDAP filter
     * @param defaultEmail LDAP default email
     * @param defaultStorageStr LDAP default storage
     * @return Response
     */
    @POST
    @Path("config_ldap")
    public Response configLdap(@FormParam("enabled") Boolean enabled,
                               @FormParam("host") String host,
                               @FormParam("port") String portStr,
                               @FormParam("usessl") Boolean usessl,
                               @FormParam("admin_dn") String adminDn,
                               @FormParam("admin_password") String adminPassword,
                               @FormParam("base_dn") String baseDn,
                               @FormParam("filter") String filter,
                               @FormParam("default_email") String defaultEmail,
                               @FormParam("default_storage") String defaultStorageStr) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        ConfigDao configDao = new ConfigDao();

        if (enabled != null && enabled) {
            // LDAP enabled, validate everything
            ValidationUtil.validateLength(host, "host", 1, 250);
            ValidationUtil.validateInteger(portStr, "port");
            ValidationUtil.validateLength(adminDn, "admin_dn", 1, 250);
            ValidationUtil.validateLength(adminPassword, "admin_password", 1, 250);
            ValidationUtil.validateLength(baseDn, "base_dn", 1, 250);
            ValidationUtil.validateLength(filter, "filter", 1, 250);
            if (!filter.contains("USERNAME")) {
                throw new ClientException("ValidationError", "'filter' must contains 'USERNAME'");
            }
            ValidationUtil.validateLength(defaultEmail, "default_email", 1, 250);
            ValidationUtil.validateLong(defaultStorageStr, "default_storage");
            configDao.update(ConfigType.LDAP_ENABLED, Boolean.TRUE.toString());
            configDao.update(ConfigType.LDAP_HOST, host);
            configDao.update(ConfigType.LDAP_PORT, portStr);
            configDao.update(ConfigType.LDAP_USESSL, usessl.toString());
            configDao.update(ConfigType.LDAP_ADMIN_DN, adminDn);
            configDao.update(ConfigType.LDAP_ADMIN_PASSWORD, adminPassword);
            configDao.update(ConfigType.LDAP_BASE_DN, baseDn);
            configDao.update(ConfigType.LDAP_FILTER, filter);
            configDao.update(ConfigType.LDAP_DEFAULT_EMAIL, defaultEmail);
            configDao.update(ConfigType.LDAP_DEFAULT_STORAGE, defaultStorageStr);
        } else {
            // LDAP disabled
            configDao.update(ConfigType.LDAP_ENABLED, Boolean.FALSE.toString());
        }

        return Response.ok().build();
    }
}
