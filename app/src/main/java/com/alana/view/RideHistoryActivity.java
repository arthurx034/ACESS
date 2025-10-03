package com.alana.view;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alana.R;
import com.alana.adapter.RideAdapter;
import com.alana.db.AppDatabase;
import com.alana.db.Ride;

import java.util.List;
import java.util.concurrent.Executors;

public class RideHistoryActivity extends AppCompatActivity {

    private ListView listViewHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        listViewHistory = findViewById(R.id.listViewHistory);

        carregarHistorico();
    }

    private void carregarHistorico() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Ride> rides = AppDatabase.getInstance(this).rideDao().getAllRides();
            runOnUiThread(() -> {
                if (rides.isEmpty()) {
                    Toast.makeText(this, "Nenhuma corrida realizada", Toast.LENGTH_SHORT).show();
                } else {
                    RideAdapter adapter = new RideAdapter(this, rides);
                    listViewHistory.setAdapter(adapter);
                }
            });
        });
    }
}
