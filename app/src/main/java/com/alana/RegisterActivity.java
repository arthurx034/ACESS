package com.alana;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnContinue;
    private LinearLayout btnGoogle, btnSearch;
    private TextView textRegister, textInfoSms;
    private ImageView imageView;
    private TextView text_have_account;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Certifique-se de que o nome do layout é activity_register.xml

        // Inicializa os componentes
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnContinue = findViewById(R.id.btn_continue);
        btnGoogle = findViewById(R.id.btn_google);
        btnSearch = findViewById(R.id.btn_search);
        textRegister = findViewById(R.id.text_register);
        textInfoSms = findViewById(R.id.text_info_sms);
        imageView = findViewById(R.id.imageView);
        btnBack = findViewById(R.id.btn_back);

        // Ação do botão "Continuar"
        btnContinue.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String senha = edtPassword.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Login com: " + email, Toast.LENGTH_SHORT).show();
                // Aqui você pode iniciar outra Activity ou chamar uma API
                // startActivity(new Intent(this, HomeActivity.class));
            }
        });

        // Ação do botão "Continuar com o Google"
        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Login com Google (simulado)", Toast.LENGTH_SHORT).show()
        );

        // Ação do botão "Encontrar minha conta"
        btnSearch.setOnClickListener(v ->
                Toast.makeText(this, "Recuperar conta (simulado)", Toast.LENGTH_SHORT).show()
        );

        // Ação do botão "Voltar"
        btnBack.setOnClickListener(v -> onBackPressed());
    }
}