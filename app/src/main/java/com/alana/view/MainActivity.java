package com.alana.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
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
    private ImageButton btnInsta, btnEmail, btnWhats;
    private ScrollView scrollConteudo;

    private OkHttpClient client;
    private final List<String> nomesEnderecos = new ArrayList<>();
    private final List<Double> latitudes = new ArrayList<>();
    private final List<Double> longitudes = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new OkHttpClient();

        etDestino = findViewById(R.id.etDestino);
        btnMaisTarde = findViewById(R.id.btnMaisTarde);
        btnAACD = findViewById(R.id.btnAACD);
        btnCasa = findViewById(R.id.btnCasa);
        btnHome = findViewById(R.id.btn_home);
        btnPerfil = findViewById(R.id.btnPerfil);
        btnInsta = findViewById(R.id.imgInsta);
        btnEmail = findViewById(R.id.imgEmail);
        // btnWhats might not be present in layout, guard against NPE
        scrollConteudo = findViewById(R.id.scrollConteudo);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nomesEnderecos);
        etDestino.setAdapter(adapter);
        etDestino.setThreshold(2);

        etDestino.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s == null ? "" : s.toString();
                if (q.length() >= 2) buscarSugestoesNominatim(q);
            }
        });

        etDestino.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < nomesEnderecos.size()) {
                abrirPassengerActivity(nomesEnderecos.get(position), latitudes.get(position), longitudes.get(position));
            }
        });

        etDestino.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String text = etDestino.getText().toString().trim();
                if (!text.isEmpty()) {
                    buscarEnderecoEIr(text);
                } else {
                    Toast.makeText(MainActivity.this, "Digite um endereço", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        btnAACD.setOnClickListener(v -> buscarEnderecoEIr("R. da Doméstica, 250 - Planalto, Uberlândia - MG"));
        btnCasa.setOnClickListener(v -> buscarEnderecoEIr("Rua Malaquias Miguel da Silva, 60 - Luizote, Uberlândia - MG"));
        btnMaisTarde.setOnClickListener(v -> Toast.makeText(this, "Funcionalidade \"Mais tarde\" não implementada.", Toast.LENGTH_SHORT).show());

        btnHome.setOnClickListener(v -> scrollConteudo.smoothScrollTo(0,0));
        if (btnInsta != null) btnInsta.setOnClickListener(v -> openInstagramProfile("arthurx_034"));
        if (btnEmail != null) btnEmail.setOnClickListener(v -> openEmail(
                "arthuribeirorodrigues@gmail.com", "Contato via aplicativo", "Olá, tudo bem?"));
        if (btnWhats != null) btnWhats.setOnClickListener(v ->
                openWhatsApp("34999695432", "Olá! Gostaria de entrar em contato."));
    }

    private void buscarSugestoesNominatim(String query) {
        try {
            String q = URLEncoder.encode(query, "UTF-8");
            String url = "https://nominatim.openstreetmap.org/search?q=" + q + "&format=json&addressdetails=1&limit=6";
            Request req = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "AcessApp/1.0 (contact@example.com)")
                    .build();

            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                    });
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        runOnUiThread(() -> {
                            adapter.clear();
                            adapter.notifyDataSetChanged();
                        });
                        return;
                    }
                    try {
                        String body = response.body().string();
                        JSONArray arr = new JSONArray(body);

                        final List<String> names = new ArrayList<>();
                        final List<Double> lats = new ArrayList<>();
                        final List<Double> lons = new ArrayList<>();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            String display = o.optString("display_name", "");
                            String latStr = o.optString("lat", "0");
                            String lonStr = o.optString("lon", "0");
                            double lat = 0.0;
                            double lon = 0.0;
                            try { lat = Double.parseDouble(latStr); } catch (Exception ignored) {}
                            try { lon = Double.parseDouble(lonStr); } catch (Exception ignored) {}
                            if (!display.isEmpty()) {
                                names.add(display);
                                lats.add(lat);
                                lons.add(lon);
                            }
                        }

                        runOnUiThread(() -> {
                            nomesEnderecos.clear();
                            latitudes.clear();
                            longitudes.clear();
                            nomesEnderecos.addAll(names);
                            latitudes.addAll(lats);
                            longitudes.addAll(lons);
                            adapter.notifyDataSetChanged();
                            if (!nomesEnderecos.isEmpty()) etDestino.showDropDown();
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            adapter.clear();
                            adapter.notifyDataSetChanged();
                        });
                    }
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Erro ao montar requisição de sugestões", Toast.LENGTH_SHORT).show());
        }
    }

    private void buscarEnderecoEIr(String endereco) {
        try {
            String q = URLEncoder.encode(endereco, "UTF-8");
            String url = "https://nominatim.openstreetmap.org/search?q=" + q + "&format=json&limit=1";
            Request req = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "AcessApp/1.0 (contact@example.com)")
                    .build();

            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erro ao buscar endereço", Toast.LENGTH_SHORT).show());
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Resposta inválida do Nominatim", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    try {
                        String body = response.body().string();
                        JSONArray arr = new JSONArray(body);
                        if (arr.length() == 0) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Endereço não encontrado", Toast.LENGTH_SHORT).show());
                            return;
                        }
                        JSONObject o = arr.getJSONObject(0);
                        String display = o.optString("display_name", endereco);
                        double lat = 0.0;
                        double lon = 0.0;
                        try { lat = Double.parseDouble(o.optString("lat", "0")); } catch (Exception ignored) {}
                        try { lon = Double.parseDouble(o.optString("lon", "0")); } catch (Exception ignored) {}

                        final double fLat = lat;
                        final double fLon = lon;
                        final String fDisplay = display;

                        runOnUiThread(() -> abrirPassengerActivity(fDisplay, fLat, fLon));
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erro ao processar resposta do Nominatim", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao codificar endereço", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirPassengerActivity(String nome, double lat, double lon) {
        if (lat == 0.0 || lon == 0.0) {
            Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MainActivity.this, PassengerActivity.class);
        intent.putExtra("destinoNome", nome);
        intent.putExtra("destinoLat", lat);
        intent.putExtra("destinoLon", lon);
        startActivity(intent);
    }

    private void openInstagramProfile(String username) {
        try {
            Uri uri = Uri.parse("http://instagram.com/_u/" + username);
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
            likeIng.setPackage("com.instagram.android");
            startActivity(likeIng);
        } catch (ActivityNotFoundException e) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/" + username))); }
            catch (Exception ignored) { Toast.makeText(this, "Não foi possível abrir Instagram", Toast.LENGTH_SHORT).show(); }
        }
    }

    private void openEmail(String toAddress, String subject, String body) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + Uri.encode(toAddress)));
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(intent, "Enviar email"));
        } catch (Exception e) {
            Toast.makeText(this, "Nenhum app de email encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWhatsApp(String phoneNumber, String text) {
        try {
            String encodedText = URLEncoder.encode(text, "UTF-8");
            String url = "https://wa.me/" + phoneNumber + "?text=" + encodedText;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp"));
                startActivity(intent);
            } catch (Exception ignored) {
                Toast.makeText(this, "Não foi possível abrir WhatsApp", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
