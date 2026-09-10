package com.elfmcys.ysm.network.fabric;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 由 ConnectionAttributeMixin 实现，替代 26.1.2 中移除的 Connection.channel().attr()
 */
public interface YsmConnectionData {
    ConcurrentHashMap<String, Object> ysm$attributes();
}
