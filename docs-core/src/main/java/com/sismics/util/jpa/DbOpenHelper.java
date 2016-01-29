package com.sismics.util.jpa;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.sismics.docs.core.util.ConfigUtil;
import com.sismics.util.ResourceUtil;

/**
 * A helper to update the database incrementally.
 *
 * @author jtremeaux
 */
public abstract class DbOpenHelper {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DbOpenHelper.class);

    private final SqlStatementLogger sqlStatementLogger;
    
    private final JdbcConnectionAccess jdbcConnectionAccess;
    
    private final List<Exception> exceptions = new ArrayList<Exception>();

    private Formatter formatter;

    private boolean haltOnError;
    
    private Statement stmt;

    public DbOpenHelper(ServiceRegistry serviceRegistry) throws HibernateException {
        final JdbcServices jdbcServices = serviceRegistry.getService(JdbcServices.class);
        sqlStatementLogger = jdbcServices.getSqlStatementLogger();
        jdbcConnectionAccess = jdbcServices.getBootstrapJdbcConnectionAccess();
        formatter = (sqlStatementLogger.isFormat() ? FormatStyle.DDL : FormatStyle.NONE).getFormatter();
    }

    public void open() {
        log.info("Opening database and executing incremental updates");

        Connection connection = null;
        Writer outputFileWriter = null;

        exceptions.clear();

        try {
            try {
                connection = jdbcConnectionAccess.obtainConnection();
            } catch (SQLException sqle) {
                exceptions.add(sqle);
                log.error("Unable to get database metadata", sqle);
                throw sqle;
            }

            // Check if database is already created
            Integer oldVersion = null;
            try {
                stmt = connection.createStatement();
                ResultSet result = stmt.executeQuery("select c.CFG_VALUE_C from T_CONFIG c where c.CFG_ID_C='DB_VERSION'");
                if (result.next()) {
                    String oldVersionStr = result.getString(1);
                    oldVersion = Integer.parseInt(oldVersionStr);
                }
            } catch (Exception e) {
                if (e.getMessage().contains("object not found")) {
                    log.info("Unable to get database version: Table T_CONFIG not found");
                } else {
                    log.error("Unable to get database version", e);
                }
            } finally {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
            }

            stmt = connection.createStatement();
            if (oldVersion == null) {
                // Execute creation script
                log.info("Executing initial schema creation script");
                onCreate();
                oldVersion = 0;
            }
            
            // Execute update script
            ResourceBundle configBundle = ConfigUtil.getConfigBundle();
            Integer currentVersion = Integer.parseInt(configBundle.getString("db.version"));
            log.info(MessageFormat.format("Found database version {0}, new version is {1}, executing database incremental update scripts", oldVersion, currentVersion));
            onUpgrade(oldVersion, currentVersion);
            log.info("Database upgrade complete");
        } catch (Exception e) {
            exceptions.add(e);
            log.error("Unable to complete schema update", e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                jdbcConnectionAccess.releaseConnection(connection);
            } catch (Exception e) {
                exceptions.add(e);
                log.error("Unable to close connection", e);
            }
            try {
                if (outputFileWriter != null) {
                    outputFileWriter.close();
                }
            } catch (Exception e) {
                exceptions.add(e);
                log.error("Unable to close connection", e);
            }
        }
    }

    /**
     * Execute all upgrade scripts in ascending order for a given version.
     * 
     * @param version Version number
     * @throws Exception
     */
    protected void executeAllScript(final int version) throws Exception {
        List<String> fileNameList = ResourceUtil.list(getClass(), "/db/update/", new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String versionString = String.format("%03d", version);
                return name.matches("dbupdate-" + versionString + "-\\d+\\.sql");
            }
        });
        Collections.sort(fileNameList);
        
        for (String fileName : fileNameList) {
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format("Executing script: {0}", fileName));
            }
            InputStream is = getClass().getResourceAsStream("/db/update/" + fileName);
            executeScript(is);
        }
    }
    
    /**
     * Execute a SQL script. All statements must be one line only.
     * 
     * @param inputScript Script to execute
     * @throws IOException
     * @throws SQLException
     */
    protected void executeScript(InputStream inputScript) throws IOException, SQLException {
        List<String> lines = CharStreams.readLines(new InputStreamReader(inputScript));
        
        for (String sql : lines) {
            if (Strings.isNullOrEmpty(sql) || sql.startsWith("--")) {
                continue;
            }
            
            String formatted = formatter.format(sql);
            try {
                log.debug(formatted);
                stmt.executeUpdate(formatted);
            } catch (SQLException e) {
                if (haltOnError) {
                    if (stmt != null) {
                        stmt.close();
                        stmt = null;
                    }
                    throw new JDBCException("Error during script execution", e);
                }
                exceptions.add(e);
                if (log.isErrorEnabled()) {
                    log.error("Error executing SQL statement: {}", sql);
                    log.error(e.getMessage());
                }
            }
        }
    }

    public abstract void onCreate() throws Exception;
    
    public abstract void onUpgrade(int oldVersion, int newVersion) throws Exception;
    
    /**
     * Returns a List of all Exceptions which occured during the export.
     *
     * @return A List containig the Exceptions occured during the export
     */
    public List<?> getExceptions() {
        return exceptions;
    }

    public void setHaltOnError(boolean haltOnError) {
        this.haltOnError = haltOnError;
    }

    /**
     * Format the output SQL statements.
     * 
     * @param format True to format
     */
    public void setFormat(boolean format) {
        this.formatter = (format ? FormatStyle.DDL : FormatStyle.NONE).getFormatter();
    }
}
