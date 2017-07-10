package com.focus.mic.test.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.DataSourceConnectionFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class DBUtil {

  private static Properties properties;

  static {
    try {
      properties = new Properties();
      Resource resource = new ClassPathResource("db.properties");
      properties.load(resource.getInputStream());
      Class.forName("com.mysql.jdbc.Driver");
      Class.forName("oracle.jdbc.driver.OracleDriver");

    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Connection getConnectionForSchema(String schema) throws SQLException {
    Connection connection;
    switch (schema) {
      case ("CB"):
        connection = DriverManager
            .getConnection(properties.getProperty("cb.local.url"),
                properties.getProperty("cb.local.name"), properties.getProperty("cb.local.pwd"));
        break;
      case ("CBUSA"):
        connection = DriverManager
            .getConnection(properties.getProperty("cb.usa.url"),
                properties.getProperty("cb.usa.name"), properties.getProperty("cb.usa.pwd"));
        break;
      case ("CBOSS"):
        connection = DriverManager
            .getConnection(properties.getProperty("cboss.local.url"),
                properties.getProperty("cboss.local.name"),
                properties.getProperty("cboss.local.pwd"));
        break;
      case ("MICOSS2005"):
        connection = DriverManager
            .getConnection(properties.getProperty("micoss.url"),
                properties.getProperty("micoss.name"), properties.getProperty("micoss.pwd"));
        break;

      default:
        connection = null;
    }
    return connection;
  }
}
