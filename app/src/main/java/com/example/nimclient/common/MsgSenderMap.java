package com.example.nimclient.common;

import com.example.nimclient.service.MsgSender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MsgSenderMap {
    /**
     * map of MsgSender
     */
    public static final Map<String, MsgSender> map = new ConcurrentHashMap(4);

    public static void addSender(String key, MsgSender sender) {
        map.put(key, sender);
    }

    public static MsgSender getSender(String key) {
        return map.get(key);
    }

    public static void remove(String key) {
        map.remove(key);
    }
}
