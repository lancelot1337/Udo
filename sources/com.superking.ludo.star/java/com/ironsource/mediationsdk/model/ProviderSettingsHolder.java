package com.ironsource.mediationsdk.model;

import java.util.ArrayList;
import java.util.Iterator;

public class ProviderSettingsHolder {
    private static ProviderSettingsHolder mInstance;
    private ArrayList<ProviderSettings> mProviderSettingsArrayList = new ArrayList();

    public static synchronized ProviderSettingsHolder getProviderSettingsHolder() {
        ProviderSettingsHolder providerSettingsHolder;
        synchronized (ProviderSettingsHolder.class) {
            if (mInstance == null) {
                mInstance = new ProviderSettingsHolder();
            }
            providerSettingsHolder = mInstance;
        }
        return providerSettingsHolder;
    }

    private ProviderSettingsHolder() {
    }

    public void addProviderSettings(ProviderSettings providerSettings) {
        if (providerSettings != null) {
            this.mProviderSettingsArrayList.add(providerSettings);
        }
    }

    public ProviderSettings getProviderSettings(String providerName) {
        Iterator it = this.mProviderSettingsArrayList.iterator();
        while (it.hasNext()) {
            ProviderSettings providerSettings = (ProviderSettings) it.next();
            if (providerSettings.getProviderName().equals(providerName)) {
                return providerSettings;
            }
        }
        ProviderSettings ps = new ProviderSettings(providerName);
        addProviderSettings(ps);
        return ps;
    }

    public boolean containsProviderSettings(String providerName) {
        Iterator it = this.mProviderSettingsArrayList.iterator();
        while (it.hasNext()) {
            if (((ProviderSettings) it.next()).getProviderName().equals(providerName)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<ProviderSettings> getProviderSettingsArrayList() {
        return this.mProviderSettingsArrayList;
    }
}
