package com.example.starter.message.cs;

import com.example.starter.message.imp.AbstractUpMessage;

public class DemoRequest extends AbstractUpMessage {
    public int dbId;

    @Override
    protected void decodeBody() {
      dbId = bodyData.getInteger("dbId",0);
    }
}
