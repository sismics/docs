package com.sismics.util.jpa;

/**
 * Dialect utilities.
 *
 * @author jtremeaux
 */
public class DialectUtil {
    /**
     * Checks if the error from the drivers relates to an object not found.
     *
     * @param message Error message
     * @return Object not found
     */
    public static boolean isObjectNotFound(String message) {
        return EMF.isDriverH2() && message.contains("not found") ||
                EMF.isDriverPostgresql() && message.contains("does not exist");
    }


    /**
     * Transform SQL dialect to current dialect.
     *
     * @param sql SQL to transform
     * @return Transformed SQL
     */
    public static String transform(String sql) {
        if (sql.startsWith("!PGSQL!")) {
            return EMF.isDriverH2() ? null : sql.substring(7);
        }
        if (sql.startsWith("!H2!")) {
            return EMF.isDriverPostgresql() ? null : sql.substring(4);
        }

        if (EMF.isDriverPostgresql()) {
            sql = transformToPostgresql(sql);
        }
        return sql;
    }

    /**
     * Transform SQL from HSQLDB dialect to current dialect.
     *
     * @param sql SQL to transform
     * @return Transformed SQL
     */
    public static String transformToPostgresql(String sql) {
        sql = sql.replaceAll("(cached|memory) table", "table");
        sql = sql.replaceAll("datetime", "timestamp");
        sql = sql.replaceAll("longvarchar", "text");
        sql = sql.replaceAll("bit default 1", "bool default true");
        sql = sql.replaceAll("bit default 0", "bool default false");
        sql = sql.replaceAll("bit not null default 1", "bool not null default true");
        sql = sql.replaceAll("bit not null default 0", "bool not null default false");
        sql = sql.replaceAll("bit not null", "bool not null");
        return sql;
    }
}
