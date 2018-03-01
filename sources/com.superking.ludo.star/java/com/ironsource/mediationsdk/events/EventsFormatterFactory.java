package com.ironsource.mediationsdk.events;

import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;

class EventsFormatterFactory {
    public static final int AD_UNIT_INTERSTITIAL = 2;
    public static final int AD_UNIT_REWARDED_VIDEO = 3;
    public static final String TYPE_IRONBEAST = "ironbeast";
    public static final String TYPE_OUTCOME = "outcome";

    EventsFormatterFactory() {
    }

    public static AbstractEventsFormatter getFormatter(String type, int adUnit) {
        if (TYPE_IRONBEAST.equals(type)) {
            return new IronbeastEventsFormatter(adUnit);
        }
        if (TYPE_OUTCOME.equals(type)) {
            return new OutcomeEventsFormatter(adUnit);
        }
        if (adUnit == AD_UNIT_INTERSTITIAL) {
            return new IronbeastEventsFormatter(adUnit);
        }
        if (adUnit == AD_UNIT_REWARDED_VIDEO) {
            return new OutcomeEventsFormatter(adUnit);
        }
        IronSourceLoggerManager.getLogger().log(IronSourceTag.NATIVE, "EventsFormatterFactory failed to instantiate a formatter (type: " + type + ", adUnit: " + adUnit + ")", AD_UNIT_INTERSTITIAL);
        return null;
    }
}
