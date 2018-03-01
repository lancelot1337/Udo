package com.ironsource.mediationsdk.model;

public class ApplicationConfigurations {
    private ApplicationLogger mLogger;

    public ApplicationConfigurations() {
        this.mLogger = new ApplicationLogger();
    }

    public ApplicationConfigurations(ApplicationLogger logger) {
        this.mLogger = logger;
    }

    public ApplicationLogger getLoggerConfigurations() {
        return this.mLogger;
    }
}
