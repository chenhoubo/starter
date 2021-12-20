package com.example.starter.db.dao;


import com.example.starter.config.MysqlConfig;
import com.example.starter.db.MySQLUtil;

public class DaoManager {
    private final MysqlConfig mysqlConfig;

    private final MySQLUtil mySQLPool;

    private PlayerDao playerDao;

    public DaoManager(MysqlConfig mysqlConfig, MySQLUtil mySQLPool){
        this.mysqlConfig = mysqlConfig;
        this.mySQLPool = mySQLPool;
        init();
    }

    private void init(){
        playerDao = new PlayerDao(mySQLPool);
    }

    public PlayerDao getPlayerDao(){return playerDao;}
}
