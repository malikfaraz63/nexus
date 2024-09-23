package com.nexus.atp.common.scheduled;

import com.nexus.atp.common.EngineTimeProvider;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadScheduler implements ScheduledTimer {
    private static final int MAX_THREADS = 16;

    private final EngineTimeProvider engineTimeProvider;
    private final ScheduledExecutorService scheduler;

    private final AtomicInteger availableThreads;

    public ThreadScheduler(EngineTimeProvider engineTimeProvider) {
        this.engineTimeProvider = engineTimeProvider;
        this.scheduler = Executors.newScheduledThreadPool(MAX_THREADS);
        this.availableThreads = new AtomicInteger(MAX_THREADS);
    }

    private LocalTime getCurrentTime() {
        return engineTimeProvider
                .getEngineTime()
                .toInstant()
                .atZone(engineTimeProvider.getTimeZone())
                .toLocalTime();
    }

    @Override
    public void addScheduledCallback(LocalTime callbackTime, Runnable runnable) {
        LocalTime currentTime = getCurrentTime();

        if (currentTime.isAfter(callbackTime)) {
            throw new IllegalArgumentException("callback time must be after current time");
        }

        if (availableThreads.get() == 0) {
            throw new IllegalStateException("No available threads for scheduling");
        }

        availableThreads.decrementAndGet();
        scheduler.schedule(() -> {
            runnable.run();
            availableThreads.incrementAndGet();
        }, currentTime.until(callbackTime, ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
    }

    @Override
    public void addScheduledSubscription(LocalTime scheduledStart, long callbackPeriod, TimeUnit periodUnit, Runnable runnable) {
        LocalTime currentTime = getCurrentTime();

        if (currentTime.isAfter(scheduledStart)) {
            throw new IllegalArgumentException("callback time must be after current time");
        }

        if (availableThreads.get() == 0) {
            throw new IllegalStateException("No available threads for scheduling");
        }

        availableThreads.decrementAndGet();
        scheduler.scheduleAtFixedRate(runnable, currentTime.until(scheduledStart, ChronoUnit.MILLIS), periodUnit.toMillis(callbackPeriod), TimeUnit.MILLISECONDS);
    }
}
