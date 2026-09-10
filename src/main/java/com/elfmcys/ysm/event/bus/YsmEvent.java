package com.elfmcys.ysm.event.bus;

/**
 * 事件基类，对齐 Forge Event 的取消语义
 */
public abstract class YsmEvent {
    private boolean canceled;

    public boolean isCancelable() {
        return getClass().isAnnotationPresent(YsmCancelable.class);
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        if (!isCancelable()) {
            throw new UnsupportedOperationException(
                    "Attempted to cancel a non-cancelable event: " + getClass().getName());
        }
        this.canceled = canceled;
    }
}
