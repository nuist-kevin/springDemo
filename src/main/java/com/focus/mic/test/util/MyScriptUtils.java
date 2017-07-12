package com.focus.mic.test.util;

import com.focus.mic.test.util.SqlScriptInfo.SQLTYPE;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
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
  // 默认情况下，DDL需要在本地和美国两个点执行
  public static boolean BOTH_USA_AND_LOCAL = true;

  private static final Log logger = LogFactory.getLog(MyScriptUtils.class);

  public static void executeSqlScriptsFromDirectory(String directory)
      throws IOException, SQLException {
    Map<SqlScriptInfo, LinkedList<Path>> connectionLinkedListMap = propareExecutionEnv(directory);
    for (SqlScriptInfo sqlScriptInfo : connectionLinkedListMap.keySet()) {
      try (Connection connection = DBUtil.getConnectionForSchema(sqlScriptInfo.getSchema())) {
        executeScripts(connection, connectionLinkedListMap.get(sqlScriptInfo));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // 删除临时创建的文件
    FileUtils.deleteFilesWithSuffix(directory, "usa");
  }


  public static void executeScripts(Connection connection, LinkedList<Path> scriptList) {
    for (Path script : scriptList) {
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
        if (statement.trim().toLowerCase().contains("set define off")) {
          continue;
        }
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


  /*
    *   SQL脚本路径样式：
    *       /xxx/version/CB/DDL/caiwen/01.alter_sequence.sql
    *       /xxx/version/CB/DML/caiwen/02.update_ddd.sql
    * */
  public static LinkedHashMap<SqlScriptInfo, LinkedList<Path>> propareExecutionEnv(
      String directoryPath)
      throws IOException, SQLException {
    LinkedHashMap<SqlScriptInfo, LinkedList<Path>> connectionLinkedListMap = new LinkedHashMap<>();

    File versionDir = new File(directoryPath);
    for (File schemaDir : versionDir.listFiles()) {
      System.out.println(schemaDir.getName());
      for (File sqlTypeDir : schemaDir.listFiles()) {
        for (File userDir : sqlTypeDir.listFiles()) {
          LinkedList<Path> localScriptList = new LinkedList<>();
          LinkedList<Path> usaScriptList = new LinkedList<>();
          // 遍历当前文件夹
          Files.walkFileTree(Paths.get(userDir.getPath()), new SimpleFileVisitor<Path>() {
            int localIndex = 0;
            int usaIndex = 0;

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
              // readme 文件忽略， 非 .sql 结尾的也忽略
              String fileName = file.getFileName().toString().toLowerCase();
              if (!fileName.contains("readme") && fileName.endsWith("sql")) {
                // 如果是DDL，则要执行两个点
                if ("CB".equals(schemaDir.getName()) && "DDL".equals(sqlTypeDir.getName())
                    && BOTH_USA_AND_LOCAL) {
                  // 如果是sequence文件，替换 sequence 中的起始值为2
                  if (file.getFileName().toString().toLowerCase().contains("alter_sequence")) {
                    File usaSequenceFile = generateUsaSequenceScript(file.toFile());
                    usaScriptList.add(usaIndex, usaSequenceFile.toPath());
                  } else {
                    usaScriptList.add(usaIndex, file);
                  }
                  usaIndex += 1;
                }
                // 添加本地点需要执行的脚本
                localScriptList.add(localIndex, file);
                localIndex += 1;
              }
              return FileVisitResult.CONTINUE;
            }
          });
          connectionLinkedListMap
              .put(new SqlScriptInfo(schemaDir.getName(), SQLTYPE.valueOf(sqlTypeDir.getName())),
                  localScriptList);
          if (BOTH_USA_AND_LOCAL) {
            connectionLinkedListMap.put(new SqlScriptInfo("CBUSA", SQLTYPE.DDL), usaScriptList);
          }
        }
      }
    }
    return connectionLinkedListMap;

  }

  public static File generateUsaSequenceScript(File file) throws IOException {
    File usaSequenceSql = new File(file.getAbsolutePath() + "-usa");
    LineNumberReader reader = new LineNumberReader(new FileReader(file));
    try (Writer writer = new FileWriter(usaSequenceSql)) {
      String currentLine = reader.readLine();
      while (currentLine != null) {
        String newLine = currentLine;
        if (currentLine.toLowerCase().contains("minvalue 1")) {
          newLine = currentLine.replace("minvalue 1", "minvalue 2");
        }
        if (currentLine.toLowerCase().contains("start with 1")) {
          newLine = currentLine.replace("start with 1", "start with 2");
        }
        writer.write(newLine + "\n");
        currentLine = reader.readLine();
      }
    }
    return usaSequenceSql;
  }
}
