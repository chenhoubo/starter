package com.example.starter.message.imp;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

/******
 * 消息编码/解码
 * *******/
public interface MessageFactory {
    void decode(int dbId, JsonObject body, HttpServerRequest request);

    void encode();
}
