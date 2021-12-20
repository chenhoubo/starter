package com.example.starter.config;

import com.example.starter.Application;
import com.example.starter.constants.HandlerCode;
import com.example.starter.db.MySQLUtil;
import com.example.starter.db.dao.DaoManager;
import com.example.starter.handler.MysqlDbHandler;
import com.example.starter.handler.imp.HandlerManager;
import com.example.starter.handler.imp.InterHandler;
import com.example.starter.redis.RedisPool;
import com.example.starter.redis.RedisUtil;
import com.example.starter.verticle.HttpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Configure extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(Configure.class);
  private static final Configure ourInstance = new Configure();
  private Vertx vertx;

  public Configure() {
  }

  public static Configure getInstance() {
    return ourInstance;
  }

  public MysqlConfig mysqlConfig;
  private List<MySQLUtil> mySQLPool;
  public Map<Integer, DaoManager> daoManager;

  private RedisConfig redisConfig;
  private RedisPool redisPool;
  public RedisUtil redisUtil;

  public HttpServerVerticle httpServerVerticle;

  @Override
  public void start() {
//    init();
    System.out.println("start");
  }

  public Configure init(Vertx vertx) {
    this.vertx = vertx;
    initHandler();
    loadConfig();
    initDb();
//    initRedis();
//    initHttp();
    return ourInstance;
  }

  private void initHandler() {
    MysqlDbHandler mysqlDbHandler = new MysqlDbHandler();
    HandlerManager.getInstance().addHandler(HandlerCode.STARTER_DB, mysqlDbHandler);
    HandlerManager.getInstance().addHandler(HandlerCode.TEST_DB, mysqlDbHandler);
    System.out.println("initHandler");
  }

  /**
   * 加载db和Redis配置文件
   */
  protected void loadConfig() {
    mysqlConfig = new MysqlConfig(vertx, "res/mysql.json");
    redisConfig = new RedisConfig(vertx, "res/redis.json");
    System.out.println("loadConfig");
  }

  protected void initDb() {
    if (mysqlConfig.configs.size() > 0) {
      mySQLPool = new ArrayList<>();
      daoManager = new HashMap<>();
    }
    System.out.println("数据库配置：" + mysqlConfig.configs);
    for (int i = 0; i < mysqlConfig.configs.size(); i++) {
      JsonObject config = mysqlConfig.configs.getJsonObject(i);
      MySQLUtil mySQLUtil = new MySQLUtil(vertx, 2, config);
      mySQLPool.add(mySQLUtil);
      daoManager.put(config.getInteger("type"), new DaoManager(mysqlConfig, mySQLUtil));
    }
  }

  /**
   * 初始化Redis
   */
  protected void initRedis() {
    redisPool = new RedisPool(vertx, redisConfig);
    System.out.println("redis配置："+redisConfig.getPoolSize());
    redisUtil = new RedisUtil(redisPool);
  }

  public void closeResource() {
    for (int i = 0; i < mySQLPool.size(); i++) {
      MySQLUtil sqlUtil = mySQLPool.get(i);
      if (sqlUtil != null) {
        sqlUtil.close();
      }
    }
    if (redisPool != null) {
      redisPool.close();
    }
  }

  protected void initHttp() {
    httpServerVerticle = new HttpServerVerticle();
    vertx.deployVerticle(httpServerVerticle);
    System.out.println("initHttp");
  }

  public DaoManager getDaoManager(int type) {
    return daoManager.get(type);
  }
  private Future<String> deployVerticle(AbstractVerticle verticle) {
    String className = verticle.getClass().getSimpleName();
    logger.info("Component {} Start!", className);
    return vertx.deployVerticle(verticle)
      .onSuccess(r -> logger.info("Component {} Start Complete!", className))
      .onFailure(e -> logger.error("Component {} Start Failed ! cause:{}", className, e.getMessage()));
  }
}
