package com.focus.mic.test.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class FileUtils {

  /*
  *   SQL脚本路径样式：
  *       CB/DDL/caiwen/01.alter_sequence.sql
  *       CB/DML/caiwen/02.update_ddd.sql
  * */
  public static LinkedHashMap<Connection, LinkedList<Path>> propareExecutionEnv(
      String directoryPath)
      throws IOException, SQLException {
    LinkedHashMap<Connection, LinkedList<Path>> connectionLinkedListMap = new LinkedHashMap<>();

    File scriptDir = new File(directoryPath);
    for (File schemaDir : scriptDir.listFiles()) {
      System.out.println(schemaDir.getName());
      for (File sqlTypeDir : schemaDir.listFiles()) {

        for (File userDir : sqlTypeDir.listFiles()) {
          // 如果是DDL，则要执行两个点
          if ("CB".equals(schemaDir.getName()) && "DDL".equals(sqlTypeDir.getName())) {
            LinkedList<Path> localScriptList = new LinkedList<>();
            LinkedList<Path> usaScriptList = new LinkedList<>();

            Files.walkFileTree(Paths.get(userDir.getPath()), new SimpleFileVisitor<Path>() {
              int localIndex = 0;
              int usaIndex = 0;

              @Override
              public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                  throws IOException {

                if (!file.getFileName().toString().toLowerCase().contains("readme")) {
                  String index = file.getFileName().toString().substring(0, 2);
                  localScriptList.add(localIndex, file);
                  localIndex += 1;

                  // 新建sequence脚本，名为 "原文件名-usa", 替换 sequence 脚本中的起始值为 2
                  if (file.getFileName().toString().toLowerCase().contains("alter_sequence")) {
                    File usaSequenceFile = generateUsaSequenceScript(file.toFile());
                    usaScriptList.add(usaIndex, usaSequenceFile.toPath());
                  } else {
                    usaScriptList.add(usaIndex, file);
                  }
                  usaIndex += 1;
                }
                return FileVisitResult.CONTINUE;
              }
            });
            Connection connection = DBUtil.getConnectionForSchema("CB");
            Connection connectionUSA = DBUtil.getConnectionForSchema("CBUSA");
            connectionLinkedListMap.put(connection, localScriptList);
            connectionLinkedListMap.put(connectionUSA, usaScriptList);
          } else {
            LinkedList<Path> localScriptList = new LinkedList<>();
            Files.walkFileTree(Paths.get(userDir.getPath()), new SimpleFileVisitor<Path>() {
              @Override
              public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                  throws IOException {
                if (!file.getFileName().toString().toLowerCase().contains("readme")) {
                  localScriptList.add(file);
                }
                return FileVisitResult.CONTINUE;
              }
            });
            Connection connection = DBUtil.getConnectionForSchema(schemaDir.getName());
            connectionLinkedListMap.put(connection, localScriptList);
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


  // 清除指定目录下所有指定后缀名的文件
  public static void deleteFilesWithSuffix(String directory, String fileName) throws IOException {
    Files.walkFileTree(Paths.get(directory), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.getFileName().toString().endsWith(fileName)) {
          Files.delete(file);
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  public static void main(String[] args) throws Exception {
    String directoryPath = "G:\\跨境\\研发工作文档汇总\\跨境脚本\\2017\\CBOSS_LV_2017.06";
    FileUtils.propareExecutionEnv(directoryPath);

    deleteFilesWithSuffix(directoryPath, "usa");

  }
}
