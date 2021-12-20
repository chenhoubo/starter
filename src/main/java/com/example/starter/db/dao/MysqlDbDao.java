package com.example.starter.db.dao;

import com.example.starter.db.MySQLUtil;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MysqlDbDao {

  protected Logger logger = LoggerFactory.getLogger(MysqlDbDao.class);

  protected MySQLUtil mySQLPool;

  public static final String ALL_PLACE = "*";
  public static final String INSERT_TABLE = "insert into %s (name,status, json, create_time, update_time, end_time) VALUES(?,?, ?, ?, ?, ?)";
  public static final String UPDATE_TABLE = "update %s set name=?,status=?,json=?,update_time=? where 1=1 %s";
  public static final String SELECT_TABLE = "select %s from %s where 1=1 %s";
  public static final String DELETE_TABLE = "delete from %s where 1=1 %s";

  public MysqlDbDao(MySQLUtil mySQLPool) {
    this.mySQLPool = mySQLPool;
  }

  /*************************
   * 插入新数据
   */
  public Future<JsonObject> save(String tableName, JsonObject params) {
    Promise<JsonObject> resultPromise = Promise.promise();
    List<Object> list = new ArrayList<>();
    String sql = String.format(INSERT_TABLE, tableName);
    list.add(params.getString("name"));
    list.add(params.getInteger("status", 0));
    list.add(params);
    list.add(System.currentTimeMillis());
    list.add(System.currentTimeMillis());
    list.add(null);
    removeBaseFiled(params);
    mySQLPool.getConfigClient()
      .preparedQuery(sql)
      .execute(Tuple.tuple(list), ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          long lastInsertId = rows.property(MySQLClient.LAST_INSERTED_ID);
          JsonObject object = new JsonObject();
          object.put("id", lastInsertId);
          resultPromise.complete(object);
          logger.info("success to excute prepared sql-----:{} " + sql);
        } else {
          resultPromise.fail(ar.cause());
          logger.error("error to excute prepared sql-----:{} " + sql, ar.cause());
        }
      });
    return resultPromise.future();
  }

  /*************************
   * 更新一条数据
   */
  public Future<JsonObject> update(String tableName, JsonObject params) {
    Promise<JsonObject> resultPromise = Promise.promise();
    List<Object> list = new ArrayList<>();
    String sql = String.format(UPDATE_TABLE, tableName,andId(params.getLong("id")));
    list.add(params.getString("name"));
    list.add(params.getInteger("status"));
    list.add(params);
    list.add(System.currentTimeMillis());
    removeBaseFiled(params);
    mySQLPool.getConfigClient()
      .preparedQuery(sql)
      .execute(Tuple.tuple(list), res -> {
        if (res.succeeded()) {
          JsonObject object = new JsonObject();
          object.put("id", res.result().size());
          resultPromise.complete(object);
          logger.info("success to excute prepared sql-----:{} " + sql);
        } else {
          resultPromise.fail(res.cause());
          logger.error("error to excute prepared sql-----:{} " + sql, res.cause());
        }
      });
    return resultPromise.future();
  }

  public Future<JsonObject> page(String tableName, JsonObject params) {
    Promise<JsonObject> resultPromise = Promise.promise();
    String where = getWhere(params);
    String sql = String.format(SELECT_TABLE, ALL_PLACE, tableName, where);
    Integer startIndex = params.getInteger("startIndex");
    Integer endIndex = params.getInteger("endIndex");
    params.remove("startIndex");
    params.remove("endIndex");
    String countWhere = getWhere(params);
    String sqlCount = String.format(SELECT_TABLE, "count(*) as total", tableName, countWhere);

    CompositeFuture cpf = CompositeFuture.all(
      queryList(sql),
      queryCount(sqlCount)
    );
    cpf.onSuccess(res -> {
      JsonArray dataArray = res.resultAt(0);
      JsonObject count = res.resultAt(1);
      Integer total = count.getInteger("total");
      JsonObject result = new JsonObject();
      result.put("startIndex", startIndex).put("endIndex", endIndex)
        .put("total", total).put("data", dataArray);
      resultPromise.complete(result);
    }).onFailure(err -> {
      resultPromise.fail(err.getMessage());
    });
    return resultPromise.future();
  }

  public Future<JsonObject> deleteById(String tableName, long id) {
    Promise<JsonObject> resultPromise = Promise.promise();
    String where = andId(id);
    String sql = String.format(DELETE_TABLE, tableName, where);
    mySQLPool.getConfigClient()
      .preparedQuery(sql)
      .execute(res -> {
        if (res.succeeded()) {
          JsonObject object = new JsonObject();
          object.put("id", res.result().size());
          resultPromise.complete(object);
          logger.info("success to excute prepared sql-----:{} " + sql);
        } else {
          resultPromise.fail(res.cause());
          logger.error("error to excute prepared sql-----:{} " + sql, res.cause());
        }
      });
    return resultPromise.future();
  }

  public Future<JsonObject> findById(String tableName, long id) {
    Promise<JsonObject> resultPromise = Promise.promise();
    String where = andId(id);
    String sql = String.format(SELECT_TABLE, ALL_PLACE, tableName, where);
    mySQLPool.getConfigClient()
      .preparedQuery(sql)
      .execute(res -> {
        if (res.succeeded()) {
          RowSet<Row> rows = res.result();
          JsonArray result = new JsonArray();
          rows.forEach(row -> {
            JsonObject r = row.toJson();
            JsonObject json = r.getJsonObject("json");
            if (json != null) {
              r.remove("json");
              json.mergeIn(r);
              result.add(json);
            } else {
              result.add(r);
            }
          });
          resultPromise.complete(result.getJsonObject(0));
          logger.info("success to excute prepared sql-----:{} " + sql);
        } else {
          resultPromise.fail(res.cause());
          logger.error("error to excute prepared sql-----:{} " + sql, res.cause());
        }
      });
    return resultPromise.future();
  }

  private Future<JsonArray> queryList(String sql) {
    Promise<JsonArray> resultPromise = Promise.promise();
    mySQLPool.getConfigClient()
      .preparedQuery(sql)
      .execute(ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          JsonArray result = new JsonArray();
          rows.forEach(row -> {
            JsonObject r = row.toJson();
            JsonObject json = r.getJsonObject("json");
            if (json != null) {
              r.remove("json");
              json.mergeIn(r);
              result.add(json);
            } else {
              result.add(r);
            }
          });
          logger.info("Success to excute prepared sql:{}", sql);
          resultPromise.complete(result);
        } else {
          logger.error("Failed to excute prepared sql:{}", ar.cause().getMessage());
          resultPromise.fail(ar.cause());
        }
      });
    return resultPromise.future();
  }

  private Future<JsonObject> queryCount(String sql) {
    Promise<JsonObject> resultPromise = Promise.promise();
    mySQLPool.getConfigClient()
      .preparedQuery(sql).execute(ar -> {
      if (ar.succeeded()) {
        RowSet<Row> rows = ar.result();
        JsonArray result = new JsonArray();
        rows.iterator().forEachRemaining(row -> result.add(row.toJson()));
        logger.info("Success to excute prepared sql:{}", sql);
        resultPromise.complete(result.getJsonObject(0));
      } else {
        logger.error("Failed to excute prepared sql:{}", ar.cause().getMessage());
        resultPromise.fail(ar.cause());
      }
    });
    return resultPromise.future();
  }

  public String getWhere(JsonObject params) {
    StringBuilder sql = new StringBuilder();
    Iterator<Map.Entry<String, Object>> iterator = params.stream().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Object> next = iterator.next();
      String key = next.getKey();
      String value = String.valueOf(next.getValue());
      if ("id".equals(key) || "name".equals(key) || "status".equals(key)) {
        sql.append(" and ").append(key).append(" = ").append(value);
        break;
      } else if ("startTime".equals(key)) {
        sql.append(andCreateTimeStart(Long.parseLong(value)));
        break;
      } else if ("endTime".equals(key)) {
        sql.append(andCreateTimeEnd(Long.parseLong(value)));
        break;
      } else if ("startIndex".equals(key) || "endIndex".equals(key)) {
        sql.append(" ORDER BY create_time ").append(andPages(params));
        break;
      } else {
        sql.append(" and json->>'$.").append(key).append("'").append(" = ").append(value);
        break;
      }
    }
    return sql.toString();
  }

  public String andId(long id) {
    return " and  id = " + id;
  }

  public String andCreateTimeStart(long startTime) {
    return " and  create_time >= " + startTime;
  }

  public String andCreateTimeEnd(long endTime) {
    return " and create_time <= " + endTime;
  }

  public String andUpdateTimeStart(long startTime) {
    return " and update_time >= " + startTime;
  }

  public String andUpdateTimeEnd(long endTime) {
    return " and update_time <= " + endTime;
  }

  public String andEndTimeStart(long startTime) {
    return " and end_time >= " + startTime;
  }

  public String andEndTimeEnd(long endTime) {
    return " end_time <= " + endTime;
  }

  public String andPages(JsonObject params) {
    int start = params.getInteger("startIndex", 0);
    int end = params.getInteger("endIndex", 20);
    int size = end - start;
    return " limit " + start + "," + size;
  }

  private void removeBaseFiled(JsonObject params) {
    params.remove("id");
    params.remove("name");
    params.remove("status");
    params.remove("create_time");
    params.remove("update_time");
    params.remove("end_time");
  }
}

