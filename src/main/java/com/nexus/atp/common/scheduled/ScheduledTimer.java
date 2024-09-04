package com.nexus.atp.common.scheduled;

import java.time.LocalTime;

public interface ScheduledTimer {
    void addScheduledCallback(LocalTime callbackTime, Runnable runnable);
}
