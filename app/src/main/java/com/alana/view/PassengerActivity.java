package com.alana.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.alana.R;
import com.alana.db.Ride;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PassengerActivity extends FragmentActivity {

    private MapView map;
    private TextView tvDestino, tvDistancia, tvTempo, tvPreco;
    private Button btnSolicitar;
    private FirebaseFirestore firestore;

    // exemplo fixo (Uberlândia)
    private final double origemLat = -18.9185;
    private final double origemLon = -48.2772;
    private final double destinoLat = -18.9269;
    private final double destinoLon = -48.2865;

    private double lastPreco = 0;
    private double lastDistanciaKm = 0;
    private int lastTempoMin = 0;

    private ActivityResultLauncher<String> requestLocationPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // osmdroid config: user agent + prefs
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_passenger);

        map = findViewById(R.id.map);
        tvDestino = findViewById(R.id.tvDestino);
        tvDistancia = findViewById(R.id.tvDistancia);
        tvTempo = findViewById(R.id.tvTempo);
        tvPreco = findViewById(R.id.tvPreco);
        btnSolicitar = findViewById(R.id.btnSolicitar);
        firestore = FirebaseFirestore.getInstance();

        // setup map
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        map.getController().setCenter(new GeoPoint(origemLat, origemLon));

        // marcadores iniciais
        addMarker(origemLat, origemLon, "Você");
        addMarker(destinoLat, destinoLon, "Destino");

        tvDestino.setText("Destino: Shopping Uberlândia");

        // Permissão runtime (location)
        requestLocationPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    // apenas continuar (mapa já funciona sem localização)
                }
        );

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // já calcular rota na abertura
        calcularRotaECalcularPreco(origemLat, origemLon, destinoLat, destinoLon);

        btnSolicitar.setOnClickListener(v -> {
            if (lastPreco <= 0) {
                Toast.makeText(this, "Aguardando cálculo da rota...", Toast.LENGTH_SHORT).show();
                return;
            }
            btnSolicitar.setEnabled(false);
            salvarCorridaNoFirestore("Shopping Uberlândia", lastPreco, lastDistanciaKm, lastTempoMin);
        });
    }

    private void addMarker(double lat, double lon, String title) {
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(lat, lon));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        map.getOverlays().add(marker);
        map.invalidate();
    }

    private void calcularRotaECalcularPreco(double oLat, double oLon, double dLat, double dLon) {
        // OSRM exige lon,lat na URL
        String url = "https://router.project-osrm.org/route/v1/driving/"
                + oLon + "," + oLat + ";" + dLon + "," + dLat
                + "?overview=full&geometries=polyline";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PassengerActivity.this, "Erro ao buscar rota (OSRM)", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> Toast.makeText(PassengerActivity.this, "Resposta inválida do OSRM", Toast.LENGTH_SHORT).show());
                    return;
                }
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    JSONArray routes = json.getJSONArray("routes");
                    if (routes.length() == 0) {
                        runOnUiThread(() -> Toast.makeText(PassengerActivity.this, "Nenhuma rota encontrada", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    JSONObject route = routes.getJSONObject(0);
                    double distanceMeters = route.getDouble("distance"); // em metros
                    double durationSeconds = route.getDouble("duration"); // em segundos
                    String geometry = route.getString("geometry"); // polyline encoded

                    List<GeoPoint> points = decodePolylineToGeoPoints(geometry);

                    double km = distanceMeters / 1000.0;
                    int minutos = (int)Math.round(durationSeconds / 60.0);
                    double preco = calcularPrecoExemplo(km, minutos);

                    // atualizar UI
                    runOnUiThread(() -> {
                        // remover overlays antigos (exceto marcadores iniciais se quiser)
                        map.getOverlays().clear();
                        addMarker(oLat, oLon, "Você");
                        addMarker(dLat, dLon, "Destino");

                        Polyline line = new Polyline();
                        line.setPoints(points);
                        line.setWidth(8.0f);
                        map.getOverlayManager().add(line);
                        map.invalidate();

                        tvDistancia.setText(String.format("Distância: %.2f km", km));
                        tvTempo.setText("Tempo estimado: " + minutos + " min");
                        tvPreco.setText(String.format("Preço estimado: R$ %.2f", preco));

                        lastPreco = preco;
                        lastDistanciaKm = km;
                        lastTempoMin = minutos;
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(PassengerActivity.this, "Erro ao processar rota", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // Decodifica polyline (formato Google/OSRM, precision=5)
    private List<GeoPoint> decodePolylineToGeoPoints(String encoded) {
        List<GeoPoint> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            double latitude = lat / 1E5;
            double longitude = lng / 1E5;
            poly.add(new GeoPoint(latitude, longitude));
        }
        return poly;
    }

    private double calcularPrecoExemplo(double km, int minutos) {
        double bandeirada = 4.50;
        double porKm = 1.2;
        double porMin = 0.5;
        return bandeirada + (km * porKm) + (minutos * porMin);
    }

    private void salvarCorridaNoFirestore(String destinoStr, double preco, double distanciaKm, int tempoMin) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("destino", destinoStr);
        map.put("preco", preco);
        map.put("distancia", distanciaKm);
        map.put("tempo", tempoMin);
        map.put("status", "aguardando");
        map.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        firestore.collection("rides")
                .add(map)
                .addOnSuccessListener(docRef -> runOnUiThread(() -> {
                    Toast.makeText(PassengerActivity.this, "Corrida solicitada (id: " + docRef.getId() + ")", Toast.LENGTH_SHORT).show();
                    btnSolicitar.setEnabled(true);
                }))
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    Toast.makeText(PassengerActivity.this, "Erro ao salvar corrida", Toast.LENGTH_SHORT).show();
                    btnSolicitar.setEnabled(true);
                }));
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}
