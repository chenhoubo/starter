package com.example.starter.verticle;

import com.example.starter.config.RuntimeConfig;
import com.example.starter.constants.HandlerCode;
import com.example.starter.constants.HttpStatus;
import com.example.starter.handler.imp.HandlerManager;
import com.example.starter.handler.imp.InterHandler;
import com.example.starter.message.MessageRecognizer;
import com.example.starter.message.NetDownError;
import com.example.starter.message.imp.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.redis.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HttpServerVerticle extends AbstractVerticle {
  private final Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);
  private HttpServer httpServer;

  private IMessageRecognizer recognizer;

  private Router baseRouter;
  private RuntimeConfig runtimeConfig;

  Set<HttpMethod> allowedMethods;
  Set<String> allowedHeaders;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    initConfig();
    initRoute();
    bindServer(startPromise);
  }

  private void initConfig() {
    recognizer = new MessageRecognizer();
    runtimeConfig = new RuntimeConfig();
    allowedHeaders = new HashSet<>();
    // 配置跨域请求
    allowedHeaders.add("Origin");
    allowedHeaders.add("Access-Control-Request-Headers");
    allowedHeaders.add("Access-Control-Allow-Headers");
    allowedHeaders.add("DNT");
    allowedHeaders.add("X-Requested-With");
    allowedHeaders.add("X-Mx-ReqToken");
    allowedHeaders.add("Keep-Alive");
    allowedHeaders.add("User-Agent");
    allowedHeaders.add("If-Modified-Since");
    allowedHeaders.add("Cache-Control");
    allowedHeaders.add("Content-Type");
    allowedHeaders.add("Accept");
    allowedHeaders.add("Connection");
    allowedHeaders.add("Cookie");
    allowedHeaders.add("X-XSRF-TOKEN");
    allowedHeaders.add("X-CSRF-TOKEN");
    allowedHeaders.add("Authorization");

    allowedMethods = new HashSet<>();
    allowedMethods.add(HttpMethod.GET);
    allowedMethods.add(HttpMethod.POST);
    allowedMethods.add(HttpMethod.DELETE);
    allowedMethods.add(HttpMethod.PATCH);
    allowedMethods.add(HttpMethod.OPTIONS);
    allowedMethods.add(HttpMethod.PUT);
    allowedMethods.add(HttpMethod.UPDATE);
    allowedMethods.add(HttpMethod.MERGE);
    allowedMethods.add(HttpMethod.ACL);
    allowedMethods.add(HttpMethod.MKWORKSPACE);
    allowedMethods.add(HttpMethod.MOVE);
    allowedMethods.add(HttpMethod.HEAD);
    allowedMethods.add(HttpMethod.TRACE);
    allowedMethods.add(HttpMethod.CONNECT);
  }

  private void initRoute() {
    baseRouter = Router.router(vertx);
    baseRouter.route().handler(CorsHandler.create("*").allowedMethods(allowedMethods).allowedHeaders(allowedHeaders).allowCredentials(true));
    baseRouter.route().handler(BodyHandler.create().setBodyLimit(1024 * 1024 * 200));
    baseRouter.post("/test").handler(ar -> {
      System.out.println("111");
    });
    baseRouter.post("/api/:apiName/:operation").handler(this::handleCommonApi);
    baseRouter.post("/redis/:apiName/:key").handler(this::handleRedisApi);
    baseRouter.errorHandler(HttpStatus.FAIL.code(), routingContext -> {
      Throwable failure = routingContext.failure();
      String errorMessage = failure.getMessage();
      logger.error("Web Service Error: {}", errorMessage);
      failure.printStackTrace();
      returnJson(routingContext, HttpStatus.FAIL.message(), failure.getMessage(), HttpStatus.FAIL.code(), false);
    });
  }

  private void bindServer(Promise<Void> promise) {
    HttpServerOptions options = new HttpServerOptions()
      .setIdleTimeout(10000)
      .setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
      .setTcpKeepAlive(true);
    HttpServer httpServer = vertx.createHttpServer(options);
    httpServer.requestHandler(baseRouter);
    httpServer.listen(
      runtimeConfig.getServerPort(), "127.0.0.1",
      result -> {
        if (result.succeeded()) {
          logger.info("HTTP server started on port :" + runtimeConfig.getServerPort());
          promise.complete();
        } else {
          logger.error("The port has bounded by another program!:" + runtimeConfig.getServerPort());
          promise.fail(result.cause());
        }
      }
    );
//    httpServer.exceptionHandler(it -> {
//      logger.error("\n ---network io error:" + it.getMessage());
//      promise.fail(it.getMessage());
//    });
  }

  <T> void returnJson(RoutingContext context, String msg, T data, int code, Boolean successFlag) {
    if(data instanceof Response){
      String d = ((Response) data).toBuffer().toString();
      returnJsonObject(context, new JsonObject().put("msg", msg).put("flag", successFlag).put("code", code).put("data", d), code);
    }else {
      returnJsonObject(context, new JsonObject().put("msg", msg).put("flag", successFlag).put("code", code).put("data", data), code);
    }
  }

  void returnJsonObject(RoutingContext context, JsonObject jsonObject, int code) {
    context.response()
      .setStatusCode(code)
      .putHeader("content-type", "application/json; charset=utf-8");
    context.end(jsonObject.toBuffer());
  }

  <T> void handleAsyncResult(RoutingContext context, Future<T> result) {
    result.onSuccess(res -> returnJson(context, HttpStatus.OK.message(), res, HttpStatus.OK.code(), true))
      .onFailure(err -> returnJson(context, err.getMessage(), null, HttpStatus.FAIL.code(), false));
  }

  void handleCommonApi(RoutingContext context) {
    String apiName = context.pathParam("apiName");
    String operation = context.pathParam("operation");
    JsonObject body = context.getBodyAsJson();
    InterHandler handlerManager = null;
    Future<JsonObject> operationResult = null;
    if (body == null) {
      operationResult = Future.failedFuture("Body can not be empty");
      handleAsyncResult(context, operationResult);
      return;
    }
    if (body.getString("dbId") == null || body.getString("dbId").equals("")) {
      body.put("dbId", HandlerCode.STARTER_DB);
      handlerManager = HandlerManager.getInstance().getHandler(HandlerCode.STARTER_DB);
    } else {
      handlerManager = HandlerManager.getInstance().getHandler(body.getInteger("dbId"));
    }
    operationResult = handlerManager.dbHandler(apiName, operation, body);
    handleAsyncResult(context, operationResult);
  }

  void handleRedisApi(RoutingContext context) {
    String apiName = context.pathParam("apiName");
    String operation = context.pathParam("key");
    JsonObject body = context.getBodyAsJson();
    InterHandler handlerManager = HandlerManager.getInstance().getHandler(HandlerCode.REDIS_DB);
    Future<Response> operationResult = null;
    operationResult = handlerManager.redisHandler(apiName, operation, body);
    handleAsyncResult(context, operationResult);
  }

  @Override
  public void stop() {
    httpServer.close();
    logger.error(" AppLogin Server stop ------");
  }
}
