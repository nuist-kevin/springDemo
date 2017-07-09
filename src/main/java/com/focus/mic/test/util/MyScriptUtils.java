package com.focus.mic.test.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class MyScriptUtils extends ScriptUtils {

    private static final Log logger = LogFactory.getLog(MyScriptUtils.class);


    public static void executeSqlScript(Connection connection, EncodedResource resource, boolean continueOnError,
                                        boolean ignoreFailedDrops, String commentPrefix, String separator, String blockCommentStartDelimiter,
                                        String blockCommentEndDelimiter) throws ScriptException {

        try {
            if (logger.isInfoEnabled()) {
                logger.info("Executing SQL script from " + resource);
            }
            long startTime = System.currentTimeMillis();

            String script;
            LineNumberReader lnr = new LineNumberReader(resource.getReader());

            try {
                script = readScript(lnr, commentPrefix, separator);
            }
            catch (IOException ex) {
                throw new CannotReadScriptException(resource, ex);
            }

            if (separator == null) {
                separator = DEFAULT_STATEMENT_SEPARATOR;
            }
            if (!EOF_STATEMENT_SEPARATOR.equals(separator) && !containsSqlScriptDelimiters(script, separator)) {
                separator = FALLBACK_STATEMENT_SEPARATOR;
            }

            List<String> statements = new LinkedList<String>();
            splitSqlScript(resource, script, separator, commentPrefix, blockCommentStartDelimiter,
                    blockCommentEndDelimiter, statements);
            List<String> statementsWithProcedures = new LinkedList<String>();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < statements.size(); i++) {
                String statement = statements.get(i);
                if (statement.toLowerCase().startsWith("Create procedure".toLowerCase())) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(statement).append(";");
                } else {
                    if (stringBuilder.toString().equals("")) {
                        statementsWithProcedures.add(statement);
                    } else {
                        stringBuilder.append(statement).append(";");
                    }
                }
                if (statement.toLowerCase().endsWith("end")) {
                    statementsWithProcedures.add(stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                }

            }


            int stmtNumber = 0;
            Statement stmt = connection.createStatement();
            try {
                for (String statement : statementsWithProcedures) {
                    stmtNumber++;
                    try {
                        stmt.execute(statement);
                        int rowsAffected = stmt.getUpdateCount();
                        if (logger.isDebugEnabled()) {
                            logger.debug(rowsAffected + " returned as update count for SQL: " + statement);
                            SQLWarning warningToLog = stmt.getWarnings();
                            while (warningToLog != null) {
                                logger.debug("SQLWarning ignored: SQL state '" + warningToLog.getSQLState() +
                                        "', error code '" + warningToLog.getErrorCode() +
                                        "', message [" + warningToLog.getMessage() + "]");
                                warningToLog = warningToLog.getNextWarning();
                            }
                        }
                    }
                    catch (SQLException ex) {
                        boolean dropStatement = StringUtils.startsWithIgnoreCase(statement.trim(), "drop");
                        if (continueOnError || (dropStatement && ignoreFailedDrops)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(ScriptStatementFailedException.buildErrorMessage(statement, stmtNumber, resource), ex);
                            }
                        }
                        else {
                            throw new ScriptStatementFailedException(statement, stmtNumber, resource, ex);
                        }
                    }
                }
            }
            finally {
                try {
                    stmt.close();
                }
                catch (Throwable ex) {
                    logger.debug("Could not close JDBC Statement", ex);
                }
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            if (logger.isInfoEnabled()) {
                logger.info("Executed SQL script from " + resource + " in " + elapsedTime + " ms.");
            }
        }
        catch (Exception ex) {
            if (ex instanceof ScriptException) {
                throw (ScriptException) ex;
            }
            throw new UncategorizedScriptException(
                    "Failed to execute database script from resource [" + resource + "]", ex);
        }
    }

}
