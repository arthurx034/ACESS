package com.alana;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button callRideButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa o SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Erro ao carregar o mapa!", Toast.LENGTH_SHORT).show();
        }

        // Configura o botão "Chamar Veículo"
        callRideButton = findViewById(R.id.btn_call_ride);
        callRideButton.setOnClickListener(v ->
                Toast.makeText(MainActivity.this, "Corrida solicitada!", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Centraliza o mapa em São Paulo com marcador
        LatLng sp = new LatLng(-23.5505, -46.6333);
        mMap.addMarker(new MarkerOptions().position(sp).title("Você está aqui"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sp, 12));

        // Aqui você pode adicionar mais configurações do mapa, se necessário
    }
}
