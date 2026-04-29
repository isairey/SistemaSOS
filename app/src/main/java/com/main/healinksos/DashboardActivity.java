package com.main.healinksos;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardActivity extends AppCompatActivity {

    private int idUsuarioActual;
    private TextView tvBpm;
    private MapView mapView;
    private Marker userMarker;
    private Handler handler = new Handler();
    private final OkHttpClient client = new OkHttpClient();

    private LinearLayout layoutHome, layoutContacts, layoutMeds;

    // Nuestro nuevo motor de telemetría
    private NoSQLSimulator simuladorTelemetria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_dashboard);

        idUsuarioActual = getIntent().getIntExtra("id_usuario", 0);
        tvBpm = findViewById(R.id.tvBPMValue);

        layoutHome = findViewById(R.id.layout_home_content);
        layoutContacts = findViewById(R.id.layout_contacts_content);
        layoutMeds = findViewById(R.id.layout_meds_content);
        TextView tvUserInitial = findViewById(R.id.tvUserInitial);

        solicitarPermisos();
        configurarMapa();

        // Cargar inicial y configurar menú del perfil
        cargarInicialUsuario(tvUserInitial);

        tvUserInitial.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(DashboardActivity.this, v);
            popup.getMenu().add("Configurar Perfil");
            popup.getMenu().add("Cerrar Sesión");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Configurar Perfil")) {
                    Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                    intent.putExtra("id_usuario", idUsuarioActual);
                    startActivity(intent);
                } else if (item.getTitle().equals("Cerrar Sesión")) {
                    Toast.makeText(DashboardActivity.this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return true;
            });
            popup.show();
        });

        findViewById(R.id.btnAddContact).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddContactActivity.class);
            intent.putExtra("id_usuario", idUsuarioActual);
            startActivity(intent);
        });

        findViewById(R.id.btnAddMed).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMedicineActivity.class);
            intent.putExtra("id_usuario", idUsuarioActual);
            startActivity(intent);
        });

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            layoutHome.setVisibility(View.GONE);
            layoutContacts.setVisibility(View.GONE);
            layoutMeds.setVisibility(View.GONE);

            if (id == R.id.nav_home) {
                layoutHome.setVisibility(View.VISIBLE);
            } else if (id == R.id.nav_contacts) {
                layoutContacts.setVisibility(View.VISIBLE);
                obtenerContactosSQL();
            } else if (id == R.id.nav_meds) {
                layoutMeds.setVisibility(View.VISIBLE);
                obtenerMedicamentosSQL();
            }
            return true;
        });

        simuladorTelemetria = new NoSQLSimulator();

        // 1. Iniciar los latidos normales en segundo plano
        simuladorTelemetria.iniciarSimulacionNormal(idUsuarioActual, (bpm, lat, lng) -> {
            runOnUiThread(() -> {
                tvBpm.setText(bpm + " BPM");
                tvBpm.setTextColor(android.graphics.Color.parseColor("#6200EE")); // Corrección del color morado
                actualizarMarcadorMapa(lat, lng);
            });
        });

        // 2. Configurar el botón de pánico
        View btnSosEmergencia = findViewById(R.id.btnSOS);
        if (btnSosEmergencia != null) {
            btnSosEmergencia.setOnClickListener(v -> {
                Toast.makeText(this, "¡ALERTA SOS ENVIADA!", Toast.LENGTH_LONG).show();

                simuladorTelemetria.dispararAlertaSOS(idUsuarioActual, (bpm, lat, lng) -> {
                    runOnUiThread(() -> {
                        tvBpm.setText(bpm + " BPM ⚠️");
                        tvBpm.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); // Pone el BPM en rojo
                        actualizarMarcadorMapa(lat, lng);
                    });
                });
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Matamos el ciclo infinito del simulador al salir de la app
        if (simuladorTelemetria != null) {
            simuladorTelemetria.detenerSimulacion();
        }
    }

    private void actualizarMarcadorMapa(double lat, double lng) {
        if (mapView == null) return;

        GeoPoint punto = new GeoPoint(lat, lng);
        mapView.getController().setCenter(punto);

        if (userMarker == null) {
            userMarker = new Marker(mapView);
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            userMarker.setTitle("Ubicación del Usuario");
            mapView.getOverlays().add(userMarker);
        }
        userMarker.setPosition(punto);
        mapView.invalidate(); // Refresca el mapa visualmente
    }

    private void cargarInicialUsuario(TextView tvAvatar) {
        String url = BuildConfig.API_BASE_URL + ":3003/user/perfil/" + idUsuarioActual;

        client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject perfil = new JSONObject(response.body().string());
                        String nombre = perfil.getString("nombreCompleto");

                        if (!nombre.isEmpty()) {
                            String inicial = nombre.substring(0, 1).toUpperCase();
                            runOnUiThread(() -> tvAvatar.setText(inicial));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void obtenerMedicamentosSQL() {
        String url = BuildConfig.API_BASE_URL + ":3002/admin/medicamento/usuario/" + idUsuarioActual;

        client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        final JSONArray array = new JSONArray(response.body().string());
                        runOnUiThread(() -> {
                            LinearLayout container = findViewById(R.id.list_meds_container);
                            container.removeAllViews();
                            for (int i = 0; i < array.length(); i++) {
                                try {
                                    inflarMedicina(array.getJSONObject(i), container);
                                } catch (JSONException e) { e.printStackTrace(); }
                            }
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            @Override public void onFailure(Call call, IOException e) {}
        });
    }

    private void inflarMedicina(JSONObject obj, LinearLayout container) throws JSONException {
        View view = getLayoutInflater().inflate(R.layout.item_medicinas, null);
        final int idMedicina = obj.getInt("id");

        JSONArray horarios = obj.getJSONArray("horarios");
        JSONObject primerHorario = horarios.getJSONObject(0);

        String horaUTC = primerHorario.getString("horaInicio");
        int frecuencia = primerHorario.getInt("frecuenciaHoras");
        String proximaHora = calcularProximaDosis(horaUTC, frecuencia);

        ((TextView)view.findViewById(R.id.tvNombreMedItem)).setText(obj.getString("nombreFarmaco"));
        ((TextView)view.findViewById(R.id.tvInfoMedItem)).setText(obj.getString("dosis") + " - Cada " + frecuencia + "h");
        ((TextView)view.findViewById(R.id.tvHoraMedItem)).setText("Próxima: " + proximaHora);

        view.findViewById(R.id.btnEliminarItem).setOnClickListener(v -> {
            eliminarRecursoAPI("/admin/medicamento/" + idMedicina, this::obtenerMedicamentosSQL);
        });

        container.addView(view);
    }

    private String calcularProximaDosis(String horaInicio, int frecuencia) {
        try {
            SimpleDateFormat sdfUTC = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date dateInicio = sdfUTC.parse(horaInicio);

            SimpleDateFormat sdfLocal = new SimpleDateFormat("HH:mm", Locale.getDefault());
            sdfLocal.setTimeZone(TimeZone.getDefault());

            Calendar cal = Calendar.getInstance();
            if (dateInicio != null) {
                cal.setTime(dateInicio);
                cal.add(Calendar.HOUR_OF_DAY, frecuencia);
                return sdfLocal.format(cal.getTime());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "--:--";
    }

    private void obtenerContactosSQL() {
        String url = BuildConfig.API_BASE_URL + ":3002/admin/contacto/usuario/" + idUsuarioActual;

        client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        final JSONArray array = new JSONArray(response.body().string());
                        runOnUiThread(() -> {
                            LinearLayout listContainer = findViewById(R.id.list_contacts_container);
                            listContainer.removeAllViews();
                            for (int i = 0; i < array.length(); i++) {
                                try {
                                    inflarContacto(array.getJSONObject(i), listContainer);
                                } catch (JSONException e) { e.printStackTrace(); }
                            }
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            @Override public void onFailure(Call call, IOException e) {}
        });
    }

    private void inflarContacto(JSONObject obj, LinearLayout container) throws JSONException {
        View view = getLayoutInflater().inflate(R.layout.item_contacto, null);
        final int idContacto = obj.getInt("id");

        ((TextView)view.findViewById(R.id.tvNombreCont)).setText(obj.getString("nombreContacto"));
        ((TextView)view.findViewById(R.id.tvTelCont)).setText(obj.getString("telefono"));
        ((TextView)view.findViewById(R.id.tvPrioridadIcon)).setText(String.valueOf(obj.getInt("prioridad")));

        view.findViewById(R.id.btnEliminarItem).setOnClickListener(v -> {
            eliminarRecursoAPI("/admin/contacto/" + idContacto, this::obtenerContactosSQL);
        });

        container.addView(view);
    }

    private void eliminarRecursoAPI(String endpoint, Runnable onSuccess) {
        String url = BuildConfig.API_BASE_URL + ":3002" + endpoint;
        Request request = new Request.Builder().url(url).delete().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(DashboardActivity.this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                        onSuccess.run();
                    });
                }
            }
            @Override public void onFailure(Call call, IOException e) {}
        });
    }

    private void configurarMapa() {
        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(17.0);
    }

    private void solicitarPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
    }
}