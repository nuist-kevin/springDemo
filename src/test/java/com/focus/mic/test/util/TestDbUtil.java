package com.focus.mic.test.util;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by caiwen on 2017/7/10.
 */
public class TestDbUtil {

  @Test
  public void test()  {
    Connection connection = null;
    try  {
      connection = DBUtil.getConnectionForSchema("CB");
      Assert.assertNotNull(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}
