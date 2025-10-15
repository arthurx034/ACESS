package com.alana.util;

import android.app.Activity;
import android.location.Location;

import com.alana.util.NominatimUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationUtil {

    public interface LocationCallback {
        void onLocation(double lat, double lon, String address);
        void onError(String message);
    }

    // Fetch last known location and try reverse-geocode; callbacks are invoked from background threads,
    // caller should switch to UI thread if needed.
    public static void getCurrentLocation(Activity activity, LocationCallback cb) {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(activity);
        try {
            client.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    // reverse geocode asynchronously
                    NominatimUtil.reverseGeocode(lat, lon, new NominatimUtil.GeocodeCallback() {
                        @Override public void onResult(double latR, double lonR, String displayName) {
                            cb.onLocation(latR, lonR, displayName);
                        }
                        @Override public void onError(String message) {
                            // return coords with fallback display name
                            cb.onLocation(lat, lon, "Minha localização");
                        }
                    });
                } else {
                    cb.onError("Location unavailable");
                }
            }).addOnFailureListener(e -> cb.onError(e.getMessage()));
        } catch (SecurityException se) {
            cb.onError("Permission denied");
        }
    }
}
