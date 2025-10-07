package com.alana.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alana.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText edtCodigo;
    private Button btnVerificar;
    private ImageButton btnBack;
    private String verificationId;
    private String telefone;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        edtCodigo = findViewById(R.id.edt_codigo);
        btnVerificar = findViewById(R.id.btn_verificar);
        btnBack = findViewById(R.id.btn_back);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Pega os dados da intent
        verificationId = getIntent().getStringExtra("verificationId");
        telefone = getIntent().getStringExtra("telefone");

        btnVerificar.setOnClickListener(v -> verificarCodigo());
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void verificarCodigo() {
        String codigo = edtCodigo.getText().toString().trim();

        if (codigo.isEmpty()) {
            Toast.makeText(this, "Digite o código recebido por SMS", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, codigo);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            salvarUsuarioNoFirestore(user.getUid());
                            Toast.makeText(this, "Telefone verificado com sucesso!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, PassengerActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Código incorreto ou expirado", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void salvarUsuarioNoFirestore(String userId) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("contato", telefone);
        usuario.put("role", "passenger");
        usuario.put("criadoEm", FieldValue.serverTimestamp());

        db.collection("users").document(userId)
                .set(usuario)
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao salvar no banco: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
