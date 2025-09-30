package com.alana;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private TextView tvDestino, tvDistancia, tvTempo, tvPreco;
    private Button btnSolicitar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        LinearLayout bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(300);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        tvDestino = findViewById(R.id.tvDestino);
        tvDistancia = findViewById(R.id.tvDistancia);
        tvTempo = findViewById(R.id.tvTempo);
        tvPreco = findViewById(R.id.tvPreco);
        btnSolicitar = findViewById(R.id.btnSolicitar);

        tvDestino.setText("Destino: Shopping Uberlândia");

        btnSolicitar.setOnClickListener(v -> {
            Toast.makeText(this, "Viagem solicitada!", Toast.LENGTH_SHORT).show();
            buscarInformacoesDaCorrida();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng origem = new LatLng(-18.9146, -48.2755);
        LatLng destino = new LatLng(-18.9185, -48.2772);

        mMap.addMarker(new MarkerOptions().position(origem).title("Você está aqui"));
        mMap.addMarker(new MarkerOptions().position(destino).title("Destino"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origem, 14));
    }

    private void buscarInformacoesDaCorrida() {
        OkHttpClient client = new OkHttpClient();

        // Simulação de API fictícia
        String url = "https://api.exemplo.com/corrida?origem=Uberlandia&destino=Shopping";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(PassengerActivity.this, "Erro ao buscar dados da corrida", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

                    double preco = obj.get("preco").getAsDouble();
                    double distancia = obj.get("distancia").getAsDouble();
                    int tempo = obj.get("tempo").getAsInt();

                    runOnUiThread(() -> {
                        tvPreco.setText("Preço estimado: R$ " + preco);
                        tvDistancia.setText("Distância: " + distancia + " km");
                        tvTempo.setText("Tempo estimado: " + tempo + " min");
                    });
                }
            }
        });
    }
}
