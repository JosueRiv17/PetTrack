package com.example.proyecto;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class AgregarMascota extends AppCompatActivity {

    private ImageView ivPreviewFoto;
    private EditText etNombreMascota, etRazaMascota, etAnioNacimiento, etPesoKg;
    private CheckBox cbEsterilizado;
    private TextView txtContadorVacunasAgregadas, txtInfoPerfil;

    private String fotoSeleccionadaUri = "";
    private int vacunasRegistradas = 0;

    private final ActivityResultLauncher<String> selectorImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (SecurityException ignored) {
                    }
                    fotoSeleccionadaUri = uri.toString();
                    ivPreviewFoto.clearColorFilter();
                    ivPreviewFoto.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agregar_mascotas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainAgregar), (View v, WindowInsetsCompat insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int idUsuarioActual = getIntent().getIntExtra("id_usuario", 1);

        ivPreviewFoto = findViewById(R.id.ivPreviewFoto);
        etNombreMascota = findViewById(R.id.etNombreMascota);
        etRazaMascota = findViewById(R.id.etRazaMascota);
        etAnioNacimiento = findViewById(R.id.etAnioNacimiento);
        etPesoKg = findViewById(R.id.etPesoKg);
        cbEsterilizado = findViewById(R.id.cbEsterilizado);
        txtContadorVacunasAgregadas = findViewById(R.id.txtContadorVacunasAgregadas);
        txtInfoPerfil = findViewById(R.id.txtInfoPerfil);

        txtInfoPerfil.setText(
                "Perfil local · Usuario " + idUsuarioActual +
                        ". Los identificadores y vínculos se generan al guardar. Sexo: 0 = macho, 1 = hembra."
        );

        LinearLayout contenedorFoto = findViewById(R.id.contenedorFoto);
        LinearLayout btnAgregarVacuna = findViewById(R.id.btnAgregarVacuna);
        Button btnGuardarMascota = findViewById(R.id.btnGuardarMascota);
        LinearLayout btnNavInicio = findViewById(R.id.btnNavInicio);
        LinearLayout btnNavCuenta = findViewById(R.id.btnNavCuenta);

        contenedorFoto.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));
        btnAgregarVacuna.setOnClickListener(v -> mostrarDialogoAgregarVacuna());
        btnGuardarMascota.setOnClickListener(v -> guardarMascota(idUsuarioActual));

        btnNavInicio.setOnClickListener(v -> finish());
        btnNavCuenta.setOnClickListener(v ->
                Toast.makeText(this, "Cuenta", Toast.LENGTH_SHORT).show()
        );
    }

    private void mostrarDialogoAgregarVacuna() {
        EditText inputVacuna = new EditText(this);
        inputVacuna.setHint("Ej. Rabia, Séxtuple");
        inputVacuna.setPadding(40, 24, 40, 24);

        new AlertDialog.Builder(this)
                .setTitle("Agregar vacuna")
                .setMessage("Registra una dosis aplicada")
                .setView(inputVacuna)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    vacunasRegistradas++;
                    String nombreDosis = inputVacuna.getText().toString().trim();
                    if (TextUtils.isEmpty(nombreDosis)) {
                        nombreDosis = "Dosis " + vacunasRegistradas;
                    }
                    txtContadorVacunasAgregadas.setText(
                            vacunasRegistradas + (vacunasRegistradas == 1 ? " dosis agregada (" : " dosis agregadas (") + nombreDosis + ")"
                    );
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void guardarMascota(int idUsuarioActual) {
        String nombre = etNombreMascota.getText().toString().trim();
        String raza = etRazaMascota.getText().toString().trim();
        String anioStr = etAnioNacimiento.getText().toString().trim();
        String pesoStr = etPesoKg.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            etNombreMascota.setError("Rellena este campo.");
            etNombreMascota.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(raza)) {
            raza = "Mestizo";
        }

        int edadAnios = 0;
        if (!TextUtils.isEmpty(anioStr)) {
            int anioNac = Integer.parseInt(anioStr);
            int anioActual = Calendar.getInstance().get(Calendar.YEAR);
            edadAnios = Math.max(0, anioActual - anioNac);
        }

        double peso = 0.0;
        if (!TextUtils.isEmpty(pesoStr)) {
            try {
                peso = Double.parseDouble(pesoStr);
            } catch (NumberFormatException e) {
                etPesoKg.setError("Peso no válido");
                etPesoKg.requestFocus();
                return;
            }
        }

        int esterilizado = cbEsterilizado.isChecked() ? 1 : 0;
        int sexo = 0; // 0 = macho por defecto

        DataBaseHelper dbHelper = new DataBaseHelper(this);
        boolean insertado = dbHelper.insertarMascotaConVacunas(
                idUsuarioActual, nombre, raza, edadAnios,
                peso, esterilizado, sexo,
                fotoSeleccionadaUri, vacunasRegistradas, vacunasRegistradas
        );

        if (insertado) {
            Toast.makeText(this, nombre + " guardado correctamente", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error al guardar en SQLite", Toast.LENGTH_SHORT).show();
        }
    }
}