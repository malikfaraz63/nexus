package com.nexus.atp.common;

import java.time.ZoneId;
import java.util.Date;

public interface EngineTimeProvider {
    Date getEngineTime();
    ZoneId getTimeZone();
}
