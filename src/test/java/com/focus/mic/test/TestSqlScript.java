package com.focus.mic.test;

import com.focus.mic.test.util.MyScriptUtils;
import com.mysql.jdbc.authentication.MysqlClearPasswordPlugin;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.support.EncodedResource;

public class TestSqlScript {

  @Test
  public void testScript() throws ClassNotFoundException, SQLException, IOException {

    MyScriptUtils.BOTH_USA_AND_LOCAL = false;
    MyScriptUtils.executeSqlScriptsFromDirectory("G:\\跨境\\研发工作文档汇总\\跨境脚本\\2017\\CB_LV_2017.19(@CBOSS_LV_2017.12)");

  }
}
