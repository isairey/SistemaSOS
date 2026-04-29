package com.main.healinksos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicialización de los componentes de la interfaz de usuario
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPass = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegistro = findViewById(R.id.tvGoToRegister);

        // Configuración del evento de inicio de sesión
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            // Validación de integridad para campos obligatorios
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese sus credenciales de acceso", Toast.LENGTH_SHORT).show();
                return;
            }
            validarLoginConAPI(email, pass);
        });

        // Evento para navegar a la actividad de registro de nuevos usuarios
        tvRegistro.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    /**
     * Transmite las credenciales al servidor para la autenticación del usuario.
     */
    private void validarLoginConAPI(String email, String password) {
        String url = BuildConfig.API_BASE_URL + ":3001/auth/login";

        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(),
                MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        // Procesamiento de la respuesta JSON tras una autenticación exitosa
                        JSONObject resObj = new JSONObject(response.body().string());
                        int idUsuario = resObj.getInt("id");

                        // Redirección al Dashboard principal transfiriendo el ID del usuario
                        runOnUiThread(() -> {
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            intent.putExtra("id_usuario", idUsuario);
                            startActivity(intent);
                            finish();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}