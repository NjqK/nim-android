package com.example.nimclient.service;

public interface FailedReconnect {
    /**
     * 获取新的可用节点然后重连
     */
    void searchNewAvailableNodeAndConnect();
}
