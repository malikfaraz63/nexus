package com.nexus.atp.application;

import com.nexus.atp.common.EngineTimeProvider;
import com.nexus.atp.common.scheduled.ScheduledTimer;

import java.time.ZoneId;
import java.util.Date;

public class ResourcesBuilder {
    private EngineTimeProvider timeProvider;

    public EngineTimeProvider getTimeProvider() {
        if (timeProvider == null) {
            timeProvider = new EngineTimeProvider() {
                @Override
                public Date getEngineTime() {
                    return new Date();
                }

                @Override
                public ZoneId getTimeZone() {
                    return ZoneId.systemDefault();
                }
            };
        }

        return timeProvider;
    }

    public ScheduledTimer getScheduledTimer() {
        throw new UnsupportedOperationException(); // TODO: implement scheduled timer
    }
}
