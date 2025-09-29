package com.alana;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

public class DriverActivity extends AppCompatActivity {

    private ListView listRequests;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        listRequests = findViewById(R.id.list_requests);

        // Por enquanto, dados falsos
        String[] mockRequests = {"Passageiro 1 - Av Paulista", "Passageiro 2 - Rua Vergueiro"};
        listRequests.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mockRequests));
    }
}
