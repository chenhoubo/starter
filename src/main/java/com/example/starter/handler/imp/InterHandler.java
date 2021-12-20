package com.example.starter.handler.imp;

import com.example.starter.message.imp.AbstractUpMessage;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

public interface InterHandler {
    void handler(AbstractUpMessage up, HttpServerResponse resp);

    Future<JsonObject> baseHandler(String tableName, String operation, JsonObject params);
}
