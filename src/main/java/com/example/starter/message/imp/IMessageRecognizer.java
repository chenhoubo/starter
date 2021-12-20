package com.example.starter.message.imp;

/***
 * 根据mId获取不同的解码器
 * ***/
public interface IMessageRecognizer {
    MessageFactory recognize(int messageId);
}
