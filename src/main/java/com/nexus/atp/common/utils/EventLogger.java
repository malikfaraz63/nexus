package com.nexus.atp.common.utils;

import com.nexus.atp.common.EngineTimeProvider;

public class EventLogger implements Logger {
    private static final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private final EngineTimeProvider engineTimeProvider;

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";

    public EventLogger(EngineTimeProvider engineTimeProvider) {
        this.engineTimeProvider = engineTimeProvider;
    }

    @Override
    public void info(String format, Object... args) {
        logWithColour(BLUE, (String.format(format, args)));
    }

    @Override
    public void info(String message) {
        logWithColour(BLUE, message);
    }

    @Override
    public void warn(String format, Object... args) {
        logWithColour(YELLOW, (String.format(format, args)));
    }

    @Override
    public void warn(String message) {
        logWithColour(YELLOW, message);
    }

    @Override
    public void error(String format, Object... args) {
        logWithColour(RED, (String.format(format, args)));
    }

    @Override
    public void error(String message) {
        logWithColour(RED, message);
    }

    private void logWithColour(String colour, String message) {
        String callingClassName = getCallerDetails();

        String[] messageLines = message.split("\n");
        String infoLine = engineTimeProvider.getEngineTime() + " - " + callingClassName + ": ";
        System.out.println(colour + infoLine + messageLines[0]);

        int offset = infoLine.length();

        for (int i = 1; i < messageLines.length; i++) {
            System.out.println(" ".repeat(offset) + messageLines[i]);
        }

        System.out.print(RESET);
    }

    private static String getCallerDetails() {
        return walker.walk(frames -> frames
                .skip(3)
                .findFirst()
                .map(stackFrame -> stackFrame.getClassName() + "." + stackFrame.getMethodName() + "()")
                .orElse("unknown"));
    }
}
