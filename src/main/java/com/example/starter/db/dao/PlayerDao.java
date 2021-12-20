package com.example.starter.db.dao;

import com.example.starter.db.MySQLUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerDao extends MysqlDbDao {

  protected Logger logger = LoggerFactory.getLogger(PlayerDao.class);

  public PlayerDao(MySQLUtil mySQLPool) {
    super(mySQLPool);
  }

  /*************************
   * 查询数据
   * 根据 实体类T获取数据并实例化
   */
  public <T> void queryList(String sql, Handler<AsyncResult<JsonArray>> handler) {
    mySQLPool.getConfigClient().query(sql)
      .execute(qRes -> {
        if (qRes.succeeded()) {
          RowSet<Row> rows = qRes.result();
          JsonArray result = new JsonArray();
          rows.forEach(row -> {
            JsonObject r = row.toJson();
            JsonObject j = r.getJsonObject("json");
            if (j != null) {
              r.remove("json");
              j.mergeIn(r);
              result.add(j);
            } else {
              result.add(r);
            }
          });
          logger.info("Success to excute prepared sql:{}", sql);
          handler.handle(Future.succeededFuture(result));
        } else {
          handler.handle(Future.failedFuture(qRes.cause()));
          logger.error("error to excute prepared sql-----:{} " + sql, qRes.cause());
        }
      });
  }
}
