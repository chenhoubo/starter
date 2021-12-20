package com.example.starter.message;

import com.example.starter.constants.HttpStatus;
import com.example.starter.message.imp.AbstractDownMessage;

public class NetDownError extends AbstractDownMessage {
    public NetDownError(short requestId, HttpStatus status){
        this.dbId = requestId;
        this.resultCode = status.code();
    }

    @Override
    protected void encodeBody() {

    }
}
