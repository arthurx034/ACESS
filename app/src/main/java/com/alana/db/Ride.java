package com.alana.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rides")
public class Ride {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String destino;
    public double preco;
    public double distancia;
    public int tempo;

    public Ride(String destino, double preco, double distancia, int tempo) {
        this.destino = destino;
        this.preco = preco;
        this.distancia = distancia;
        this.tempo = tempo;
    }
}
