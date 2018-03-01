package com.bugsnag.android;

public class BugsnagException extends Throwable {
    private String name;

    public BugsnagException(String name, String message, StackTraceElement[] frames) {
        super(message);
        super.setStackTrace(frames);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
