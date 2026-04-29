package com.main.healinksos;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoSQLSimulator {

    private final OkHttpClient client = new OkHttpClient();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private boolean simulando = false;
    private final Random random = new Random();

    // Coordenadas base (Ej. Zócalo CDMX)
    private double baseLat = 19.432600;
    private double baseLng = -99.133200;

    public interface OnTelemetryUpdateListener {
        void onUpdate(int bpm, double lat, double lng);
    }

    // Arranca el ciclo infinito que manda datos cada 5 segundos
    public void iniciarSimulacionNormal(int idUsuarioReal, OnTelemetryUpdateListener listener) {
        if (simulando) return;
        simulando = true;

        runnable = new Runnable() {
            @Override
            public void run() {
                // Fluctuación de latidos sanos (60 a 85)
                int bpmNormal = 60 + random.nextInt(26);

                // Movimiento ligero en el mapa
                double latActual = baseLat + (random.nextDouble() - 0.5) * 0.0005;
                double lngActual = baseLng + (random.nextDouble() - 0.5) * 0.0005;

                enviarDatosAPI(idUsuarioReal, false, bpmNormal, latActual, lngActual);
                listener.onUpdate(bpmNormal, latActual, lngActual);

                // Repetir cada 5 segundos
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(runnable);
    }

    public void detenerSimulacion() {
        simulando = false;
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    // Método que el botón SOS va a llamar
    public void dispararAlertaSOS(int idUsuarioReal, OnTelemetryUpdateListener listener) {
        // En una emergencia el corazón se acelera (110 a 140)
        int bpmPanico = 110 + random.nextInt(31);

        enviarDatosAPI(idUsuarioReal, true, bpmPanico, baseLat, baseLng);
        listener.onUpdate(bpmPanico, baseLat, baseLng);
    }

    private void enviarDatosAPI(int idUsuario, boolean esAlertaSOS, int bpm, double lat, double lng) {
        try {
            String idDispositivo = "WATCH_USER_" + String.format(Locale.US, "%03d", idUsuario);

            JSONObject json = new JSONObject();
            json.put("id_dispositivo", idDispositivo);
            json.put("id_usuario", idUsuario);
            json.put("alerta_sos", esAlertaSOS);

            JSONObject biometria = new JSONObject();
            biometria.put("temperatura", 36.5);
            biometria.put("bpm", bpm);
            biometria.put("oxigeno", 96);
            json.put("biometria", biometria);

            JSONObject ubicacion = new JSONObject();
            ubicacion.put("latitud", lat);
            ubicacion.put("longitud", lng);
            json.put("ubicacion", ubicacion);

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

            String url = BuildConfig.API_BASE_URL + ":3004/sos/telemetria";
            Request request = new Request.Builder().url(url).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("HeaLink_SOS", "Falló la conexión al API: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("HeaLink_SOS", "Datos enviados. SOS=" + esAlertaSOS + " | Código: " + response.code());
                    response.close();
                }
            });

        } catch (Exception e) {
            Log.e("HeaLink_SOS", "Error creando JSON: " + e.getMessage());
        }
    }
}