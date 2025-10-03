package com.alana.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alana.R;
import com.alana.adapter.RideAdapter;
import com.alana.db.AppDatabase;
import com.alana.db.Ride;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class DriverActivity extends AppCompatActivity {

    private ListView listRequests;
    private Button btnAccept;
    private List<Ride> listaCorridas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        // Inicializa elementos da UI
        listRequests = findViewById(R.id.list_requests);
        btnAccept = findViewById(R.id.btn_accept_ride);

        // Carrega corridas do banco
        carregarCorridas();

        // Botão de aceitar corrida
        btnAccept.setOnClickListener(v -> {
            int position = listRequests.getCheckedItemPosition();
            if (position != ListView.INVALID_POSITION && position < listaCorridas.size()) {
                Ride rideSelecionada = listaCorridas.get(position);

                // Aqui você pode atualizar no banco, ex:
                // AppDatabase.getInstance(this).rideDao().updateStatus(rideSelecionada.id, "aceita");

                Toast.makeText(this, "Corrida aceita para: " + rideSelecionada.destino, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Selecione uma corrida primeiro!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarCorridas() {
        Executors.newSingleThreadExecutor().execute(() -> {
            listaCorridas = AppDatabase.getInstance(this).rideDao().getAllRides();
            runOnUiThread(() -> {
                if (listaCorridas.isEmpty()) {
                    Toast.makeText(this, "Nenhuma corrida encontrada", Toast.LENGTH_SHORT).show();
                } else {
                    RideAdapter adapter = new RideAdapter(this, listaCorridas);
                    listRequests.setAdapter(adapter);
                }
            });
        });
    }
}
