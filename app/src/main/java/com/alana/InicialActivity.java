package com.alana;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class InicialActivity extends AppCompatActivity {

    Button btn_login, btn_cadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        btn_login = findViewById(R.id.btn_login);
        btn_cadastro = findViewById(R.id.btn_cadastro);

        btn_login.setOnClickListener(v -> {
            startActivity(new Intent(InicialActivity.this, LoginActivity.class));
        });

        btn_cadastro.setOnClickListener(v -> {
            startActivity(new Intent(InicialActivity.this, RegisterActivity.class));
        });
    }
}
