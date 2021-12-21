package com.example.starter.handler.imp;

import com.example.starter.message.imp.AbstractUpMessage;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Response;

public interface InterHandler {

  void handler(AbstractUpMessage up, HttpServerResponse resp);

  Future<JsonObject> dbHandler(String apiName, String key, JsonObject params);

  Future<Response> redisHandler(String apiName, String key, JsonObject params);
}
