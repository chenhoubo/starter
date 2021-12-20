package com.example.starter.config;

public class RuntimeConfig {

  private Integer serverPort;

  public RuntimeConfig() {
    this.serverPort = 8888;
  }

  public int getServerPort() {
    return serverPort;
  }

  public void setServerPort(Integer serverPort) {
    this.serverPort = serverPort;
  }
}

