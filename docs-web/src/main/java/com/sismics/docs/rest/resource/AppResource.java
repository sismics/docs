package com.sismics.docs.rest.resource;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.jpa.criteria.DocumentCriteria;
import com.sismics.docs.core.dao.jpa.dto.DocumentDto;
import com.sismics.docs.core.event.ExtractFileAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.ConfigUtil;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
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
     * Return the information about the application.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response info() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        ResourceBundle configBundle = ConfigUtil.getConfigBundle();
        String currentVersion = configBundle.getString("api.current_version");
        String minVersion = configBundle.getString("api.min_version");

        JSONObject response = new JSONObject();
        
        // Specific data
        DocumentDao documentDao = new DocumentDao();
        PaginatedList<DocumentDto> paginatedList = PaginatedLists.create(1, 0);
        SortCriteria sortCriteria = new SortCriteria(0, true);
        DocumentCriteria documentCriteria = new DocumentCriteria();
        documentCriteria.setUserId(principal.getId());
        try {
            documentDao.findByCriteria(paginatedList, documentCriteria, sortCriteria);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in documents", e);
        }
        response.put("document_count", paginatedList.getResultCount());
        
        // General data
        response.put("current_version", currentVersion.replace("-SNAPSHOT", ""));
        response.put("min_version", minVersion);
        response.put("total_memory", Runtime.getRuntime().totalMemory());
        response.put("free_memory", Runtime.getRuntime().freeMemory());
        
        return Response.ok().entity(response).build();
    }
    
    /**
     * Retrieve the application logs.
     * 
     * @param level Filter on logging level
     * @param tag Filter on logger name / tag
     * @param message Filter on message
     * @param limit Page limit
     * @param offset Page offset
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("log")
    @Produces(MediaType.APPLICATION_JSON)
    public Response log(
            @QueryParam("level") String level,
            @QueryParam("tag") String tag,
            @QueryParam("message") String message,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Get the memory appender
        Logger logger = Logger.getRootLogger();
        Appender appender = logger.getAppender("MEMORY");
        if (appender == null || !(appender instanceof MemoryAppender)) {
            throw new ServerException("ServerError", "MEMORY appender not configured");
        }
        MemoryAppender memoryAppender = (MemoryAppender) appender;
        
        // Find the logs
        LogCriteria logCriteria = new LogCriteria();
        logCriteria.setLevel(StringUtils.stripToNull(level));
        logCriteria.setTag(StringUtils.stripToNull(tag));
        logCriteria.setMessage(StringUtils.stripToNull(message));
        
        PaginatedList<LogEntry> paginatedList = PaginatedLists.create(limit, offset);
        memoryAppender.find(logCriteria, paginatedList);
        JSONObject response = new JSONObject();
        List<JSONObject> logs = new ArrayList<>();
        for (LogEntry logEntry : paginatedList.getResultList()) {
            JSONObject log = new JSONObject();
            log.put("date", logEntry.getTimestamp());
            log.put("level", logEntry.getLevel());
            log.put("tag", logEntry.getTag());
            log.put("message", logEntry.getMessage());
            logs.add(log);
        }
        response.put("total", paginatedList.getResultCount());
        response.put("logs", logs);
        
        return Response.ok().entity(response).build();
    }
    
    /**
     * OCR-ize all files again.
     * 
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("batch/ocr")
    @Produces(MediaType.APPLICATION_JSON)
    public Response batchOcr() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Raise a OCR file event
        AppContext.getInstance().getAsyncEventBus().post(new ExtractFileAsyncEvent());
        
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    /**
     * Destroy and rebuild Lucene index.
     * 
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("batch/reindex")
    @Produces(MediaType.APPLICATION_JSON)
    public Response batchReindex() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        try {
            AppContext.getInstance().getIndexingService().rebuildIndex();
        } catch (Exception e) {
            throw new ServerException("IndexingError", "Error rebuilding index", e);
        }
        
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    /**
     * Destroy and rebuild Lucene index.
     * 
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("batch/clean_storage")
    @Produces(MediaType.APPLICATION_JSON)
    public Response batchCleanStorage() throws JSONException {
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
        
        // Check if each stored file is valid
        java.io.File[] storedFileList = DirectoryUtil.getStorageDirectory().listFiles();
        for (java.io.File storedFile : storedFileList) {
            String fileName = storedFile.getName();
            String[] fileNameArray = fileName.split("_");
            if (!fileMap.containsKey(fileNameArray[0])) {
                storedFile.delete();
            }
        }
        
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    /**
     * Regenerate file variations.
     * 
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("batch/file_variations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response batchFileVariations() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Get all files
        FileDao fileDao = new FileDao();
        List<File> fileList = fileDao.findAll();
        
        // Generate variations for each file
        for (File file : fileList) {
            java.io.File originalFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId()).toFile();
            try {
                FileUtil.saveVariations(file, originalFile);
            } catch (IOException e) {
                throw new ServerException("FileError", "Error generating file variations", e);
            }
        }
        
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
