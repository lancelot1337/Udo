package com.ironsource.environment;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import com.google.firebase.analytics.FirebaseAnalytics.Param;

public class LocationService {
    public static Location getLastLocation(Context context) {
        Location bestLocation = null;
        if (!ApplicationContext.isPermissionGranted(context, "android.permission.ACCESS_FINE_LOCATION")) {
            return null;
        }
        LocationManager locationManager = (LocationManager) context.getApplicationContext().getSystemService(Param.LOCATION);
        for (String provider : locationManager.getAllProviders()) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null && location.getTime() > Long.MIN_VALUE) {
                bestLocation = location;
            }
        }
        return bestLocation;
    }
}
