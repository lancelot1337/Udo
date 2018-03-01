package com.ironsource.mediationsdk.model;

import io.branch.indexing.ContentDiscoveryManifest;

public enum PlacementCappingType {
    PER_DAY("d"),
    PER_HOUR(ContentDiscoveryManifest.HASH_MODE_KEY);
    
    public String value;

    private PlacementCappingType(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }
}
