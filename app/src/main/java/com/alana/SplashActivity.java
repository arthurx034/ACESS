// Java
package com.alana;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Intent intent = new Intent(this, ChooseRoleActivity.class);
        startActivity(intent);
        finish(); // fecha a tela de splash para não voltar com o botão "Voltar"
    }
}