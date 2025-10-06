package com.alana.view;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alana.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DriverActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private ArrayList<String> display = new ArrayList<>();
    private ArrayList<String> docIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        firestore = FirebaseFirestore.getInstance();
        listView = findViewById(R.id.list_requests);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, display);
        listView.setAdapter(adapter);

        // Ouve corridas aguardando
        firestore.collection("rides")
                .whereEqualTo("status", "aguardando")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) return;
                    if (snap != null) {
                        for (DocumentChange dc : snap.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Map<String, Object> data = dc.getDocument().getData();
                                    String destino = data.get("destino") != null ? data.get("destino").toString() : "Destino";
                                    String preco = data.get("preco") != null ? data.get("preco").toString() : "0";
                                    display.add(destino + " - R$ " + preco);
                                    docIds.add(dc.getDocument().getId());
                                    adapter.notifyDataSetChanged();
                                    break;
                            }
                        }
                    }
                });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String docId = docIds.get(position);
            aceitarCorrida(docId);
        });
    }

    private void aceitarCorrida(String docId) {
        firestore.collection("rides").document(docId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && "aguardando".equals(doc.getString("status"))) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status", "aceita");
                        updates.put("driverId", "driver_dummy_1");

                        firestore.collection("rides")
                                .document(docId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> runOnUiThread(() ->
                                        Toast.makeText(DriverActivity.this, "Corrida aceita!", Toast.LENGTH_SHORT).show()))
                                .addOnFailureListener(e -> runOnUiThread(() ->
                                        Toast.makeText(DriverActivity.this, "Erro ao aceitar corrida", Toast.LENGTH_SHORT).show()));
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(DriverActivity.this, "Corrida já foi aceita por outro motorista", Toast.LENGTH_SHORT).show());
                    }
                });
    }
}
