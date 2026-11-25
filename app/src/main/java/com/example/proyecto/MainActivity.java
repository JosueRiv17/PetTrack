package com.example.proyecto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private boolean tieneNotificaciones = true;
    private final int idUsuarioActual = 1;

    private DataBaseHelper dbHelper;
    private RecyclerView rvMascotas;

    private final ActivityResultLauncher<Intent> agregarMascotaLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    cargarCartillasMascotas();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (View v, WindowInsetsCompat insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Fecha de hoy
        TextView txtFecha = findViewById(R.id.txtFecha);
        Locale localeEs = new Locale("es", "MX");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE d 'de' MMMM 'del' yyyy", localeEs);
        String fechaRaw = sdf.format(calendar.getTime());
        String fechaFormateada = fechaRaw.substring(0, 1).toUpperCase(localeEs) + fechaRaw.substring(1);
        txtFecha.setText(fechaFormateada);

        // 2. Saludo con nombre de usuario desde SQLite
        dbHelper = new DataBaseHelper(this);
        TextView txtSaludo = findViewById(R.id.txtSaludo);

        String nombreUsuario = dbHelper.obtenerNombreUsuario(idUsuarioActual);
        if (nombreUsuario != null && !nombreUsuario.trim().isEmpty()) {
            txtSaludo.setText("Hola @" + nombreUsuario);
        } else {
            txtSaludo.setText("Hola @Usuario");
        }

        // 3. Miniaturas de cartillas en RecyclerView
        rvMascotas = findViewById(R.id.rvMascotas);
        rvMascotas.setLayoutManager(new LinearLayoutManager(this));
        cargarCartillasMascotas();

        // 4. Eventos de la barra superior
        ImageView ivAjustes = findViewById(R.id.ivAjustes);
        ImageView ivCampana = findViewById(R.id.ivCampana);

        ivAjustes.setOnClickListener(v ->
                Toast.makeText(this, "Abrir Ajustes", Toast.LENGTH_SHORT).show()
        );

        ivCampana.setOnClickListener(v -> {
            if (tieneNotificaciones) {
                ivCampana.setImageResource(R.drawable.icon_sinnotificacion);
                tieneNotificaciones = false;
                Toast.makeText(this, "Notificaciones revisadas", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sin notificaciones nuevas", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Eventos de la barra inferior
        LinearLayout btnNavInicio = findViewById(R.id.btnNavInicio);
        LinearLayout btnNavAgregar = findViewById(R.id.btnNavAgregar);
        LinearLayout btnNavCuenta = findViewById(R.id.btnNavCuenta);

        btnNavInicio.setOnClickListener(v -> cargarCartillasMascotas());

        btnNavAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AgregarMascota.class);
            intent.putExtra("id_usuario", idUsuarioActual);
            agregarMascotaLauncher.launch(intent);
        });

        btnNavCuenta.setOnClickListener(v ->
                Toast.makeText(this, "Cuenta", Toast.LENGTH_SHORT).show()
        );
    }

    private void cargarCartillasMascotas() {
        List<MascotaCartilla> cartillas = dbHelper.obtenerCartillasPorUsuario(idUsuarioActual);
        MascotaCartillaAdapter adapter = new MascotaCartillaAdapter(cartillas, mascota ->
                Toast.makeText(this, "Cartilla de " + mascota.getNombre(), Toast.LENGTH_SHORT).show()
        );
        rvMascotas.setAdapter(adapter);
    }
}