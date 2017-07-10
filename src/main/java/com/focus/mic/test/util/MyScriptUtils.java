package com.focus.mic.test.util;

import java.io.File;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.*;
import org.springframework.util.Assert;
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

  public static final String CREATE_OR_REPLACE = "CREATE OR REPLACE";
  public static final String PROCEDURE_END = "END";

  private static final Log logger = LogFactory.getLog(MyScriptUtils.class);

  public static void executeScripts( Connection connection, LinkedList<Path> scriptList) {
    for (Path script: scriptList) {
      executeScript(connection, script);
    }
  }


  public static void executeScript(Connection connection, Path path) {
    executeSqlScript(connection, new EncodedResource(new PathResource(path)), false, true,
        DEFAULT_COMMENT_PREFIX, DEFAULT_STATEMENT_SEPARATOR,
        DEFAULT_BLOCK_COMMENT_START_DELIMITER, DEFAULT_BLOCK_COMMENT_END_DELIMITER);
  }

  public static void executeScript(Connection connection, EncodedResource resource) {
    executeSqlScript(connection, resource, false, true,
        DEFAULT_COMMENT_PREFIX, DEFAULT_STATEMENT_SEPARATOR,
        DEFAULT_BLOCK_COMMENT_START_DELIMITER, DEFAULT_BLOCK_COMMENT_END_DELIMITER);
  }


  public static void executeSqlScript(Connection connection, EncodedResource resource,
      boolean continueOnError,
      boolean ignoreFailedDrops, String commentPrefix, String separator,
      String blockCommentStartDelimiter,
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
      } catch (IOException ex) {
        throw new CannotReadScriptException(resource, ex);
      }

      if (separator == null) {
        separator = DEFAULT_STATEMENT_SEPARATOR;
      }
      if (!EOF_STATEMENT_SEPARATOR.equals(separator) && !containsSqlScriptDelimiters(script,
          separator)) {
        separator = FALLBACK_STATEMENT_SEPARATOR;
      }

      List<String> statements = new LinkedList<String>();
      splitSqlScript(resource, script, separator, commentPrefix, blockCommentStartDelimiter,
          blockCommentEndDelimiter, statements);

      List<String> statementsWithProcedures = new LinkedList<String>();
      StringBuilder stringBuilder = new StringBuilder();
      for (int i = 0; i < statements.size(); i++) {
        String statement = statements.get(i);
        if (statement.toUpperCase().startsWith(CREATE_OR_REPLACE)) {
          stringBuilder = new StringBuilder();
          stringBuilder.append(statement).append(";");
        } else {
          if (stringBuilder.toString().equals("")) {
            statementsWithProcedures.add(statement);
          } else {
            stringBuilder.append(statement).append(";");
          }
        }
        if (statement.toUpperCase().endsWith(PROCEDURE_END)) {
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
          } catch (SQLException ex) {
            boolean dropStatement = StringUtils.startsWithIgnoreCase(statement.trim(), "drop");
            if (continueOnError || (dropStatement && ignoreFailedDrops)) {
              if (logger.isDebugEnabled()) {
                logger.debug(ScriptStatementFailedException
                    .buildErrorMessage(statement, stmtNumber, resource), ex);
              }
            } else {
              throw new ScriptStatementFailedException(statement, stmtNumber, resource, ex);
            }
          }
        }
      } finally {
        try {
          stmt.close();
        } catch (Throwable ex) {
          logger.debug("Could not close JDBC Statement", ex);
        }
      }

      long elapsedTime = System.currentTimeMillis() - startTime;
      if (logger.isInfoEnabled()) {
        logger.info("Executed SQL script from " + resource + " in " + elapsedTime + " ms.");
      }
    } catch (Exception ex) {
      if (ex instanceof ScriptException) {
        throw (ScriptException) ex;
      }
      throw new UncategorizedScriptException(
          "Failed to execute database script from resource [" + resource + "]", ex);
    }
  }


  public static void splitSqlScript(EncodedResource resource, String script, String separator,
      String commentPrefix,
      String blockCommentStartDelimiter, String blockCommentEndDelimiter, List<String> statements)
      throws ScriptException {

    Assert.hasText(script, "'script' must not be null or empty");
    Assert.notNull(separator, "'separator' must not be null");
    Assert.hasText(commentPrefix, "'commentPrefix' must not be null or empty");
    Assert.hasText(blockCommentStartDelimiter,
        "'blockCommentStartDelimiter' must not be null or empty");
    Assert
        .hasText(blockCommentEndDelimiter, "'blockCommentEndDelimiter' must not be null or empty");

    StringBuilder sb = new StringBuilder();
    boolean inSingleQuote = false;
    boolean inDoubleQuote = false;
    boolean inEscape = false;
    for (int i = 0; i < script.length(); i++) {
      char c = script.charAt(i);
      if (inEscape) {
        inEscape = false;
        sb.append(c);
        continue;
      }
      // MySQL style escapes
      if (c == '\\') {
        inEscape = true;
        sb.append(c);
        continue;
      }
      if (!inDoubleQuote && (c == '\'')) {
        inSingleQuote = !inSingleQuote;
      } else if (!inSingleQuote && (c == '"')) {
        inDoubleQuote = !inDoubleQuote;
      }
      if (!inSingleQuote && !inDoubleQuote) {
        if (script.startsWith(separator, i)) {
          // We've reached the end of the current statement
          if (sb.length() > 0) {
            statements.add(sb.toString());
            sb = new StringBuilder();
          }
          i += separator.length() - 1;
          continue;
        } else if (script.startsWith(commentPrefix, i)) {
          // Skip over any content from the start of the comment to the EOL
          int indexOfNextNewline = script.indexOf("\n", i);
          if (indexOfNextNewline > i) {
            i = indexOfNextNewline;
            continue;
          } else {
            // If there's no EOL, we must be at the end of the script, so stop here.
            break;
          }
        } else if (script.startsWith(blockCommentStartDelimiter, i)) {
          // Skip over any block comments
          int indexOfCommentEnd = script.indexOf(blockCommentEndDelimiter, i);
          if (indexOfCommentEnd > i) {
            i = indexOfCommentEnd + blockCommentEndDelimiter.length() - 1;
            continue;
          } else {
            throw new ScriptParseException(
                "Missing block comment end delimiter: " + blockCommentEndDelimiter, resource);
          }
        } else if (c == ' ' || c == '\n' || c == '\t') {
          // Avoid multiple adjacent whitespace characters
          if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
            c = ' ';
          } else {
            continue;
          }
        } else if (c == '/') {
          continue;
        }
      }
      sb.append(c);
    }
    if (StringUtils.hasText(sb)) {
      statements.add(sb.toString());
    }
  }

  public static void executeSqlScriptsFromDirectory(String directory)
      throws IOException, SQLException {
    Map<Connection, LinkedList<Path>> connectionLinkedListMap = FileUtils.propareExecutionEnv(directory);
    for (Connection connection: connectionLinkedListMap.keySet()) {
      executeScripts(connection, connectionLinkedListMap.get(connection));
    }

    FileUtils.deleteFilesWithSuffix(directory, "usa");

  }
}
