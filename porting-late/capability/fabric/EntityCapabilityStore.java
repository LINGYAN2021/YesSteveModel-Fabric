package com.elfmcys.ysm.capability.fabric;

import java.util.Map;

/**
 * 由 EntityCapabilityMixin 实现，持有实体上的 capability 实例表
 */
public interface EntityCapabilityStore {
    Map<YsmCapability<?>, Object> ysm$capabilityMap();
}
