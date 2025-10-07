package com.alana.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alana.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 100;

    private EditText edtEntrada, edtSenha;
    private Button btnLogin;
    private LinearLayout btnGoogle;
    private ImageButton btnVoltar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;

    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicialização
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        role = getIntent().getStringExtra("role");
        if (role == null) role = "passenger";

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // UI
        edtEntrada = findViewById(R.id.edt_email);
        edtSenha = findViewById(R.id.edt_password);
        btnLogin = findViewById(R.id.btn_continue);
        btnGoogle = findViewById(R.id.btn_google);
        btnVoltar = findViewById(R.id.btn_back);

        aplicarMascaraTelefone(edtEntrada);

        btnVoltar.setOnClickListener(v -> onBackPressed());
        btnLogin.setOnClickListener(v -> realizarLogin());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    // ------------------------------------------------------
    // 🔹 Login principal (email + senha ou telefone + senha)
    // ------------------------------------------------------
    private void realizarLogin() {
        String entrada = edtEntrada.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        if (TextUtils.isEmpty(entrada) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (validarEmail(entrada)) {
            // Login com email
            firebaseAuth.signInWithEmailAndPassword(entrada, senha)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Login com email realizado!", Toast.LENGTH_SHORT).show();
                            irParaProximaTela();
                        } else {
                            Toast.makeText(this, "Erro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else if (validarTelefone(entrada)) {
            // Login com telefone (sem SMS)
            String telefone = normalizarTelefone(entrada);

            db.collection("users")
                    .whereEqualTo("contato", telefone)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // Telefone existe → login aceito
                            Toast.makeText(this, "Login com telefone realizado!", Toast.LENGTH_SHORT).show();
                            irParaProximaTela();
                        } else {
                            Toast.makeText(this, "Número não encontrado. Cadastre-se primeiro.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao buscar telefone: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Formato inválido! Digite um email ou telefone válido.", Toast.LENGTH_LONG).show();
        }
    }

    // ------------------------------------------------------
    // 🔹 Login com Google
    // ------------------------------------------------------
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
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login Google realizado!", Toast.LENGTH_SHORT).show();
                        irParaProximaTela();
                    } else {
                        Toast.makeText(this, "Erro ao autenticar com Firebase!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ------------------------------------------------------
    // 🔹 Utilitários
    // ------------------------------------------------------
    private boolean validarEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean validarTelefone(String telefone) {
        if (telefone == null) return false;
        String digitos = telefone.replaceAll("[^\\d]", "");
        return (digitos.length() == 10 || digitos.length() == 11 || (telefone.startsWith("+") && digitos.length() >= 8));
    }

    private String normalizarTelefone(String raw) {
        String digitos = raw.replaceAll("[^\\d]", "");
        if (digitos.length() == 10 || digitos.length() == 11) {
            return "+55" + digitos;
        } else if (raw.startsWith("+")) {
            return "+" + digitos;
        }
        return raw;
    }

    private void irParaProximaTela() {
        Intent intent = "passenger".equalsIgnoreCase(role)
                ? new Intent(this, PassengerActivity.class)
                : new Intent(this, DriverActivity.class);
        startActivity(intent);
        finish();
    }

    // ------------------------------------------------------
    // 🔹 Máscara automática para telefone
    // ------------------------------------------------------
    private void aplicarMascaraTelefone(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            boolean isAtualizando = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAtualizando) return;
                String atual = s.toString();
                if (atual.contains("@") || atual.matches(".*[A-Za-z].*")) return;

                String digitos = atual.replaceAll("[^\\d]", "");
                String formatado;
                if (digitos.length() == 0) formatado = "";
                else if (digitos.length() <= 2) formatado = "(" + digitos;
                else if (digitos.length() <= 6) formatado = "(" + digitos.substring(0, 2) + ") " + digitos.substring(2);
                else if (digitos.length() <= 10)
                    formatado = "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, digitos.length() - 4) + "-" + digitos.substring(digitos.length() - 4);
                else {
                    String parte1 = digitos.substring(0, 2);
                    String parte2 = digitos.substring(2, Math.min(7, digitos.length()));
                    String parte3 = digitos.length() > 7 ? digitos.substring(7, Math.min(11, digitos.length())) : "";
                    formatado = "(" + parte1 + ") " + parte2;
                    if (!parte3.isEmpty()) formatado += "-" + parte3;
                }

                isAtualizando = true;
                editText.setText(formatado);
                editText.setSelection(formatado.length());
                isAtualizando = false;
            }
        });
    }
}
