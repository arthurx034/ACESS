package com.alana.util;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple OSRM helper to fetch a driving route as GeoJSON coordinates.
 * Callback runs on the background thread (OkHttp). Caller must switch to UI thread when updating UI.
 */
public class OSRMUtil {
    private static final OkHttpClient client = new OkHttpClient();

    public interface RouteCallback {
        void onSuccess(double distanceKm, int durationMin, List<GeoPoint> geometry);
        void onFailure(String errorMessage);
    }

    /**
     * Request route from OSRM public server.
     *
     * @param fromLat start latitude
     * @param fromLon start longitude
     * @param toLat   destination latitude
     * @param toLon   destination longitude
     * @param cb      callback invoked with parsed route
     */
    public static void fetchRoute(double fromLat, double fromLon,
                                  double toLat, double toLon,
                                  RouteCallback cb) {

        // OSRM expects lon,lat pairs in the path string; request geojson geometry for easy parsing
        String url = String.format(
                "https://router.project-osrm.org/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson",
                fromLon, fromLat, toLon, toLat);

        Request req = new Request.Builder()
                .url(url)
                .header("User-Agent", "AcessApp/1.0")
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                cb.onFailure(e.getMessage() == null ? "Network error" : e.getMessage());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    cb.onFailure("OSRM returned empty or error response");
                    return;
                }

                try {
                    String body = response.body().string();
                    JSONObject root = new JSONObject(body);
                    JSONArray routes = root.optJSONArray("routes");
                    if (routes == null || routes.length() == 0) {
                        cb.onFailure("No routes found");
                        return;
                    }

                    JSONObject route = routes.getJSONObject(0);
                    double distanceMeters = route.optDouble("distance", 0.0);
                    double durationSeconds = route.optDouble("duration", 0.0);

                    double distanceKm = distanceMeters / 1000.0;
                    int durationMin = (int) Math.round(durationSeconds / 60.0);

                    List<GeoPoint> points = new ArrayList<>();
                    // geometry is a GeoJSON object: { "type":"LineString", "coordinates":[ [lon,lat], ... ] }
                    JSONObject geometry = route.optJSONObject("geometry");
                    if (geometry != null) {
                        JSONArray coords = geometry.optJSONArray("coordinates");
                        if (coords != null) {
                            for (int i = 0; i < coords.length(); i++) {
                                JSONArray pair = coords.getJSONArray(i);
                                double lon = pair.optDouble(0, 0.0);
                                double lat = pair.optDouble(1, 0.0);
                                points.add(new GeoPoint(lat, lon));
                            }
                        }
                    }

                    cb.onSuccess(distanceKm, durationMin, points);
                } catch (Exception ex) {
                    cb.onFailure(ex.getMessage() == null ? "Parsing error" : ex.getMessage());
                }
            }
        });
    }
}
