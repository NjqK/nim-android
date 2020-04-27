package com.example.nimclient.service;

public interface SpecialReconnect {
    /**
     * 获取新的可用节点然后重连
     */
    void searchNewAvailableNodeAndConnect();

    /**
     * 连接特定的服务器
     */
    void connectSpecialServer(String host, String port);
}
