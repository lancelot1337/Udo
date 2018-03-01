package com.superking.iap;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LevelEndEvent;
import com.crashlytics.android.answers.LevelStartEvent;
import com.crashlytics.android.answers.LoginEvent;

public class FabricAnswersInterface {
    public static void login(String method) {
        Answers.getInstance().logLogin(new LoginEvent().putMethod(method).putSuccess(true));
    }

    public static void startLevel(String levelName) {
        Answers.getInstance().logLevelStart(new LevelStartEvent().putLevelName(levelName));
    }

    public static void endLevel(String levelName, int points) {
        Answers.getInstance().logLevelEnd(new LevelEndEvent().putLevelName(levelName).putScore(Integer.valueOf(points)).putSuccess(true));
    }

    public static void logEvent(String eventName, String param1, int param2) {
        CustomEvent event = new CustomEvent(eventName);
        event.putCustomAttribute("param1", param1);
        event.putCustomAttribute("param2", Integer.valueOf(param2));
        Answers.getInstance().logCustom(event);
    }
}
