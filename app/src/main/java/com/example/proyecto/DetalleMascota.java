package com.example.proyecto;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DetalleMascota extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_mascota);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainDetalle), (View v, WindowInsetsCompat insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int idUsuarioActual = getIntent().getIntExtra("id_usuario", 1);

        LinearLayout btnNavInicio = findViewById(R.id.btnNavInicioDetalle);
        LinearLayout btnNavAgregar = findViewById(R.id.btnNavAgregarDetalle);
        LinearLayout btnNavCuenta = findViewById(R.id.btnNavCuentaDetalle);

        btnNavInicio.setOnClickListener(v -> finish());

        btnNavAgregar.setOnClickListener(v -> {
            Intent intentAgregar = new Intent(DetalleMascota.this, AgregarMascota.class);
            intentAgregar.putExtra("id_usuario", idUsuarioActual);
            startActivity(intentAgregar);
            finish();
        });

        btnNavCuenta.setOnClickListener(v ->
                Toast.makeText(this, "Cuenta", Toast.LENGTH_SHORT).show()
        );
        TextView btnVolver = findViewById(R.id.btnVolverDetalle);
        btnVolver.setOnClickListener(v -> finish());

        int idMascota = getIntent().getIntExtra("id_mascota", -1);
        if (idMascota == -1) {
            Toast.makeText(this, "No se encontró la mascota", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DataBaseHelper dbHelper = new DataBaseHelper(this);
        MascotaDetalle mascota = dbHelper.obtenerDetalleMascota(idMascota);
        if (mascota == null) {
            Toast.makeText(this, "Mascota no encontrada en la base de datos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView ivFoto = findViewById(R.id.ivDetalleFoto);
        TextView txtNombre = findViewById(R.id.txtDetalleNombre);
        TextView txtRaza = findViewById(R.id.txtDetalleRaza);
        TextView txtEdad = findViewById(R.id.txtDetalleEdad);
        TextView txtPeso = findViewById(R.id.txtDetallePeso);
        TextView txtSexo = findViewById(R.id.txtDetalleSexo);
        TextView txtEsterilizado = findViewById(R.id.txtDetalleEsterilizado);
        TextView txtConteoVacunas = findViewById(R.id.txtDetalleConteoVacunas);
        LinearLayout contenedorVacunas = findViewById(R.id.contenedorListaVacunasDetalle);

        if (!TextUtils.isEmpty(mascota.getFoto())) {
            try {
                ivFoto.setImageURI(Uri.parse(mascota.getFoto()));
            } catch (Exception e) {
                ivFoto.setImageResource(R.drawable.icon_usuario);
            }
        } else {
            ivFoto.setImageResource(R.drawable.icon_usuario);
        }

        txtNombre.setText(mascota.getNombre());
        txtRaza.setText(!TextUtils.isEmpty(mascota.getRaza()) ? mascota.getRaza() : "Mestizo");

        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        int edad = mascota.getAnoNacimiento() > 0 ? Math.max(0, anioActual - mascota.getAnoNacimiento()) : 0;
        String edadStr = edad + (edad == 1 ? " año" : " años");
        if (mascota.getAnoNacimiento() > 0) {
            edadStr += " (Nacido en " + mascota.getAnoNacimiento() + ")";
        }
        txtEdad.setText("Edad: " + edadStr);

        String pesoStr = mascota.getPeso() > 0
                ? String.format(Locale.getDefault(), "%.1f kg", mascota.getPeso())
                : "No registrado";
        txtPeso.setText("Peso: " + pesoStr);

        txtSexo.setText("Sexo: " + (mascota.getSexo() == 1 ? "Hembra" : "Macho"));
        txtEsterilizado.setText("Esterilizado/a: " + (mascota.getEsterilizado() == 1 ? "Sí" : "No"));

        List<VacunaDetalle> vacunas = mascota.getVacunas();
        int totalVacunas = vacunas.size();
        txtConteoVacunas.setText(totalVacunas + (totalVacunas == 1 ? " dosis" : " dosis"));

        mostrarListaVacunas(contenedorVacunas, vacunas);
    }

    private void mostrarListaVacunas(LinearLayout contenedor, List<VacunaDetalle> vacunas) {
        float density = getResources().getDisplayMetrics().density;
        int pad = (int) (16 * density);

        if (vacunas.isEmpty()) {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("Aún no hay vacunas registradas para esta mascota.");
            tvVacio.setTextColor(Color.parseColor("#6E7772"));
            tvVacio.setBackgroundColor(Color.WHITE);
            tvVacio.setPadding(pad, pad, pad, pad);
            contenedor.addView(tvVacio);
            return;
        }

        for (int i = 0; i < vacunas.size(); i++) {
            VacunaDetalle v = vacunas.get(i);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(i % 2 == 0 ? Color.WHITE : Color.parseColor("#EFF4EE"));
            card.setPadding(pad, pad, pad, pad);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = (int) (12 * density);

            TextView tvTitulo = new TextView(this);
            tvTitulo.setText(v.getNombre());
            tvTitulo.setTextColor(Color.parseColor("#1F2421"));
            tvTitulo.setTextSize(16f);
            tvTitulo.setTypeface(null, Typeface.BOLD);
            card.addView(tvTitulo);

            TextView tvFechas = new TextView(this);
            StringBuilder sbFechas = new StringBuilder("Aplicada: ").append(v.getFechaAplicacion());
            if (!TextUtils.isEmpty(v.getProximaAplicacion())) {
                sbFechas.append("  ·  Próxima: ").append(v.getProximaAplicacion());
            }
            tvFechas.setText(sbFechas.toString());
            tvFechas.setTextColor(Color.parseColor("#355847"));
            tvFechas.setTextSize(13f);
            tvFechas.setPadding(0, (int) (6 * density), 0, 0);
            card.addView(tvFechas);

            TextView tvLote = new TextView(this);
            StringBuilder sbLote = new StringBuilder("Lote: ").append(v.getFolioLote())
                    .append("  ·  Registro: ").append(v.getNumeroRegistro());
            if (v.getPeriodicidadDias() != null && v.getPeriodicidadDias() > 0) {
                sbLote.append("  ·  Cada ").append(v.getPeriodicidadDias()).append(" días");
            }
            tvLote.setText(sbLote.toString());
            tvLote.setTextColor(Color.parseColor("#55615A"));
            tvLote.setTextSize(12f);
            tvLote.setPadding(0, (int) (4 * density), 0, 0);
            card.addView(tvLote);

            if (!TextUtils.isEmpty(v.getDescripcion())) {
                TextView tvDesc = new TextView(this);
                tvDesc.setText("Notas: " + v.getDescripcion());
                tvDesc.setTextColor(Color.parseColor("#6E7772"));
                tvDesc.setTextSize(12f);
                tvDesc.setPadding(0, (int) (4 * density), 0, 0);
                card.addView(tvDesc);
            }

            contenedor.addView(card, params);
        }
    }


}
