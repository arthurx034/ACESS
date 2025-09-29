package com.alana;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Papel do usuário (passenger ou driver)
        role = getIntent().getStringExtra("role");
        if (role == null) role = "passenger";

        // Inicializa FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // Configuração do Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Elementos da UI
        EditText edtEmail = findViewById(R.id.edt_email);
        EditText edtPassword = findViewById(R.id.edt_password);
        Button btnLogin = findViewById(R.id.btn_continue);
        LinearLayout btnGoogle = findViewById(R.id.btn_google);

        // Login manual temporário
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Login manual temporário!", Toast.LENGTH_SHORT).show();
            goToNextScreen();
        });

        // Login com Google
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException.class);

                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Toast.makeText(this, "Falha ao obter conta Google", Toast.LENGTH_SHORT).show();
                }

            } catch (ApiException e) {
                Log.e("GoogleLogin", "Erro: ", e);
                Toast.makeText(this, "Login Google falhou: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Toast.makeText(this, "Token inválido!", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login Google realizado!", Toast.LENGTH_SHORT).show();
                        goToNextScreen();
                    } else {
                        Toast.makeText(this, "Erro ao autenticar com Firebase!", Toast.LENGTH_SHORT).show();
                        Log.e("FirebaseAuth", "Falha: ", task.getException());
                    }
                });
    }

    private void goToNextScreen() {
        Intent intent = "passenger".equalsIgnoreCase(role)
                ? new Intent(this, PassengerActivity.class)
                : new Intent(this, DriverActivity.class);
        startActivity(intent);
        finish();
    }
}
