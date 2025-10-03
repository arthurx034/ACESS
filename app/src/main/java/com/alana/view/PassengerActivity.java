package com.alana.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.alana.R;
import com.alana.db.AppDatabase;
import com.alana.db.Ride;
import com.alana.db.RideDao;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PassengerActivity extends FragmentActivity {

    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private TextView tvDestino, tvDistancia, tvTempo, tvPreco;
    private Button btnSolicitar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger); // layout com FrameLayout "map"

        // BottomSheet
        LinearLayout bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(300);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // UI
        tvDestino = findViewById(R.id.tvDestino);
        tvDistancia = findViewById(R.id.tvDistancia);
        tvTempo = findViewById(R.id.tvTempo);
        tvPreco = findViewById(R.id.tvPreco);
        btnSolicitar = findViewById(R.id.btnSolicitar);

        tvDestino.setText("Destino: Shopping Uberlândia");

        btnSolicitar.setOnClickListener(v -> {
            Toast.makeText(this, "Solicitando corrida...", Toast.LENGTH_SHORT).show();
            buscarInformacoesDaCorrida();
        });
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
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

                    double preco = obj.get("preco").getAsDouble();
                    double distancia = obj.get("distancia").getAsDouble();
                    int tempo = obj.get("tempo").getAsInt();

                    runOnUiThread(() -> {
                        tvPreco.setText("Preço estimado: R$ " + preco);
                        tvDistancia.setText("Distância: " + distancia + " km");
                        tvTempo.setText("Tempo estimado: " + tempo + " min");

                        salvarCorridaNoBanco("Shopping Uberlândia", preco, distancia, tempo);
                    });
                }
            }
        });
    }

    private void salvarCorridaNoBanco(String destino, double preco, double distancia, int tempo) {
        Executors.newSingleThreadExecutor().execute(() -> {
            RideDao rideDao = AppDatabase.getInstance(this).rideDao();
            Ride ride = new Ride(destino, preco, distancia, tempo);
            rideDao.insertRide(ride);
        });
    }
}
