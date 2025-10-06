package com.alana.db;

public class Ride {
    private String id;
    private String destino;
    private double preco;
    private double distancia; // em km
    private int tempo; // em minutos
    private String status;
    private String passengerId;
    private String driverId;

    public Ride() {} // necessário pro Firestore

    public Ride(String destino, double preco, double distancia, int tempo) {
        this.destino = destino;
        this.preco = preco;
        this.distancia = distancia;
        this.tempo = tempo;
        this.status = "aguardando";
    }

    // getters e setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
    public double getDistancia() { return distancia; }
    public void setDistancia(double distancia) { this.distancia = distancia; }
    public int getTempo() { return tempo; }
    public void setTempo(int tempo) { this.tempo = tempo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String passengerId) { this.passengerId = passengerId; }
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
}
