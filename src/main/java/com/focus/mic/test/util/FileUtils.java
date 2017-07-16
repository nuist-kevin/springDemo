package com.focus.mic.test.util;


import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {

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

}
