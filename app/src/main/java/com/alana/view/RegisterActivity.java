package com.alana.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alana.R;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;
    private EditText edtEmail, edtPassword;
    private Button btnContinue;
    private LinearLayout btnGoogle;
    private ImageButton btnBack;

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private FirebaseFirestore db;

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
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnContinue = findViewById(R.id.btn_continue);
        btnGoogle = findViewById(R.id.btn_google);
        btnBack = findViewById(R.id.btn_back);

        btnContinue.setOnClickListener(v -> registerWithEmail());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void registerWithEmail() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        salvarUsuarioNoFirestore(userId, email, "passenger");
                        goToNextScreen();
                    } else {
                        Toast.makeText(this, "Erro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void salvarUsuarioNoFirestore(String userId, String email, String role) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", role);
        user.put("criadoEm", FieldValue.serverTimestamp());

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Usuário salvo"))
                .addOnFailureListener(e -> Log.e("Firestore", "Erro ao salvar", e));
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
                }

            } catch (ApiException e) {
                Toast.makeText(this, "Falha: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String email = firebaseAuth.getCurrentUser().getEmail();
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        salvarUsuarioNoFirestore(userId, email, "passenger");
                        goToNextScreen();
                    } else {
                        Toast.makeText(this, "Erro ao autenticar!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToNextScreen() {
        startActivity(new Intent(this, PassengerActivity.class));
        finish();
    }
}
