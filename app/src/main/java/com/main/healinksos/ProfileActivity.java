package com.main.healinksos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    private int idUsuarioActual;
    private TextInputEditText etNombre, etFecha, etAlergias, etHistorial;
    private AutoCompleteTextView etSangre;
    private TextView tvAvatarBig;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        idUsuarioActual = getIntent().getIntExtra("id_usuario", 0);

        etNombre = findViewById(R.id.etProfileNombre);
        etFecha = findViewById(R.id.etProfileFecha);
        etSangre = findViewById(R.id.etProfileSangre);
        etAlergias = findViewById(R.id.etProfileAlergias);
        etHistorial = findViewById(R.id.etProfileHistorial);
        tvAvatarBig = findViewById(R.id.tvProfileAvatarBig);

        String[] tiposSangre = new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tiposSangre);
        etSangre.setAdapter(adapter);

        cargarDatosPerfil();

        findViewById(R.id.btnGuardarPerfil).setOnClickListener(v -> guardarCambios());
        findViewById(R.id.btnEliminarCuenta).setOnClickListener(v -> confirmarEliminacion());
        findViewById(R.id.btnCancelarPerfil).setOnClickListener(v -> finish());
    }

    private void cargarDatosPerfil() {
        String url = BuildConfig.API_BASE_URL + ":3003/user/perfil/" + idUsuarioActual;

        client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());

                        String nombre = json.getString("nombreCompleto");
                        String fechaUTC = json.getString("fechaNacimiento");
                        String tipoSangre = json.optString("tipoSangre", "");
                        String alergias = json.optString("alergias", "");
                        String historial = json.optString("historialMedico", "");

                        String fechaLocal = convertirUTCA_Local(fechaUTC);

                        runOnUiThread(() -> {
                            etNombre.setText(nombre);
                            etFecha.setText(fechaLocal);
                            etSangre.setText(tipoSangre.equals("null") ? "" : tipoSangre, false);
                            etAlergias.setText(alergias.equals("null") ? "" : alergias);
                            etHistorial.setText(historial.equals("null") ? "" : historial);

                            if (!nombre.isEmpty()) {
                                tvAvatarBig.setText(nombre.substring(0, 1).toUpperCase());
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override public void onFailure(Call call, IOException e) {}
        });
    }

    private void guardarCambios() {
        String url = BuildConfig.API_BASE_URL + ":3003/user/perfil/" + idUsuarioActual;

        JSONObject json = new JSONObject();
        try {
            json.put("tipoSangre", etSangre.getText().toString());
            json.put("alergias", etAlergias.getText().toString());
            json.put("historialMedico", etHistorial.getText().toString());
        } catch (JSONException e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).put(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override public void onFailure(Call call, IOException e) {}
        });
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar cuenta?")
                .setMessage("Esta acción no se puede deshacer. Se borrarán todas tus medicinas, contactos e historial médico.")
                .setPositiveButton("Sí, eliminar todo", (dialog, which) -> ejecutarEliminacionAPI())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarEliminacionAPI() {
        String url = BuildConfig.API_BASE_URL + ":3003/user/perfil/" + idUsuarioActual;
        Request request = new Request.Builder().url(url).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Cuenta eliminada correctamente", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(ProfileActivity.this, "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override public void onFailure(Call call, IOException e) {}
        });
    }

    private String convertirUTCA_Local(String fechaUTC) {
        try {
            SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdfUTC.parse(fechaUTC);

            SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdfLocal.setTimeZone(TimeZone.getDefault());

            if (date != null) {
                return sdfLocal.format(date);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return fechaUTC;
    }
}