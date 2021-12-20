package com.example.starter.message.sc;

import com.example.starter.message.imp.AbstractDownMessage;

public class DemoResponse extends AbstractDownMessage {
    private String name;
    private String info;

    public DemoResponse(int dbId,String name,String info){
      this.dbId = dbId;
      this.name = name;
      this.info = info;
    }

    @Override
    protected void encodeBody() {
        bodyData.put("name",name);
        bodyData.put("info",info);
    }
}
