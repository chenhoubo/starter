package com.example.starter.message;

import com.example.starter.constants.HandlerCode;
import com.example.starter.message.cs.DemoRequest;
import com.example.starter.message.imp.IMessageRecognizer;
import com.example.starter.message.imp.MessageFactory;

public class MessageRecognizer implements IMessageRecognizer {
    @Override
    public MessageFactory recognize(int messageId) {
        switch (messageId){
            case HandlerCode.DEMO_V1: return new DemoRequest();
            default:return new DemoRequest();
        }
    }
}
