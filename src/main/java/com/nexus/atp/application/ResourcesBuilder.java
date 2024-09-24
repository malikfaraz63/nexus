package com.nexus.atp.application;

import com.nexus.atp.common.EngineTimeProvider;
import com.nexus.atp.common.scheduled.ScheduledTimer;
import com.nexus.atp.common.scheduled.ThreadScheduler;
import com.nexus.atp.common.utils.EventLogger;
import com.nexus.atp.common.utils.Logger;

import java.time.ZoneId;
import java.util.Date;

public class ResourcesBuilder {
    private EngineTimeProvider timeProvider;
    private ScheduledTimer scheduledTimer;
    private Logger logger;

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
        if (scheduledTimer == null) {
            scheduledTimer = new ThreadScheduler(getTimeProvider());
        }
        return scheduledTimer;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = new EventLogger(getTimeProvider());
        }
        return logger;
    }
}
