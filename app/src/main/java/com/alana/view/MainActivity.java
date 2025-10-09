package com.alana.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.alana.R;

public class MainActivity extends AppCompatActivity {

    private EditText etDestino;
    private Button btnMaisTarde, btnAACD, btnCasa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etDestino = findViewById(R.id.etDestino);
        btnMaisTarde = findViewById(R.id.btnMaisTarde);
        btnAACD = findViewById(R.id.btnAACD);
        btnCasa = findViewById(R.id.btnCasa);

        btnMaisTarde.setOnClickListener(v ->
                Toast.makeText(this, "Destino será escolhido mais tarde", Toast.LENGTH_SHORT).show());

        btnAACD.setOnClickListener(v ->
                Toast.makeText(this, "Indo para AACD...", Toast.LENGTH_SHORT).show());

        btnCasa.setOnClickListener(v ->
                Toast.makeText(this, "Indo para Casa...", Toast.LENGTH_SHORT).show());
    }
}
