package com.nexus.atp.application;

import com.nexus.atp.common.EngineTimeProvider;
import com.nexus.atp.common.scheduled.ScheduledTimer;

import java.time.Instant;
import java.util.Date;

public class ResourcesBuilder {
    private EngineTimeProvider timeProvider;

    public EngineTimeProvider getTimeProvider() {
        if (timeProvider == null) {
            timeProvider = () -> Date.from(Instant.now());
        }

        return timeProvider;
    }

    public ScheduledTimer getScheduledTimer() {
        throw new UnsupportedOperationException(); // TODO: implement scheduled timer
    }
}
