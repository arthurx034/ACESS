package com.alana.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.alana.R;
import com.alana.util.OSRMUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;
import java.util.Locale;

/**
 * PassengerActivity (corrigido)
 * - mantém design atual (bottom sheet)
 * - pega localização atual (last known)
 * - chama OSRM via OSRMUtil.fetchRoute()
 * - desenha polyline, adiciona marcadores e faz auto-zoom
 */
public class PassengerActivity extends FragmentActivity {

    private MapView map;
    private TextView tvDestino, tvDistancia, tvTempo, tvPreco;
    private Button btnSolicitar;

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> requestLocationPermission;

    // destination (recebido via Intent)
    private double destinoLat = 0.0;
    private double destinoLon = 0.0;
    private String destinoNome = "Destino";

    // origin (obtido ou fallback)
    private double origemLat = -18.9185;
    private double origemLon = -48.2772;
    private String origemDisplay = "Minha localização";

    private double lastPreco = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // osmdroid config (necessário antes de usar MapView)
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_passenger);

        // Bind UI
        map = findViewById(R.id.map);
        tvDestino = findViewById(R.id.tvDestino);
        tvDistancia = findViewById(R.id.tvDistancia);
        tvTempo = findViewById(R.id.tvTempo);   // no layout atual tvTempo existe
        tvPreco = findViewById(R.id.tvPreco);
        btnSolicitar = findViewById(R.id.btnSolicitar);

        // Get destination from Intent
        destinoNome = getIntent().getStringExtra("destinoNome");
        destinoLat = getIntent().getDoubleExtra("destinoLat", destinoLat);
        destinoLon = getIntent().getDoubleExtra("destinoLon", destinoLon);
        if (destinoNome == null || destinoNome.isEmpty()) {
            destinoNome = String.format(Locale.getDefault(), "%.5f, %.5f", destinoLat, destinoLon);
        }

        // prepare map
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);

        // UI text
        tvDestino.setText("Destino: " + destinoNome);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // permission launcher
        requestLocationPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        obtainLocationAndDraw();
                    } else {
                        Toast.makeText(this, "Permissão de localização negada. Usando origem padrão.", Toast.LENGTH_SHORT).show();
                        // draw with fallback origin
                        drawRouteAndDisplay(origemLat, origemLon, destinoLat, destinoLon);
                    }
                });

        // Request permission or proceed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            obtainLocationAndDraw();
        }

        btnSolicitar.setOnClickListener(v -> {
            if (lastPreco <= 0.0) {
                Toast.makeText(this, "Aguardando cálculo da rota...", Toast.LENGTH_SHORT).show();
                return;
            }
            btnSolicitar.setEnabled(false);
            Toast.makeText(this, String.format(Locale.getDefault(), "Corrida solicitada. Preço: R$ %.2f", lastPreco), Toast.LENGTH_SHORT).show();
            btnSolicitar.setEnabled(true);
        });
    }

    private void obtainLocationAndDraw() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            origemLat = location.getLatitude();
                            origemLon = location.getLongitude();
                            origemDisplay = String.format(Locale.getDefault(), "Você (%.5f, %.5f)", origemLat, origemLon);
                        } else {
                            origemDisplay = String.format(Locale.getDefault(), "Fallback (%.5f, %.5f)", origemLat, origemLon);
                        }
                        // Update a UI (ETA or origin display can be shown in tvTempo temporarily)
                        runOnUiThread(() -> tvTempo.setText("Origem: " + origemDisplay));
                        drawRouteAndDisplay(origemLat, origemLon, destinoLat, destinoLon);
                    })
                    .addOnFailureListener(e -> {
                        origemDisplay = String.format(Locale.getDefault(), "Fallback (%.5f, %.5f)", origemLat, origemLon);
                        runOnUiThread(() -> tvTempo.setText("Origem: " + origemDisplay));
                        drawRouteAndDisplay(origemLat, origemLon, destinoLat, destinoLon);
                    });
        } catch (SecurityException se) {
            Toast.makeText(this, "Permissão de localização faltando", Toast.LENGTH_SHORT).show();
            drawRouteAndDisplay(origemLat, origemLon, destinoLat, destinoLon);
        }
    }

    private void drawRouteAndDisplay(double oLat, double oLon, double dLat, double dLon) {
        // Basic validation
        if (dLat == 0.0 && dLon == 0.0) {
            runOnUiThread(() -> Toast.makeText(this, "Coordenadas do destino inválidas", Toast.LENGTH_SHORT).show());
            return;
        }

        // clear overlays and add temporary markers
        runOnUiThread(() -> {
            map.getOverlays().clear();
            addCustomMarker(oLat, oLon, "Origem", android.R.drawable.ic_menu_mylocation);
            addCustomMarker(dLat, dLon, "Destino", android.R.drawable.ic_menu_mapmode);
            map.getController().setCenter(new GeoPoint(oLat, oLon));
        });

        // fetch route using OSRMUtil (background thread)
        OSRMUtil.fetchRoute(oLat, oLon, dLat, dLon, new OSRMUtil.RouteCallback() {
            @Override
            public void onSuccess(double distanceKm, int durationMin, List<GeoPoint> geometry) {
                final double price = calcularPrecoExemplo(distanceKm, durationMin);
                lastPreco = price;

                // compute bbox
                double minLat = Double.MAX_VALUE, minLon = Double.MAX_VALUE;
                double maxLat = -Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
                for (GeoPoint p : geometry) {
                    double lat = p.getLatitude();
                    double lon = p.getLongitude();
                    if (lat < minLat) minLat = lat;
                    if (lat > maxLat) maxLat = lat;
                    if (lon < minLon) minLon = lon;
                    if (lon > maxLon) maxLon = lon;
                }

                final double fMinLat = minLat, fMinLon = minLon, fMaxLat = maxLat, fMaxLon = maxLon;
                runOnUiThread(() -> {
                    map.getOverlays().clear();

                    // markers again
                    addCustomMarker(oLat, oLon, "Origem", android.R.drawable.ic_menu_mylocation);
                    addCustomMarker(dLat, dLon, "Destino", android.R.drawable.ic_menu_mapmode);

                    // polyline
                    Polyline line = new Polyline(map);
                    line.setPoints(geometry);
                    line.setWidth(8.0f);
                    line.getOutlinePaint().setColor(Color.BLACK);
                    line.getPaint().setColor(Color.parseColor("#143470"));
                    map.getOverlayManager().add(line);

                    // auto-zoom if geometry ok
                    if (!geometry.isEmpty() && fMinLat != Double.MAX_VALUE) {
                        BoundingBox bb = new BoundingBox(fMaxLat, fMaxLon, fMinLat, fMinLon);
                        map.zoomToBoundingBox(bb, true, 80);
                    } else {
                        map.getController().setCenter(new GeoPoint(oLat, oLon));
                    }

                    // update UI texts
                    tvDistancia.setText(String.format(Locale.getDefault(), "Distância: %.2f km", distanceKm));
                    tvTempo.setText(String.format(Locale.getDefault(), "Tempo estimado: %d min", durationMin));
                    tvPreco.setText(String.format(Locale.getDefault(), "Preço estimado: R$ %.2f", price));
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(PassengerActivity.this,
                        "Erro ao calcular rota: " + errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void addCustomMarker(double lat, double lon, String title, int androidDrawableId) {
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(lat, lon));
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        try {
            Drawable icon = ContextCompat.getDrawable(this, androidDrawableId);
            if (icon != null) marker.setIcon(icon);
        } catch (Exception ignored) { /* fallback to default icon */ }
        map.getOverlays().add(marker);
        map.invalidate();
    }

    private double calcularPrecoExemplo(double km, int minutos) {
        double bandeirada = 4.50;
        double porKm = 1.2;
        double porMin = 0.5;
        return bandeirada + (km * porKm) + (minutos * porMin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }
}
