package com.example.proyecto;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AgregarMascota extends AppCompatActivity {

    private ImageView ivPreviewFoto;
    private EditText etNombreMascota, etRazaMascota, etAnioNacimiento, etPesoKg;
    private RadioGroup rgSexoMascota;
    private RadioButton rbHembra;
    private CheckBox cbEsterilizado;
    private TextView txtContadorVacunasAgregadas, txtInfoPerfil;

    private String fotoSeleccionadaUri = "";
    private Uri fotoCamaraUri = null;
    private final List<VacunaPendiente> listaVacunas = new ArrayList<>();

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

    private final ActivityResultLauncher<Uri> tomarFotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), tomada -> {
                if (tomada && fotoCamaraUri != null) {
                    fotoSeleccionadaUri = fotoCamaraUri.toString();
                    ivPreviewFoto.clearColorFilter();
                    ivPreviewFoto.setImageURI(fotoCamaraUri);
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
        rgSexoMascota = findViewById(R.id.rgSexoMascota);
        rbHembra = findViewById(R.id.rbHembra);
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

        contenedorFoto.setOnClickListener(v -> mostrarOpcionesFoto());
        btnAgregarVacuna.setOnClickListener(v -> mostrarDialogoAgregarVacuna());
        btnGuardarMascota.setOnClickListener(v -> guardarMascota(idUsuarioActual));

        btnNavInicio.setOnClickListener(v -> finish());
        btnNavCuenta.setOnClickListener(v ->
                Toast.makeText(this, "Cuenta", Toast.LENGTH_SHORT).show()
        );
    }

    private void mostrarOpcionesFoto() {
        String[] opciones = {"Tomar foto con cámara", "Elegir de galería"};
        new AlertDialog.Builder(this)
                .setTitle("Foto de la mascota")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        abrirCamara();
                    } else {
                        selectorImagenLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void abrirCamara() {
        try {
            File carpetaFotos = new File(getCacheDir(), "fotos_mascotas");
            if (!carpetaFotos.exists() && !carpetaFotos.mkdirs()) {
                Toast.makeText(this, "No se pudo crear la carpeta de fotos", Toast.LENGTH_SHORT).show();
                return;
            }
            File archivoFoto = new File(carpetaFotos, "mascota_" + System.currentTimeMillis() + ".jpg");
            fotoCamaraUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    archivoFoto
            );
            tomarFotoLauncher.launch(fotoCamaraUri);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoAgregarVacuna() {
        ScrollView scrollModal = new ScrollView(this);
        scrollModal.setBackgroundColor(Color.parseColor("#FAF8F5"));

        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (18 * getResources().getDisplayMetrics().density);
        contenedor.setPadding(pad, pad, pad, pad);
        scrollModal.addView(contenedor);

        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        EditText etNombreVacuna = crearCampoModal(
                contenedor, "Nombre de la vacuna *", "Ej. Rabia, Séxtuple",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS, ""
        );
        EditText etFolioLote = crearCampoModal(
                contenedor, "Folio / Lote *", "Ej. LOT-2026-A1",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, ""
        );
        EditText etNumRegistro = crearCampoModal(
                contenedor, "Número de registro *", "Ej. SAGARPA-1234",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, ""
        );
        EditText etPeriodicidad = crearCampoModal(
                contenedor, "Periodicidad (días)", "Ej. 365 (opcional)",
                InputType.TYPE_CLASS_NUMBER, ""
        );
        EditText etFechaAplicacion = crearCampoModal(
                contenedor, "Fecha de aplicación * (toca para elegir)", "AAAA-MM-DD",
                InputType.TYPE_CLASS_DATETIME, hoy
        );
        EditText etProximaAplicacion = crearCampoModal(
                contenedor, "Próxima aplicación (toca para elegir)", "AAAA-MM-DD (opcional)",
                InputType.TYPE_CLASS_DATETIME, ""
        );
        EditText etDescripcion = crearCampoModal(
                contenedor, "Descripción", "Notas o refuerzo (opcional)",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES, ""
        );

        configurarSelectorFecha(etFechaAplicacion, true);
        configurarSelectorFecha(etProximaAplicacion, false);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Agregar vacuna")
                .setView(scrollModal)
                .setPositiveButton("Guardar vacuna", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btnPositivo = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnPositivo.setOnClickListener(v -> {
                String nombreVac = etNombreVacuna.getText().toString().trim();
                String folio = etFolioLote.getText().toString().trim();
                String numReg = etNumRegistro.getText().toString().trim();
                String perStr = etPeriodicidad.getText().toString().trim();
                String fechaAp = etFechaAplicacion.getText().toString().trim();
                String proxAp = etProximaAplicacion.getText().toString().trim();
                String desc = etDescripcion.getText().toString().trim();

                if (TextUtils.isEmpty(nombreVac)) {
                    etNombreVacuna.setError("Campo obligatorio");
                    etNombreVacuna.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(folio)) {
                    etFolioLote.setError("Campo obligatorio");
                    etFolioLote.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(numReg)) {
                    etNumRegistro.setError("Campo obligatorio");
                    etNumRegistro.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(fechaAp)) {
                    fechaAp = hoy;
                }

                Integer periodicidad = null;
                if (!TextUtils.isEmpty(perStr)) {
                    try {
                        periodicidad = Integer.parseInt(perStr);
                    } catch (NumberFormatException ex) {
                        etPeriodicidad.setError("Número no válido");
                        etPeriodicidad.requestFocus();
                        return;
                    }
                }

                if (TextUtils.isEmpty(proxAp) && periodicidad != null && periodicidad > 0) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date base = sdf.parse(fechaAp);
                        if (base != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(base);
                            cal.add(Calendar.DAY_OF_YEAR, periodicidad);
                            proxAp = sdf.format(cal.getTime());
                        }
                    } catch (Exception ignored) {
                    }
                }

                listaVacunas.add(new VacunaPendiente(
                        nombreVac, periodicidad, folio, numReg, desc, fechaAp, proxAp
                ));

                int total = listaVacunas.size();
                txtContadorVacunasAgregadas.setText(
                        total + (total == 1 ? " dosis agregada (" : " dosis agregadas (") + nombreVac + " · " + fechaAp + ")"
                );
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void configurarSelectorFecha(EditText campoFecha, boolean esFechaAplicacion) {
        campoFecha.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            String actual = campoFecha.getText().toString().trim();
            if (!actual.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date parsed = sdf.parse(actual);
                    if (parsed != null) {
                        cal.setTime(parsed);
                    }
                } catch (Exception ignored) {
                }
            }

            int anio = cal.get(Calendar.YEAR);
            int mes = cal.get(Calendar.MONTH);
            int dia = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, y, m, d) -> {
                        String fechaFormateada = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
                        campoFecha.setText(fechaFormateada);
                        campoFecha.setError(null);
                    },
                    anio, mes, dia
            );

            if (esFechaAplicacion) {
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());
            }

            picker.show();
        });
    }

    private EditText crearCampoModal(LinearLayout contenedor, String etiqueta, String hint,
                                     int inputType, String valorInicial) {
        float density = getResources().getDisplayMetrics().density;

        TextView tv = new TextView(this);
        tv.setText(etiqueta);
        tv.setTextColor(Color.parseColor("#2F3E38"));
        tv.setTextSize(12f);
        tv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tvParams.topMargin = (int) (10 * density);
        contenedor.addView(tv, tvParams);

        EditText et = new EditText(this);
        et.setHint(hint);
        et.setText(valorInicial);
        et.setInputType(inputType);
        et.setTextColor(Color.parseColor("#1F2421"));
        et.setHintTextColor(Color.parseColor("#9AA39E"));
        et.setBackgroundColor(Color.WHITE);
        int hPad = (int) (12 * density);
        int vPad = (int) (10 * density);
        et.setPadding(hPad, vPad, hPad, vPad);
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        etParams.topMargin = (int) (4 * density);
        contenedor.addView(et, etParams);
        return et;
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
            try {
                int anioNac = Integer.parseInt(anioStr);
                int anioActual = Calendar.getInstance().get(Calendar.YEAR);
                edadAnios = Math.max(0, anioActual - anioNac);
            } catch (NumberFormatException e) {
                etAnioNacimiento.setError("Año no válido");
                etAnioNacimiento.requestFocus();
                return;
            }
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

        int sexo = (rbHembra != null && rbHembra.isChecked()) ? 1 : 0; // 0 = macho, 1 = hembra
        int esterilizado = cbEsterilizado.isChecked() ? 1 : 0;

        DataBaseHelper dbHelper = new DataBaseHelper(this);
        boolean insertado = dbHelper.insertarMascotaConVacunas(
                idUsuarioActual, nombre, raza, edadAnios,
                peso, esterilizado, sexo,
                fotoSeleccionadaUri, listaVacunas
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