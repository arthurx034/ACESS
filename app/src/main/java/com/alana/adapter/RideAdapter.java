package com.alana.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;

import com.alana.R;
import com.alana.db.Ride;

import java.util.List;

public class RideAdapter extends ArrayAdapter<Ride> {

    public RideAdapter(Context context, List<Ride> rides) {
        super(context, 0, rides);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ride ride = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_ride, parent, false);
        }

        TextView tvDestino = convertView.findViewById(R.id.tvDestino);
        TextView tvPreco = convertView.findViewById(R.id.tvPreco);
        TextView tvDistancia = convertView.findViewById(R.id.tvDistancia);
        TextView tvTempo = convertView.findViewById(R.id.tvTempo);

        if (ride != null) {
            tvDestino.setText("Destino: " + ride.destino);
            tvPreco.setText("Preço: R$ " + ride.preco);
            tvDistancia.setText("Distância: " + ride.distancia + " km");
            tvTempo.setText("Tempo: " + ride.tempo + " min");
        }

        return convertView;
    }
}