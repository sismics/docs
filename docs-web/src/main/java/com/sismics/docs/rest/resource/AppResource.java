package com.sismics.docs.rest.resource;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.ConfigUtil;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.log4j.LogCriteria;
import com.sismics.util.log4j.LogEntry;
import com.sismics.util.log4j.MemoryAppender;

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
     * Return the information about the application.
     * 
     * @return Response
     */
    @GET
    public Response info() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        ResourceBundle configBundle = ConfigUtil.getConfigBundle();
        String currentVersion = configBundle.getString("api.current_version");
        String minVersion = configBundle.getString("api.min_version");

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("current_version", currentVersion.replace("-SNAPSHOT", ""))
                .add("min_version", minVersion)
                .add("total_memory", Runtime.getRuntime().totalMemory())
                .add("free_memory", Runtime.getRuntime().freeMemory());
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Retrieve the application logs.
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
        // Get the memory appender
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getRootLogger();
        Appender appender = logger.getAppender("MEMORY");
        if (appender == null || !(appender instanceof MemoryAppender)) {
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
     * Destroy and rebuild Lucene index.
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
        
        try {
            AppContext.getInstance().getIndexingService().rebuildIndex();
        } catch (Exception e) {
            throw new ServerException("IndexingError", "Error rebuilding index", e);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Clean storage.
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
        List<File> fileList = fileDao.findAll();
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
        sb.append(" where d.DOC_ID_C is null and a.ACL_ID_C is null and c.COM_ID_C is null and f.FIL_ID_C is null and t.TAG_ID_C is null and u.USE_ID_C is null)");
        Query q = em.createNativeQuery(sb.toString());
        log.info("Deleting {} orphan audit logs", q.executeUpdate());
        
        // Soft delete orphan ACLs
        sb = new StringBuilder("update T_ACL a set ACL_DELETEDATE_D = :dateNow where a.ACL_ID_C in (select a.ACL_ID_C from T_ACL a ");
        sb.append(" left join T_SHARE s on s.SHA_ID_C = a.ACL_TARGETID_C ");
        sb.append(" left join T_USER u on u.USE_ID_C = a.ACL_TARGETID_C ");
        sb.append(" left join T_DOCUMENT d on d.DOC_ID_C = a.ACL_SOURCEID_C ");
        sb.append(" where s.SHA_ID_C is null and u.USE_ID_C is null or d.DOC_ID_C is null)");
        q = em.createNativeQuery(sb.toString());
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan ACLs", q.executeUpdate());
        
        // Soft delete orphan comments
        q = em.createNativeQuery("update T_COMMENT c set c.COM_DELETEDATE_D = :dateNow where c.COM_ID_C in (select c.COM_ID_C from T_COMMENT c left join T_DOCUMENT d on d.DOC_ID_C = c.COM_IDDOC_C and d.DOC_DELETEDATE_D is null where d.DOC_ID_C is null)");
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan comments", q.executeUpdate());
        
        // Soft delete orphan document tag links
        q = em.createNativeQuery("update T_DOCUMENT_TAG dt set dt.DOT_DELETEDATE_D = :dateNow where dt.DOT_ID_C in (select dt.DOT_ID_C from T_DOCUMENT_TAG dt left join T_DOCUMENT d on dt.DOT_IDDOCUMENT_C = d.DOC_ID_C and d.DOC_DELETEDATE_D is null left join T_TAG t on t.TAG_ID_C = dt.DOT_IDTAG_C and t.TAG_DELETEDATE_D is null where d.DOC_ID_C is null or t.TAG_ID_C is null)");
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan document tag links", q.executeUpdate());
        
        // Soft delete orphan shares
        q = em.createNativeQuery("update T_SHARE s set s.SHA_DELETEDATE_D = :dateNow where s.SHA_ID_C in (select s.SHA_ID_C from T_SHARE s left join T_ACL a on a.ACL_TARGETID_C = s.SHA_ID_C and a.ACL_DELETEDATE_D is null where a.ACL_ID_C is null)");
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan shares", q.executeUpdate());
        
        // Soft delete orphan tags
        q = em.createNativeQuery("update T_TAG t set t.TAG_DELETEDATE_D = :dateNow where t.TAG_ID_C in (select t.TAG_ID_C from T_TAG t left join T_USER u on u.USE_ID_C = t.TAG_IDUSER_C and u.USE_DELETEDATE_D is null where u.USE_ID_C is null)");
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan tags", q.executeUpdate());
        
        // Soft delete orphan documents
        q = em.createNativeQuery("update T_DOCUMENT d set d.DOC_DELETEDATE_D = :dateNow where d.DOC_ID_C in (select d.DOC_ID_C from T_DOCUMENT d left join T_USER u on u.USE_ID_C = d.DOC_IDUSER_C and u.USE_DELETEDATE_D is null where u.USE_ID_C is null)");
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan documents", q.executeUpdate());
        
        // Soft delete orphan files
        q = em.createNativeQuery("update T_FILE f set f.FIL_DELETEDATE_D = :dateNow where f.FIL_ID_C in (select f.FIL_ID_C from T_FILE f left join T_USER u on u.USE_ID_C = f.FIL_IDUSER_C and u.USE_DELETEDATE_D is null where u.USE_ID_C is null)");
        q.setParameter("dateNow", new Date());
        log.info("Deleting {} orphan files", q.executeUpdate());
        
        // Hard delete softly deleted data
        log.info("Deleting {} soft deleted document tag links", em.createQuery("delete DocumentTag dt where dt.deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted ACLs", em.createQuery("delete Acl a where a.deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted shares", em.createQuery("delete Share s where s.deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted tags", em.createQuery("delete Tag t where t.deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted comments", em.createQuery("delete Comment c where c.deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted files", em.createQuery("delete File f where f.deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted documents", em.createQuery("delete Document d where d.deleteDate is not null").executeUpdate());
        log.info("Deleting {} soft deleted users", em.createQuery("delete User u where u.deleteDate is not null").executeUpdate());
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Recompute the quota for each user.
     * 
     * @return Response
     */
    @POST
    @Path("batch/recompute_quota")
    public Response batchRecomputeQuota() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Get all files
        FileDao fileDao = new FileDao();
        List<File> fileList = fileDao.findAll();
        
        // Count each file for the corresponding user quota
        UserDao userDao = new UserDao();
        Map<String, User> userMap = new HashMap<>();
        for (File file : fileList) {
            java.nio.file.Path storedFile = DirectoryUtil.getStorageDirectory().resolve(file.getId());
            User user = null;
            if (userMap.containsKey(file.getUserId())) {
                user = userMap.get(file.getUserId());
            } else {
                user = userDao.getById(file.getUserId());
                user.setStorageCurrent(0l);
                userMap.put(user.getId(), user);
            }
            
            try {
                user.setStorageCurrent(user.getStorageCurrent() + Files.size(storedFile));
            } catch (IOException e) {
                throw new ServerException("MissingFile", "File does not exist", e);
            }
        }
        
        // Save all users
        for (User user : userMap.values()) {
            if (user.getDeleteDate() == null) {
                userDao.updateQuota(user);
            }
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
