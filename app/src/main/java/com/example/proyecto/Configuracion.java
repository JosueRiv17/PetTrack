package com.example.proyecto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class Configuracion extends AppCompatActivity {

    private static final String PREFS_AJUSTES = "ajustes";
    private static final String KEY_NOTIFICACIONES = "notificaciones";
    private static final String KEY_RECORDATORIOS = "recordatorios";

    private DataBaseHelper dbHelper;
    private int idUsuarioActual;
    private TextView txtNombrePerfil, txtCorreoPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_configuracion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DataBaseHelper(this);
        idUsuarioActual = getIntent().getIntExtra("id_usuario", -1);

        txtNombrePerfil = findViewById(R.id.txtNombrePerfil);
        txtCorreoPerfil = findViewById(R.id.txtCorreoPerfil);
        cargarPerfil();

        // Cuenta
        findViewById(R.id.opcionEditarPerfil).setOnClickListener(v -> mostrarDialogoEditarPerfil());
        findViewById(R.id.opcionCambiarContrasena).setOnClickListener(v -> mostrarDialogoCambiarContrasena());

        // Preferencias (se guardan en SharedPreferences)
        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);
        SwitchCompat swNotificaciones = findViewById(R.id.swNotificaciones);
        SwitchCompat swRecordatorios = findViewById(R.id.swRecordatorios);
        swNotificaciones.setChecked(prefs.getBoolean(KEY_NOTIFICACIONES, true));
        swRecordatorios.setChecked(prefs.getBoolean(KEY_RECORDATORIOS, true));
        swNotificaciones.setOnCheckedChangeListener((b, activo) ->
                prefs.edit().putBoolean(KEY_NOTIFICACIONES, activo).apply());
        swRecordatorios.setOnCheckedChangeListener((b, activo) ->
                prefs.edit().putBoolean(KEY_RECORDATORIOS, activo).apply());

        // Datos y app
        findViewById(R.id.opcionCompartirApp).setOnClickListener(v -> compartirApp());
        findViewById(R.id.opcionBorrarDatos).setOnClickListener(v -> confirmarBorrarDatos());

        // Cerrar sesión
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(v -> confirmarCerrarSesion());
    }

    private void cargarPerfil() {
        String[] datos = dbHelper.obtenerDatosUsuario(idUsuarioActual);
        if (datos != null) {
            txtNombrePerfil.setText(datos[0]);
            txtCorreoPerfil.setText(datos[1]);
        }
    }

    private EditText crearCampo(String hint, int inputType) {
        EditText campo = new EditText(this);
        campo.setHint(hint);
        campo.setInputType(inputType);
        return campo;
    }

    private LinearLayout crearContenedor() {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(48, 16, 48, 0);
        return contenedor;
    }

    private void mostrarDialogoEditarPerfil() {
        String[] datos = dbHelper.obtenerDatosUsuario(idUsuarioActual);
        if (datos == null) return;

        EditText inputNombre = crearCampo("Tu nombre",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        EditText inputCorreo = crearCampo("ejemplo@correo.com",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText inputDireccion = crearCampo("Dirección (opcional)",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
        inputNombre.setText(datos[0]);
        inputCorreo.setText(datos[1]);
        inputDireccion.setText(datos[2]);

        LinearLayout contenedor = crearContenedor();
        contenedor.addView(inputNombre);
        contenedor.addView(inputCorreo);
        contenedor.addView(inputDireccion);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("Editar perfil")
                .setView(contenedor)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre = inputNombre.getText().toString().trim();
            String correo = inputCorreo.getText().toString().trim();
            String direccion = inputDireccion.getText().toString().trim();

            if (TextUtils.isEmpty(nombre)) {
                inputNombre.setError("Rellena este campo.");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                inputCorreo.setError("Correo no válido");
                return;
            }

            if (dbHelper.actualizarPerfil(idUsuarioActual, nombre, correo, direccion)) {
                Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                cargarPerfil();
                setResult(RESULT_OK);
                dialogo.dismiss();
            } else {
                inputCorreo.setError("Este correo ya está registrado");
            }
        }));

        dialogo.show();
    }

    private void mostrarDialogoCambiarContrasena() {
        int tipoPassword = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        EditText inputActual = crearCampo("Contraseña actual", tipoPassword);
        EditText inputNueva = crearCampo("Nueva contraseña (mín. 6 caracteres)", tipoPassword);
        EditText inputConfirmar = crearCampo("Confirma la nueva contraseña", tipoPassword);

        LinearLayout contenedor = crearContenedor();
        contenedor.addView(inputActual);
        contenedor.addView(inputNueva);
        contenedor.addView(inputConfirmar);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("Cambiar contraseña")
                .setView(contenedor)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String actual = inputActual.getText().toString();
            String nueva = inputNueva.getText().toString();
            String confirmar = inputConfirmar.getText().toString();

            if (TextUtils.isEmpty(actual)) {
                inputActual.setError("Rellena este campo.");
                return;
            }
            if (nueva.length() < 6) {
                inputNueva.setError("Mínimo 6 caracteres");
                return;
            }
            if (!nueva.equals(confirmar)) {
                inputConfirmar.setError("Las contraseñas no coinciden");
                return;
            }

            if (dbHelper.cambiarContrasenaPorId(idUsuarioActual, actual, nueva)) {
                Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                dialogo.dismiss();
            } else {
                inputActual.setError("Contraseña actual incorrecta");
            }
        }));

        dialogo.show();
    }

    private void compartirApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,
                "Estoy usando Mi Huellita para llevar la cartilla de vacunación de mis mascotas. ¡Pruébala!");
        startActivity(Intent.createChooser(intent, "Compartir Mi Huellita"));
    }

    private void confirmarBorrarDatos() {
        new AlertDialog.Builder(this)
                .setTitle("Borrar mis datos")
                .setMessage("Se eliminarán todas tus mascotas y sus cartillas. Esta acción no se puede deshacer.")
                .setPositiveButton("Borrar", (d, w) -> {
                    int borradas = dbHelper.borrarMascotasUsuario(idUsuarioActual);
                    Toast.makeText(this,
                            borradas + (borradas == 1 ? " mascota eliminada" : " mascotas eliminadas"),
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarCerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres salir de tu cuenta?")
                .setPositiveButton("Cerrar sesión", (d, w) -> {

                    getSharedPreferences(InicioSesion.PREFS_SESION, MODE_PRIVATE)
                            .edit().remove(InicioSesion.KEY_ID_USUARIO).apply();


                    Intent intent = new Intent(this, InicioSesion.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
