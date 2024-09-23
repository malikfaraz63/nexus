package com.nexus.atp.common.utils;

public interface Logger {
    void info(String format, Object... args);
    void info(String message);

    void warn(String format, Object... args);
    void warn(String message);

    void error(String format, Object... args);
    void error(String message);
}
