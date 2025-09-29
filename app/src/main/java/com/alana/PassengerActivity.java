package com.alana;

import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnCallRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        btnCallRide = findViewById(R.id.btn_call_ride);
        btnCallRide.setOnClickListener(v ->
                Toast.makeText(this, "Viagem solicitada!", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sp = new LatLng(-23.5505, -46.6333);
        mMap.addMarker(new MarkerOptions().position(sp).title("Você está aqui"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sp, 12));
    }
}
