package com.alana.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alana.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView etDestino;
    private Button btnMaisTarde, btnAACD, btnCasa;
    private ImageButton btnHome, btnPerfil;
    private ImageButton btnInsta, btnEmail;
    private LinearLayout layoutInsta, layoutEmail;

    private OkHttpClient client;
    private List<String> nomesEnderecos;
    private List<Double> latitudes, longitudes;
    private ArrayAdapter<String> adapter;

    private int selectedIndex = -1; // índice do item selecionado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ======= INICIALIZAÇÃO UI =======
        etDestino = findViewById(R.id.etDestino);
        btnMaisTarde = findViewById(R.id.btnMaisTarde);
        btnAACD = findViewById(R.id.btnAACD);
        btnCasa = findViewById(R.id.btnCasa);

        btnHome = findViewById(R.id.btn_home);
        btnPerfil = findViewById(R.id.btnPerfil);

        btnInsta = findViewById(R.id.imgInsta);
        btnEmail = findViewById(R.id.imgEmail);
        layoutInsta = findViewById(R.id.layoutInsta);
        layoutEmail = findViewById(R.id.layoutEmail);

        // ======= LISTAS E CLIENTE HTTP =======
        client = new OkHttpClient();
        nomesEnderecos = new ArrayList<>();
        latitudes = new ArrayList<>();
        longitudes = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nomesEnderecos);
        etDestino.setAdapter(adapter);
        etDestino.setThreshold(1);

        // ======= SUGESTÕES ENQUANTO DIGITA =======
        etDestino.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedIndex = -1; // resetar seleção
                if (s.length() >= 1) buscarSugestoesNominatim(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        // ======= CLIQUE EM SUGESTÃO =======
        etDestino.setOnItemClickListener((parent, view, position, id) -> {
            selectedIndex = position;
            etDestino.setText(nomesEnderecos.get(position));
            etDestino.setSelection(etDestino.getText().length());
        });

        // ======= ENTER NO TECLADO =======
        etDestino.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                if (selectedIndex >= 0 && selectedIndex < nomesEnderecos.size()) {
                    abrirPassengerActivity(selectedIndex);
                } else {
                    Toast.makeText(this, "Selecione um destino válido", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        // ======= BOTÕES DE LOCAIS FIXOS =======
        btnMaisTarde.setOnClickListener(v ->
                Toast.makeText(this, "Destino será escolhido mais tarde", Toast.LENGTH_SHORT).show());

        btnAACD.setOnClickListener(v -> buscarEnderecoEIr("R. da Doméstica, 250 - Planalto"));
        btnCasa.setOnClickListener(v -> buscarEnderecoEIr("Rua. Malaquias Miguel da Silva, 60 - Luziote"));

        // ======= BARRA INFERIOR =======
        btnHome.setOnClickListener(v -> Toast.makeText(this, "Tela inicial", Toast.LENGTH_SHORT).show());
        btnPerfil.setOnClickListener(v -> Toast.makeText(this, "Abrindo perfil", Toast.LENGTH_SHORT).show());

        // ======= REDES =======
        btnInsta.setOnClickListener(v -> Toast.makeText(this, "Abrindo Instagram", Toast.LENGTH_SHORT).show());
        btnEmail.setOnClickListener(v -> Toast.makeText(this, "Abrindo Email", Toast.LENGTH_SHORT).show());
    }

    // ======= BUSCAR SUGESTÕES NOMINATIM =======
    private void buscarSugestoesNominatim(String query) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?q="
                    + URLEncoder.encode(query, "UTF-8")
                    + "&format=json&addressdetails=1&limit=5";

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "AcessApp/1.0 (acessapp@gmail.com)")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) { e.printStackTrace(); }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) return;

                    String json = response.body().string();
                    try {
                        JSONArray array = new JSONArray(json);
                        nomesEnderecos.clear();
                        latitudes.clear();
                        longitudes.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            nomesEnderecos.add(obj.getString("display_name"));
                            latitudes.add(Double.parseDouble(obj.getString("lat")));
                            longitudes.add(Double.parseDouble(obj.getString("lon")));
                        }

                        runOnUiThread(() -> adapter.notifyDataSetChanged());

                    } catch (Exception e) { e.printStackTrace(); }
                }
            });

        } catch (Exception e) { e.printStackTrace(); }
    }

    // ======= BUSCAR ENDEREÇO FIXO E ABRIR PASSENGER =======
    private void buscarEnderecoEIr(String endereco) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?q="
                    + URLEncoder.encode(endereco, "UTF-8")
                    + "&format=json&addressdetails=1&limit=1";

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "AcessApp/1.0 (acessapp@gmail.com)")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) { e.printStackTrace(); }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) return;

                    String json = response.body().string();
                    try {
                        JSONArray array = new JSONArray(json);
                        if (array.length() > 0) {
                            JSONObject obj = array.getJSONObject(0);
                            String nome = obj.getString("display_name");
                            double lat = Double.parseDouble(obj.getString("lat"));
                            double lon = Double.parseDouble(obj.getString("lon"));

                            runOnUiThread(() -> abrirPassengerActivity(nome, lat, lon));
                        }

                    } catch (Exception e) { e.printStackTrace(); }
                }
            });

        } catch (Exception e) { e.printStackTrace(); }
    }

    // ======= ABRIR PASSENGER ACTIVITY =======
    private void abrirPassengerActivity(int pos) {
        abrirPassengerActivity(nomesEnderecos.get(pos), latitudes.get(pos), longitudes.get(pos));
    }

    private void abrirPassengerActivity(String nome, double lat, double lon) {
        Intent intent = new Intent(MainActivity.this, PassengerActivity.class);
        intent.putExtra("destinoNome", nome);
        intent.putExtra("destinoLat", lat);
        intent.putExtra("destinoLon", lon);
        startActivity(intent);
    }
}
