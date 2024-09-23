package com.nexus.atp.common.scheduled;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public interface ScheduledTimer {
    void addScheduledCallback(LocalTime callbackTime, Runnable runnable);
    void addScheduledSubscription(LocalTime scheduledStart,
                                  long callbackPeriod, TimeUnit periodUnit,
                                  Runnable runnable);
}
