package com.alana.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alana.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int RC_SIGN_IN = 101;

    private EditText edtEntrada;
    private EditText edtSenha;
    private Button btnContinuar;
    private LinearLayout btnGoogle;
    private ImageButton btnVoltar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;

    private String verifId; // para Phone Auth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

        edtEntrada = findViewById(R.id.edt_email);
        edtSenha = findViewById(R.id.edt_password);
        btnContinuar = findViewById(R.id.btn_continue);
        btnGoogle = findViewById(R.id.btn_google);
        btnVoltar = findViewById(R.id.btn_back);

        aplicarMascaraTelefone(edtEntrada);

        btnContinuar.setOnClickListener(v -> registrarUsuario());
        btnGoogle.setOnClickListener(v -> entrarComGoogle());
        btnVoltar.setOnClickListener(v -> onBackPressed());
    }

    private void registrarUsuario() {
        String entrada = edtEntrada.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        if (TextUtils.isEmpty(entrada) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (validarEmail(entrada)) {
            // Registro via email
            firebaseAuth.createUserWithEmailAndPassword(entrada, senha)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                salvarUsuarioNoFirestore(user.getUid(), entrada, "passenger");
                                irParaProximaTela();
                            }
                        } else {
                            Toast.makeText(this, "Erro ao registrar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        } else if (validarTelefone(entrada)) {
            // Registro via telefone com verificação SMS
            String telefone = normalizarTelefone(entrada);

            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber(telefone)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                    // Auto verificação
                                    signInWithPhoneAuthCredential(credential, telefone);
                                }

                                @Override
                                public void onVerificationFailed(@NonNull FirebaseException e) {
                                    Toast.makeText(RegisterActivity.this, "Falha na verificação: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onCodeSent(@NonNull String verificationId,
                                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                    verifId = verificationId;
                                    // Aqui você precisa abrir uma tela para o usuário inserir o código SMS
                                    Toast.makeText(RegisterActivity.this, "Código enviado para " + telefone, Toast.LENGTH_SHORT).show();
                                    // Exemplo: startActivity(new Intent(this, VerifyCodeActivity.class).putExtra("telefone", telefone));
                                }
                            }).build();
            PhoneAuthProvider.verifyPhoneNumber(options);

        } else {
            Toast.makeText(this, "Email ou telefone inválido", Toast.LENGTH_LONG).show();
        }
    }

    // Função para fazer login com telefone após SMS
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, String telefone) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            salvarUsuarioNoFirestore(user.getUid(), telefone, "passenger");
                            irParaProximaTela();
                        }
                    } else {
                        Toast.makeText(this, "Erro ao autenticar telefone", Toast.LENGTH_SHORT).show();
                    }
                });
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

    private boolean validarEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean validarTelefone(String telefone) {
        if (telefone == null) return false;
        String digitos = telefone.replaceAll("[^\\d]", "");
        return (digitos.length() == 10 || digitos.length() == 11 || (telefone.startsWith("+") && digitos.length() >= 8));
    }

    private void salvarUsuarioNoFirestore(String userId, String contato, String role) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("contato", contato);
        usuario.put("role", role);
        usuario.put("criadoEm", FieldValue.serverTimestamp());

        db.collection("users").document(userId).set(usuario)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Usuário salvo"))
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar no banco: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void entrarComGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = com.google.android.gms.auth.api.signin.GoogleSignIn
                        .getSignedInAccountFromIntent(data)
                        .getResult(ApiException.class);

                if (account != null) {
                    autenticarFirebaseComGoogle(account.getIdToken());
                }

            } catch (ApiException e) {
                Toast.makeText(this, "Falha: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void autenticarFirebaseComGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            salvarUsuarioNoFirestore(user.getUid(), user.getEmail(), "passenger");
                            irParaProximaTela();
                        }
                    } else {
                        Toast.makeText(this, "Erro ao autenticar Google!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void irParaProximaTela() {
        startActivity(new Intent(this, PassengerActivity.class));
        finish();
    }

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
                else if (digitos.length() <= 10) formatado = "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, digitos.length() - 4) + "-" + digitos.substring(digitos.length() - 4);
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
