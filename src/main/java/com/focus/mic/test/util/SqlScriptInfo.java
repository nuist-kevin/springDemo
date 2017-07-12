package com.focus.mic.test.util;

public class SqlScriptInfo {

  private String schema;
  private SQLTYPE sqltype;

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public SQLTYPE getSqltype() {
    return sqltype;
  }

  public void setSqltype(SQLTYPE sqltype) {
    this.sqltype = sqltype;
  }

  public SqlScriptInfo(String schema, SQLTYPE sqltype) {
    this.schema = schema;
    this.sqltype = sqltype;
  }

  enum SQLTYPE {
    DDL, DML
  }

}
