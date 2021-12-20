package com.example.starter.config;

import com.example.starter.loadjson.JsonObjectConfig;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MysqlConfig extends JsonObjectConfig {

    public JsonArray configs;

    public MysqlConfig(Vertx vertx, String path){
        super(vertx,path);
    }

    @Override
    public void parse(JsonObject jsonObject) {
        configs = jsonObject.getJsonArray("config");
    }
}
