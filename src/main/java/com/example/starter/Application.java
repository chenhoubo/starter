package com.example.starter;

import com.example.starter.config.Configure;
import com.example.starter.verticle.HttpServerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    VertxOptions vertxOptions = new VertxOptions()
      .setBlockedThreadCheckInterval(10000L)
      .setWorkerPoolSize(8)
      .setInternalBlockingPoolSize(8);
    Vertx vertx = Vertx.vertx(vertxOptions);
    try {
      extracted();
      Configure.getInstance().init(vertx);
      vertx.deployVerticle(HttpServerVerticle.class.getName(),new DeploymentOptions().setInstances(VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE))
        .onSuccess(h -> logger.info("服务端部署成功----"))
        .onFailure(err -> {
          err.printStackTrace();
          logger.error("服务端部署失败---" + err.getCause().getMessage());
        });
//      vertx.deployVerticle(HttpServerVerticle.class.getName(),
//        new DeploymentOptions().setInstances(VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE), res -> {
//        if (res.succeeded()) {
//          logger.warn("服务端部署成功----");
//        } else {
//          logger.error("服务端部署失败---" + res.cause());
//        }
//      });
    } catch (Exception ex) {
      logger.error("Init Services fail", ex);
      System.err.println("Init Services fail,exit");
      System.out.flush();
      System.err.flush();
      System.exit(1);
    }
  }

  private static void extracted() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        Configure.getInstance().closeResource();
        logger.info("关服-----结束...");
      }
    });
  }
}
