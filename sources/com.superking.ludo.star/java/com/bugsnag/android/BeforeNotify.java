package com.bugsnag.android;

public interface BeforeNotify {
    boolean run(Error error);
}
