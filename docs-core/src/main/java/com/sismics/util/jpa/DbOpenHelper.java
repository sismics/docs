package com.sismics.util.jpa;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.sismics.docs.core.util.ConfigUtil;
import com.sismics.util.ResourceUtil;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A helper to update the database incrementally.
 *
 * @author jtremeaux
 */
abstract class DbOpenHelper {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DbOpenHelper.class);

    private final JdbcConnectionAccess jdbcConnectionAccess;

    private final List<Exception> exceptions = new ArrayList<>();

    private Formatter formatter;

    private Statement stmt;

    DbOpenHelper(ServiceRegistry serviceRegistry) throws HibernateException {
        final JdbcServices jdbcServices = serviceRegistry.getService(JdbcServices.class);
        SqlStatementLogger sqlStatementLogger = jdbcServices.getSqlStatementLogger();
        jdbcConnectionAccess = jdbcServices.getBootstrapJdbcConnectionAccess();
        formatter = (sqlStatementLogger.isFormat() ? FormatStyle.DDL : FormatStyle.NONE).getFormatter();
    }

    public void open() {
        log.info("Opening database and executing incremental updates");

        Connection connection = null;

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
                if (DialectUtil.isObjectNotFound(e.getMessage())) {
                    log.info("Unable to get database version: Table T_CONFIG not found");
                } else {
                    log.error("Unable to get database version", e);
                }
            } finally {
                connection.commit();
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
                connection.commit();
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                jdbcConnectionAccess.releaseConnection(connection);
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
     * @throws Exception e
     */
    void executeAllScript(final int version) throws Exception {
        List<String> fileNameList = ResourceUtil.list(getClass(), "/db/update/", (dir, name) -> {
            String versionString = String.format("%03d", version);
            return name.matches("dbupdate-" + versionString + "-\\d+\\.sql");
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
     * @throws IOException e
     */
    private void executeScript(InputStream inputScript) throws IOException {
        List<String> lines = CharStreams.readLines(new InputStreamReader(inputScript));

        for (String sql : lines) {
            if (Strings.isNullOrEmpty(sql) || sql.startsWith("--")) {
                continue;
            }

            String transformed = DialectUtil.transform(sql);
            if (transformed != null) {
                String formatted = formatter.format(transformed);
                try {
                    log.debug(formatted);
                    stmt.executeUpdate(formatted);
                } catch (SQLException e) {
                    exceptions.add(e);
                    if (log.isErrorEnabled()) {
                        log.error("Error executing SQL statement: {}", sql);
                        log.error(e.getMessage());
                    }
                }
            }
        }
    }

    public abstract void onCreate() throws Exception;

    public abstract void onUpgrade(int oldVersion, int newVersion) throws Exception;

    /**
     * Returns a List of all Exceptions which occurred during the export.
     *
     * @return A List containing the Exceptions occurred during the export
     */
    public List<?> getExceptions() {
        return exceptions;
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
