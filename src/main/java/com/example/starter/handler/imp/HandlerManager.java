package com.example.starter.handler.imp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerManager {
    private final Map<Integer,InterHandler> handlers = new ConcurrentHashMap<>();

    private static HandlerManager ourInstance = new HandlerManager();

    public static HandlerManager getInstance() {
        if(ourInstance == null){
            synchronized (HandlerManager.class){
                ourInstance = new HandlerManager();
            }
        }
        return ourInstance;
    }

    public void addHandler(int dbId, InterHandler handler){
        InterHandler old = handlers.putIfAbsent(dbId,handler);
        if(old != null){
            throw new RuntimeException("handler repeat :"+dbId);
        }
    }

    public InterHandler getHandler(int code){
        return handlers.get(code);
    }
}
