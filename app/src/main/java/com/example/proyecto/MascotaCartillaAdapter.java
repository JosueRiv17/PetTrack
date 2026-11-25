package com.example.proyecto;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MascotaCartillaAdapter extends RecyclerView.Adapter<MascotaCartillaAdapter.ViewHolder> {

    public interface OnMascotaClickListener {
        void onMascotaClick(MascotaCartilla mascota);
    }

    private final List<MascotaCartilla> listaMascotas;
    private final OnMascotaClickListener listener;

    public MascotaCartillaAdapter(List<MascotaCartilla> listaMascotas, OnMascotaClickListener listener) {
        this.listaMascotas = listaMascotas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.objeto_mascota_cartilla, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MascotaCartilla mascota = listaMascotas.get(position);

        holder.txtNombreMascota.setText(mascota.getNombre());
        holder.txtRazaMascota.setText(mascota.getRaza());
        holder.txtEdadMascota.setText(mascota.getEdadTexto());
        holder.txtResumenVacunas.setText(
                mascota.getVacunasAplicadas() + " de " + mascota.getVacunasTotales() + " vacunas"
        );

        // Alternar color de tarjeta (crema cálido y lila suave)
        if (position % 2 == 0) {
            holder.layoutFondoTarjeta.setBackgroundColor(Color.parseColor("#FFF1E1"));
        } else {
            holder.layoutFondoTarjeta.setBackgroundColor(Color.parseColor("#ECE8FA"));
        }

        int pendientes = Math.max(0, mascota.getVacunasTotales() - mascota.getVacunasAplicadas());
        if (pendientes == 0 && mascota.getVacunasTotales() > 0) {
            holder.txtEstadoVacunas.setText("• Al día");
            holder.txtEstadoVacunas.setTextColor(Color.parseColor("#1E8E5A"));
            holder.txtEstadoVacunas.setBackgroundColor(Color.parseColor("#DDF5EA"));
        } else {
            holder.txtEstadoVacunas.setText("• " + pendientes + (pendientes == 1 ? " pendiente" : " pendientes"));
            holder.txtEstadoVacunas.setTextColor(Color.parseColor("#5B4B9E"));
            holder.txtEstadoVacunas.setBackgroundColor(Color.parseColor("#F7F5FF"));
        }

        if (mascota.getFotoUri() != null && !mascota.getFotoUri().isEmpty()) {
            try {
                holder.ivFotoMascota.setImageURI(Uri.parse(mascota.getFotoUri()));
            } catch (Exception e) {
                holder.ivFotoMascota.setImageResource(R.drawable.icon_usuario);
            }
        } else {
            holder.ivFotoMascota.setImageResource(R.drawable.icon_usuario);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMascotaClick(mascota);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMascotas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutFondoTarjeta;
        ImageView ivFotoMascota;
        TextView txtRazaMascota, txtEdadMascota, txtNombreMascota, txtEstadoVacunas, txtResumenVacunas;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutFondoTarjeta = itemView.findViewById(R.id.layoutFondoTarjeta);
            ivFotoMascota = itemView.findViewById(R.id.ivFotoMascota);
            txtRazaMascota = itemView.findViewById(R.id.txtRazaMascota);
            txtEdadMascota = itemView.findViewById(R.id.txtEdadMascota);
            txtNombreMascota = itemView.findViewById(R.id.txtNombreMascota);
            txtEstadoVacunas = itemView.findViewById(R.id.txtEstadoVacunas);
            txtResumenVacunas = itemView.findViewById(R.id.txtResumenVacunas);
        }
    }
}