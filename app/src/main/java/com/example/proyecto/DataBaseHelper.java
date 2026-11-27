package com.example.proyecto;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MascotasDB.db";
    private static final int DATABASE_VERSION = 1;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE usuarios (" +
                "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "correo TEXT NOT NULL UNIQUE, " +
                "direccion TEXT, " +
                "contrasena_hash TEXT NOT NULL)");

        db.execSQL("CREATE TABLE mascotas (" +
                "id_mascota INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "foto TEXT, " +
                "nombre TEXT NOT NULL, " +
                "raza TEXT, " +
                "ano_nacimiento INTEGER, " +
                "peso REAL, " +
                "esterilizado INTEGER DEFAULT 0, " +
                "sexo INTEGER NOT NULL, " +
                "id_usuario INTEGER NOT NULL, " +
                "FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE inyecciones (" +
                "id_inyeccion INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "periodicidad_dias INTEGER, " +
                "folio_lote TEXT NOT NULL, " +
                "numero_registro TEXT NOT NULL, " +
                "descripcion TEXT)");

        db.execSQL("CREATE TABLE historial_vacunacion (" +
                "id_historial INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_mascota INTEGER NOT NULL, " +
                "id_inyeccion INTEGER NOT NULL, " +
                "fecha_aplicacion TEXT NOT NULL, " +
                "proxima_aplicacion TEXT, " +
                "FOREIGN KEY (id_mascota) REFERENCES mascotas(id_mascota) ON DELETE CASCADE, " +
                "FOREIGN KEY (id_inyeccion) REFERENCES inyecciones(id_inyeccion))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS historial_vacunacion");
        db.execSQL("DROP TABLE IF EXISTS mascotas");
        db.execSQL("DROP TABLE IF EXISTS inyecciones");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ===================== USUARIOS / SESIÓN =====================

    /** Convierte la contraseña a SHA-256 (hex) para no guardarla en texto plano. */
    public static String hashContrasena(String contrasena) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(contrasena.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /**
     * Registra un usuario nuevo.
     * @return id del usuario creado, o -1 si el correo ya existe o hubo error.
     */
    public long registrarUsuario(String nombre, String correo, String contrasena) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("correo", correo.trim().toLowerCase(Locale.ROOT));
        values.put("direccion", "");
        values.put("contrasena_hash", hashContrasena(contrasena));
        return db.insertWithOnConflict("usuarios", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * Valida correo y contraseña.
     * @return id del usuario si las credenciales son correctas, o -1 si no.
     */
    public int validarUsuario(String correo, String contrasena) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id_usuario FROM usuarios WHERE correo = ? AND contrasena_hash = ? LIMIT 1",
                new String[]{correo.trim().toLowerCase(Locale.ROOT), hashContrasena(contrasena)}
        );
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return -1;
    }

    /**
     * Cambia la contraseña de un usuario verificando su correo y nombre registrado.
     * @return true si se actualizó, false si no coincide ningún usuario.
     */
    public boolean cambiarContrasena(String correo, String nombre, String nuevaContrasena) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("contrasena_hash", hashContrasena(nuevaContrasena));
        int filas = db.update("usuarios", values,
                "correo = ? AND LOWER(nombre) = LOWER(?)",
                new String[]{correo.trim().toLowerCase(Locale.ROOT), nombre.trim()});
        return filas > 0;
    }

    // ===================== CONFIGURACIÓN =====================

    /** @return {nombre, correo, direccion} del usuario, o null si no existe. */
    public String[] obtenerDatosUsuario(int idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT nombre, correo, direccion FROM usuarios WHERE id_usuario = ? LIMIT 1",
                new String[]{String.valueOf(idUsuario)}
        );
        try {
            if (cursor.moveToFirst()) {
                String direccion = cursor.getString(2);
                return new String[]{cursor.getString(0), cursor.getString(1), direccion == null ? "" : direccion};
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    /** @return false si el correo ya lo usa otra cuenta. */
    public boolean actualizarPerfil(int idUsuario, String nombre, String correo, String direccion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("correo", correo.trim().toLowerCase(Locale.ROOT));
        values.put("direccion", direccion);
        try {
            return db.update("usuarios", values, "id_usuario = ?",
                    new String[]{String.valueOf(idUsuario)}) > 0;
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            return false;
        }
    }

    /** Cambia la contraseña solo si la actual es correcta. */
    public boolean cambiarContrasenaPorId(int idUsuario, String actual, String nueva) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("contrasena_hash", hashContrasena(nueva));
        return db.update("usuarios", values, "id_usuario = ? AND contrasena_hash = ?",
                new String[]{String.valueOf(idUsuario), hashContrasena(actual)}) > 0;
    }

    /** Borra todas las mascotas del usuario (su historial se borra en cascada). */
    public int borrarMascotasUsuario(int idUsuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("mascotas", "id_usuario = ?", new String[]{String.valueOf(idUsuario)});
    }

    public String obtenerNombreUsuario(int idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        String nombre = "Usuario";

        Cursor cursor = db.rawQuery(
                "SELECT nombre FROM usuarios WHERE id_usuario = ? LIMIT 1",
                new String[]{String.valueOf(idUsuario)}
        );

        try {
            if (cursor.moveToFirst()) {
                String valor = cursor.getString(0);
                if (valor != null && !valor.trim().isEmpty()) {
                    nombre = valor.trim();
                }
            }
        } finally {
            cursor.close();
        }
        return nombre;
    }

    public List<MascotaCartilla> obtenerCartillasPorUsuario(int idUsuario) {
        List<MascotaCartilla> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT m.id_mascota, m.nombre, m.raza, m.ano_nacimiento, m.foto, " +
                "COUNT(h.id_historial) AS total_vacunas " +
                "FROM mascotas m " +
                "LEFT JOIN historial_vacunacion h ON m.id_mascota = h.id_mascota " +
                "WHERE m.id_usuario = ? " +
                "GROUP BY m.id_mascota";

        int anioActual = Calendar.getInstance().get(Calendar.YEAR);

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(idUsuario)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    int idMascota = cursor.getInt(0);
                    String nombre = cursor.getString(1);
                    String raza = cursor.getString(2);
                    int anoNacimiento = cursor.getInt(3);
                    String foto = cursor.getString(4);
                    int aplicadas = cursor.getInt(5);

                    int edadAnios = anoNacimiento > 0 ? Math.max(0, anioActual - anoNacimiento) : 0;
                    String edadTexto = edadAnios + (edadAnios == 1 ? " año" : " años");

                    if (raza == null || raza.trim().isEmpty()) {
                        raza = "Mestizo";
                    }

                    // Mientras no haya catálogo de totales por especie, usamos el total registrado
                    lista.add(new MascotaCartilla(
                            idMascota, nombre, raza, edadTexto, foto, aplicadas, aplicadas
                    ));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return lista;
    }

    public boolean insertarMascotaConVacunas(int idUsuario, String nombre, String raza, int edadAnios,
                                             String fotoUri, int aplicadas, int totales) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            int anioActual = Calendar.getInstance().get(Calendar.YEAR);
            int anoNacimiento = edadAnios > 0 ? (anioActual - edadAnios) : anioActual;

            ContentValues valuesMascota = new ContentValues();
            valuesMascota.put("id_usuario", idUsuario);
            valuesMascota.put("nombre", nombre);
            valuesMascota.put("raza", raza);
            valuesMascota.put("ano_nacimiento", anoNacimiento);
            valuesMascota.put("peso", 0.0);
            valuesMascota.put("esterilizado", 0);
            valuesMascota.put("sexo", 0);
            valuesMascota.put("foto", fotoUri);

            long idMascota = db.insert("mascotas", null, valuesMascota);
            if (idMascota == -1) {
                return false;
            }

            if (aplicadas > 0) {
                long idInyeccionBase = obtenerOCrearInyeccionBase(db);
                String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                for (int i = 1; i <= aplicadas; i++) {
                    ContentValues valuesHist = new ContentValues();
                    valuesHist.put("id_mascota", idMascota);
                    valuesHist.put("id_inyeccion", idInyeccionBase);
                    valuesHist.put("fecha_aplicacion", hoy);
                    valuesHist.put("proxima_aplicacion", "");
                    db.insert("historial_vacunacion", null, valuesHist);
                }
            }

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    private long obtenerOCrearInyeccionBase(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT id_inyeccion FROM inyecciones LIMIT 1", null);
        try {
            if (c.moveToFirst()) {
                return c.getLong(0);
            }
        } finally {
            c.close();
        }

        ContentValues values = new ContentValues();
        values.put("nombre", "Dosis inicial");
        values.put("periodicidad_dias", 365);
        values.put("folio_lote", "LOTE-001");
        values.put("numero_registro", "REG-001");
        values.put("descripcion", "Registro inicial de cartilla");
        return db.insert("inyecciones", null, values);
    }

    private void asegurarUsuarioExistente(SQLiteDatabase db, int idUsuario) {
        Cursor c = db.rawQuery(
                "SELECT id_usuario FROM usuarios WHERE id_usuario = ? LIMIT 1",
                new String[]{String.valueOf(idUsuario)}
        );
        try {
            if (c.moveToFirst()) {
                return;
            }
        } finally {
            c.close();
        }

        ContentValues values = new ContentValues();
        if (idUsuario > 0) {
            values.put("id_usuario", idUsuario);
        }
        values.put("nombre", "Usuario");
        values.put("correo", "usuario" + (idUsuario > 0 ? idUsuario : 1) + "@local.app");
        values.put("direccion", "");
        values.put("contrasena_hash", "local");
        db.insertWithOnConflict("usuarios", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public boolean insertarMascotaConVacunas(int idUsuario, String nombre, String raza, int edadAnios,
                                             double peso, int esterilizado, int sexo,
                                             String fotoUri, int aplicadas, int totales) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            asegurarUsuarioExistente(db, idUsuario);

            int anioActual = Calendar.getInstance().get(Calendar.YEAR);
            int anoNacimiento = edadAnios > 0 ? (anioActual - edadAnios) : anioActual;

            ContentValues valuesMascota = new ContentValues();
            valuesMascota.put("id_usuario", idUsuario);
            valuesMascota.put("nombre", nombre);
            valuesMascota.put("raza", raza);
            valuesMascota.put("ano_nacimiento", anoNacimiento);
            valuesMascota.put("peso", peso);
            valuesMascota.put("esterilizado", esterilizado);
            valuesMascota.put("sexo", sexo);
            valuesMascota.put("foto", fotoUri);

            long idMascota = db.insert("mascotas", null, valuesMascota);
            if (idMascota == -1) {
                return false;
            }

            if (aplicadas > 0) {
                long idInyeccionBase = obtenerOCrearInyeccionBase(db);
                String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                for (int i = 1; i <= aplicadas; i++) {
                    ContentValues valuesHist = new ContentValues();
                    valuesHist.put("id_mascota", idMascota);
                    valuesHist.put("id_inyeccion", idInyeccionBase);
                    valuesHist.put("fecha_aplicacion", hoy);
                    valuesHist.put("proxima_aplicacion", "");
                    db.insert("historial_vacunacion", null, valuesHist);
                }
            }

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public boolean insertarMascotaConVacunas(int idUsuario, String nombre, String raza, int edadAnios,
                                             double peso, int esterilizado, int sexo,
                                             String fotoUri, List<VacunaPendiente> vacunas) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            asegurarUsuarioExistente(db, idUsuario);

            int anioActual = Calendar.getInstance().get(Calendar.YEAR);
            int anoNacimiento = edadAnios > 0 ? (anioActual - edadAnios) : anioActual;

            ContentValues valuesMascota = new ContentValues();
            valuesMascota.put("id_usuario", idUsuario);
            valuesMascota.put("nombre", nombre);
            valuesMascota.put("raza", raza);
            valuesMascota.put("ano_nacimiento", anoNacimiento);
            valuesMascota.put("peso", peso);
            valuesMascota.put("esterilizado", esterilizado);
            valuesMascota.put("sexo", sexo);
            valuesMascota.put("foto", fotoUri);

            long idMascota = db.insert("mascotas", null, valuesMascota);
            if (idMascota == -1) {
                return false;
            }

            if (vacunas != null) {
                for (VacunaPendiente v : vacunas) {
                    ContentValues valuesInyeccion = new ContentValues();
                    valuesInyeccion.put("nombre", v.getNombre());
                    if (v.getPeriodicidadDias() != null) {
                        valuesInyeccion.put("periodicidad_dias", v.getPeriodicidadDias());
                    } else {
                        valuesInyeccion.putNull("periodicidad_dias");
                    }
                    valuesInyeccion.put("folio_lote", v.getFolioLote());
                    valuesInyeccion.put("numero_registro", v.getNumeroRegistro());
                    valuesInyeccion.put("descripcion", v.getDescripcion());

                    long idInyeccion = db.insert("inyecciones", null, valuesInyeccion);
                    if (idInyeccion == -1) {
                        return false;
                    }

                    ContentValues valuesHist = new ContentValues();
                    valuesHist.put("id_mascota", idMascota);
                    valuesHist.put("id_inyeccion", idInyeccion);
                    valuesHist.put("fecha_aplicacion", v.getFechaAplicacion());
                    valuesHist.put("proxima_aplicacion", v.getProximaAplicacion());

                    long idHist = db.insert("historial_vacunacion", null, valuesHist);
                    if (idHist == -1) {
                        return false;
                    }
                }
            }

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public MascotaDetalle obtenerDetalleMascota(int idMascota) {
        SQLiteDatabase db = this.getReadableDatabase();
        MascotaDetalle detalle = null;

        Cursor cMascota = db.rawQuery(
                "SELECT id_mascota, foto, nombre, raza, ano_nacimiento, peso, esterilizado, sexo " +
                        "FROM mascotas WHERE id_mascota = ? LIMIT 1",
                new String[]{String.valueOf(idMascota)}
        );

        try {
            if (cMascota.moveToFirst()) {
                String foto = cMascota.getString(1);
                String nombre = cMascota.getString(2);
                String raza = cMascota.getString(3);
                int anoNacimiento = cMascota.getInt(4);
                double peso = cMascota.getDouble(5);
                int esterilizado = cMascota.getInt(6);
                int sexo = cMascota.getInt(7);

                List<VacunaDetalle> vacunas = new ArrayList<>();
                Cursor cVacunas = db.rawQuery(
                        "SELECT i.nombre, i.periodicidad_dias, i.folio_lote, i.numero_registro, " +
                                "i.descripcion, h.fecha_aplicacion, h.proxima_aplicacion " +
                                "FROM historial_vacunacion h " +
                                "INNER JOIN inyecciones i ON h.id_inyeccion = i.id_inyeccion " +
                                "WHERE h.id_mascota = ? " +
                                "ORDER BY h.fecha_aplicacion DESC, h.id_historial DESC",
                        new String[]{String.valueOf(idMascota)}
                );

                try {
                    if (cVacunas.moveToFirst()) {
                        do {
                            String nombreVac = cVacunas.getString(0);
                            Integer periodicidad = cVacunas.isNull(1) ? null : cVacunas.getInt(1);
                            String folio = cVacunas.getString(2);
                            String numReg = cVacunas.getString(3);
                            String desc = cVacunas.getString(4);
                            String fechaAp = cVacunas.getString(5);
                            String proxAp = cVacunas.getString(6);

                            vacunas.add(new VacunaDetalle(
                                    nombreVac, periodicidad, folio, numReg, desc, fechaAp, proxAp
                            ));
                        } while (cVacunas.moveToNext());
                    }
                } finally {
                    cVacunas.close();
                }

                detalle = new MascotaDetalle(
                        idMascota, foto, nombre, raza, anoNacimiento, peso, esterilizado, sexo, vacunas
                );
            }
        } finally {
            cMascota.close();
        }

        return detalle;
    }
}