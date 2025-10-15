package com.alana.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Corrida {
    public String origem;
    public String destino;
    public double distancia_km;
    public int tempo_estimado_min;
    public double preco_estimado;
    public String status;
    public String dataHora; // ISO string

    public Corrida() {}

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("origem", origem);
        m.put("destino", destino);
        m.put("distancia_km", distancia_km);
        m.put("tempo_estimado_min", tempo_estimado_min);
        m.put("preco_estimado", preco_estimado);
        m.put("status", status);
        m.put("dataHora", dataHora != null ? dataHora : isoNow());
        return m;
    }

    private String isoNow() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
