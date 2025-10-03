package com.alana.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.alana.R;

public class ChooseRoleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);

        Button btnPassenger = findViewById(R.id.btn_passenger);
        Button btnDriver = findViewById(R.id.btn_driver);

        btnPassenger.setOnClickListener(v -> navigateToLogin("passenger"));
        btnDriver.setOnClickListener(v -> navigateToLogin("driver"));
    }

    /**
     * Abre a LoginActivity passando o papel do usuário
     */
    private void navigateToLogin(String role) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
        finish(); // opcional: fecha a tela de escolha para não voltar
    }
}
