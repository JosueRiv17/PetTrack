package com.example.proyecto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class InicioSesion extends AppCompatActivity {

    public static final String PREFS_SESION = "sesion";
    public static final String KEY_ID_USUARIO = "id_usuario";

    private DataBaseHelper dbHelper;
    private EditText etCorreo, etContrasena;
    private CheckBox cbRecordarme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DataBaseHelper(this);


        int idGuardado = getSharedPreferences(PREFS_SESION, MODE_PRIVATE).getInt(KEY_ID_USUARIO, -1);
        if (idGuardado > 0) {
            abrirInicio(idGuardado);
            return;
        }

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_inicio_sesion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        cbRecordarme = findViewById(R.id.cbRecordarme);
        Button btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        TextView txtOlvideContrasena = findViewById(R.id.txtOlvideContrasena);
        TextView txtRegistrarse = findViewById(R.id.txtRegistrarse);

        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());
        txtRegistrarse.setOnClickListener(v -> mostrarDialogoRegistro());
        txtOlvideContrasena.setOnClickListener(v -> mostrarDialogoCambiarContrasena());
    }

    private void iniciarSesion() {
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString();

        if (TextUtils.isEmpty(correo)) {
            etCorreo.setError("Rellena este campo.");
            etCorreo.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Correo no válido");
            etCorreo.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(contrasena)) {
            etContrasena.setError("Rellena este campo.");
            etContrasena.requestFocus();
            return;
        }

        int idUsuario = dbHelper.validarUsuario(correo, contrasena);
        if (idUsuario <= 0) {
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cbRecordarme.isChecked()) {
            guardarSesion(idUsuario);
        }
        abrirInicio(idUsuario);
    }

    private void mostrarDialogoRegistro() {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(48, 16, 48, 0);

        EditText inputNombre = new EditText(this);
        inputNombre.setHint("Tu nombre");
        inputNombre.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        EditText inputCorreo = new EditText(this);
        inputCorreo.setHint("ejemplo@correo.com");
        inputCorreo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        inputCorreo.setText(etCorreo.getText());

        EditText inputContrasena = new EditText(this);
        inputContrasena.setHint("Contraseña (mín. 6 caracteres)");
        inputContrasena.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        contenedor.addView(inputNombre);
        contenedor.addView(inputCorreo);
        contenedor.addView(inputContrasena);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("Crear cuenta")
                .setMessage("Registra tus datos para guardar a tus mascotas")
                .setView(contenedor)
                .setPositiveButton("Registrarme", null) // se asigna abajo para no cerrar si hay errores
                .setNegativeButton("Cancelar", null)
                .create();

        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre = inputNombre.getText().toString().trim();
            String correo = inputCorreo.getText().toString().trim();
            String contrasena = inputContrasena.getText().toString();

            if (TextUtils.isEmpty(nombre)) {
                inputNombre.setError("Rellena este campo.");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                inputCorreo.setError("Correo no válido");
                return;
            }
            if (contrasena.length() < 6) {
                inputContrasena.setError("Mínimo 6 caracteres");
                return;
            }

            long idNuevo = dbHelper.registrarUsuario(nombre, correo, contrasena);
            if (idNuevo == -1) {
                inputCorreo.setError("Este correo ya está registrado");
                return;
            }

            Toast.makeText(this, "Cuenta creada. ¡Bienvenido, " + nombre + "!", Toast.LENGTH_SHORT).show();
            dialogo.dismiss();
            if (cbRecordarme.isChecked()) {
                guardarSesion((int) idNuevo);
            }
            abrirInicio((int) idNuevo);
        }));

        dialogo.show();
    }

    private void mostrarDialogoCambiarContrasena() {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(48, 16, 48, 0);

        EditText inputCorreo = new EditText(this);
        inputCorreo.setHint("Correo registrado");
        inputCorreo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        inputCorreo.setText(etCorreo.getText());

        EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre con el que te registraste");
        inputNombre.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        EditText inputNueva = new EditText(this);
        inputNueva.setHint("Nueva contraseña (mín. 6 caracteres)");
        inputNueva.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText inputConfirmar = new EditText(this);
        inputConfirmar.setHint("Confirma la nueva contraseña");
        inputConfirmar.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        contenedor.addView(inputCorreo);
        contenedor.addView(inputNombre);
        contenedor.addView(inputNueva);
        contenedor.addView(inputConfirmar);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("Cambiar contraseña")
                .setMessage("Confirma tu correo y nombre para crear una nueva contraseña")
                .setView(contenedor)
                .setPositiveButton("Guardar", null) // se asigna abajo para no cerrar si hay errores
                .setNegativeButton("Cancelar", null)
                .create();

        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String correo = inputCorreo.getText().toString().trim();
            String nombre = inputNombre.getText().toString().trim();
            String nueva = inputNueva.getText().toString();
            String confirmar = inputConfirmar.getText().toString();

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                inputCorreo.setError("Correo no válido");
                return;
            }
            if (TextUtils.isEmpty(nombre)) {
                inputNombre.setError("Rellena este campo.");
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

            if (dbHelper.cambiarContrasena(correo, nombre, nueva)) {
                Toast.makeText(this, "Contraseña actualizada. Ya puedes iniciar sesión", Toast.LENGTH_SHORT).show();
                etCorreo.setText(correo);
                etContrasena.setText("");
                dialogo.dismiss();
            } else {
                Toast.makeText(this, "El correo y el nombre no coinciden con ninguna cuenta", Toast.LENGTH_SHORT).show();
            }
        }));

        dialogo.show();
    }

    private void guardarSesion(int idUsuario) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_SESION, MODE_PRIVATE).edit();
        editor.putInt(KEY_ID_USUARIO, idUsuario);
        editor.apply();
    }

    private void abrirInicio(int idUsuario) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("id_usuario", idUsuario);
        startActivity(intent);
        finish();
    }
}
