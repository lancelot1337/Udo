package com.bugsnag.android;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.WeakHashMap;

class ExceptionHandler implements UncaughtExceptionHandler {
    final WeakHashMap<Client, Boolean> clientMap = new WeakHashMap();
    private final UncaughtExceptionHandler originalHandler;

    static void enable(Client client) {
        ExceptionHandler bugsnagHandler;
        UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (currentHandler instanceof ExceptionHandler) {
            bugsnagHandler = (ExceptionHandler) currentHandler;
        } else {
            bugsnagHandler = new ExceptionHandler(currentHandler);
            Thread.setDefaultUncaughtExceptionHandler(bugsnagHandler);
        }
        bugsnagHandler.clientMap.put(client, Boolean.valueOf(true));
    }

    static void disable(Client client) {
        UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (currentHandler instanceof ExceptionHandler) {
            ExceptionHandler bugsnagHandler = (ExceptionHandler) currentHandler;
            bugsnagHandler.clientMap.remove(client);
            if (bugsnagHandler.clientMap.size() == 0) {
                Thread.setDefaultUncaughtExceptionHandler(bugsnagHandler.originalHandler);
            }
        }
    }

    public ExceptionHandler(UncaughtExceptionHandler originalHandler) {
        this.originalHandler = originalHandler;
    }

    public void uncaughtException(Thread t, Throwable e) {
        for (Client client : this.clientMap.keySet()) {
            client.cacheAndNotify(e, Severity.ERROR);
        }
        if (this.originalHandler != null) {
            this.originalHandler.uncaughtException(t, e);
            return;
        }
        System.err.printf("Exception in thread \"%s\" ", new Object[]{t.getName()});
        e.printStackTrace(System.err);
    }
}
