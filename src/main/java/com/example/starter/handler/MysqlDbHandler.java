package com.example.starter.handler;

import com.example.starter.config.Configure;
import com.example.starter.db.dao.PlayerDao;
import com.example.starter.handler.imp.InterHandler;
import com.example.starter.message.cs.DemoRequest;
import com.example.starter.message.imp.AbstractUpMessage;
import com.example.starter.message.sc.DemoResponse;
import io.vertx.core.Future;
import io.vertx.core.VertxException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Response;

public class MysqlDbHandler implements InterHandler {
  @Override
  public void handler(AbstractUpMessage up, HttpServerResponse resp) {
    //上传参数
    DemoRequest request = (DemoRequest) up;
    System.out.println("上传参数:" + request.dbId);

    int type = request.dbId;
    PlayerDao playerDao = Configure.getInstance().getDaoManager(type).getPlayerDao();
    String sql = "select * from user;";
    playerDao.queryList(sql, res -> {
      System.out.println(res.result());
    });
    //返回数据
    String n = "cscscs---";
    String in = "info ---";
    //编码返回json
    DemoResponse response = new DemoResponse((short) type, n, in);
    response.encode();
    resp.end(response.SendMessage());
  }

  @Override
  public Future<JsonObject> dbHandler(String tableName, String operation, JsonObject params) {
    Integer dbId = params.getInteger("dbId");
    params.remove("dbId");
    PlayerDao playerDao = Configure.getInstance().getDaoManager(dbId).getPlayerDao();
    Future<JsonObject> operationResult = null;
    switch (operation) {
      case "save":
        operationResult = playerDao.save(tableName, params);
        break;
      case "update":
        operationResult = playerDao.update(tableName, params);
        break;
      case "delete":
        operationResult = playerDao.deleteById(tableName, params.getLong("id"));
        break;
      case "page":
        operationResult = playerDao.page(tableName, params);
        break;
      case "findById":
        operationResult = playerDao.findById(tableName, params.getLong("id"));
        break;
      default:
//        String sql = "select * from user;";
//        playerDao.queryList(sql, res -> {
//          System.out.println(res.result());
//        });
        operationResult = Future.failedFuture(new VertxException("Doesn't support this operation!"));
    }
    return operationResult;
  }

  @Override
  public Future<Response> redisHandler(String apiName, String key, JsonObject params) {
    return null;
  }

}
