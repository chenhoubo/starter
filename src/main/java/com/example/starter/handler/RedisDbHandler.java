package com.example.starter.handler;

import com.example.starter.config.Configure;
import com.example.starter.handler.imp.InterHandler;
import com.example.starter.message.imp.AbstractUpMessage;
import com.example.starter.redis.RedisUtil;
import io.vertx.core.Future;
import io.vertx.core.VertxException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Response;

public class RedisDbHandler implements InterHandler {

  @Override
  public void handler(AbstractUpMessage up, HttpServerResponse resp) {

  }

  @Override
  public Future<JsonObject> dbHandler(String apiName, String key, JsonObject params) {
    return null;
  }

  @Override
  public Future<Response> redisHandler(String apiName, String key, JsonObject params) {
    RedisUtil redisUtil = Configure.getInstance().redisUtil;
    Future<Response> operationResult = null;
      switch (apiName) {
        case "set":
        redisUtil.setConfigValue(key,params.toString(),7200);
        operationResult = Future.succeededFuture();
        break;
      case "get":
        operationResult = redisUtil.getConfigValue(key);
        break;
      default:
        operationResult = Future.failedFuture(new VertxException("Doesn't support this operation!"));
    }
    return operationResult;
  }

}
