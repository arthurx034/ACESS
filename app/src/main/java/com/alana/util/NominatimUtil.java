package com.alana.util;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NominatimUtil {

    private static final OkHttpClient client = new OkHttpClient();

    public interface GeocodeCallback {
        void onResult(double lat, double lon, String displayName);
        void onError(String message);
    }

    // Sets up autocomplete: keeps names, lats, lons lists in sync and updates adapter on UI thread.
    public static void setupAutoComplete(Context ctx, AutoCompleteTextView view,
                                         ArrayList<String> names, ArrayList<Double> lats, ArrayList<Double> lons) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_dropdown_item_1line, names);
        view.setAdapter(adapter);
        view.setThreshold(2);

        view.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s == null ? "" : s.toString();
                if (q.length() < 2) return;
                fetchSuggestions(q, names, lats, lons, adapter, view);
            }
        });
    }

    private static void fetchSuggestions(String q,
                                         ArrayList<String> names,
                                         ArrayList<Double> lats,
                                         ArrayList<Double> lons,
                                         ArrayAdapter<String> adapter,
                                         AutoCompleteTextView view) {
        try {
            String qq = URLEncoder.encode(q, "UTF-8");
            String url = "https://nominatim.openstreetmap.org/search?q=" + qq + "&format=json&addressdetails=1&limit=6";
            Request req = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "AcessApp/1.0 (contact@example.com)")
                    .build();
            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    // ignore network errors for suggestions (UI may show nothing)
                }
                @Override public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) return;
                    try {
                        String body = response.body().string();
                        JSONArray arr = new JSONArray(body);
                        names.clear();
                        lats.clear();
                        lons.clear();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            String display = o.optString("display_name", "");
                            double lat = o.optDouble("lat", 0.0);
                            double lon = o.optDouble("lon", 0.0);
                            names.add(display);
                            lats.add(lat);
                            lons.add(lon);
                        }
                        // update adapter on UI thread
                        view.post(() -> {
                            adapter.notifyDataSetChanged();
                            if (!names.isEmpty()) view.showDropDown();
                        });
                    } catch (Exception ignored) {}
                }
            });
        } catch (Exception ignored) {}
    }

    // Geocode single address -> first result
    public static void geocode(String address, GeocodeCallback cb) {
        try {
            String q = URLEncoder.encode(address, "UTF-8");
            String url = "https://nominatim.openstreetmap.org/search?q=" + q + "&format=json&limit=1";
            Request req = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "AcessApp/1.0 (contact@example.com)")
                    .build();
            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) { cb.onError(e.getMessage()); }
                @Override public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) { cb.onError("Nominatim error"); return; }
                    try {
                        String body = response.body().string();
                        JSONArray arr = new JSONArray(body);
                        if (arr.length() == 0) { cb.onError("No results"); return; }
                        JSONObject o = arr.getJSONObject(0);
                        double lat = o.optDouble("lat", 0.0);
                        double lon = o.optDouble("lon", 0.0);
                        String display = o.optString("display_name", "");
                        cb.onResult(lat, lon, display);
                    } catch (Exception e) { cb.onError(e.getMessage()); }
                }
            });
        } catch (Exception e) { cb.onError(e.getMessage()); }
    }

    // Reverse geocode lat/lon
    public static void reverseGeocode(double lat, double lon, GeocodeCallback cb) {
        try {
            String url = "https://nominatim.openstreetmap.org/reverse?lat=" + lat + "&lon=" + lon + "&format=json";
            Request req = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "AcessApp/1.0 (contact@example.com)")
                    .build();
            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) { cb.onError(e.getMessage()); }
                @Override public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) { cb.onError("Reverse failed"); return; }
                    try {
                        String body = response.body().string();
                        JSONObject o = new JSONObject(body);
                        String display = o.optString("display_name", "");
                        cb.onResult(lat, lon, display);
                    } catch (Exception e) { cb.onError(e.getMessage()); }
                }
            });
        } catch (Exception e) { cb.onError(e.getMessage()); }
    }
}
