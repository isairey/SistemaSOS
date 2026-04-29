package com.main.healinksos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();

    private AutoCompleteTextView autoSangre;
    private EditText etFecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicialización de los componentes de la interfaz de usuario
        EditText etNombre = findViewById(R.id.etNombre);
        EditText etEmail = findViewById(R.id.etEmailReg);
        etFecha = findViewById(R.id.etFechaNacimiento);
        autoSangre = findViewById(R.id.autoCompleteSangre);
        EditText etAlergias = findViewById(R.id.etAlergias);
        EditText etHistorial = findViewById(R.id.etHistorial);
        EditText etPassword = findViewById(R.id.etRegPass);
        Button btnRegistrar = findViewById(R.id.btnRegister);
        Button btnVolver = findViewById(R.id.btnBack);

        // Configuración del adaptador para el menú desplegable de tipo de sangre
        String[] tiposSangre = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                tiposSangre
        );
        autoSangre.setAdapter(adapter);

        // Configuración del evento para mostrar el DatePickerDialog
        etFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        RegisterActivity.this,
                        (view, yearSeleccionado, monthOfYear, dayOfMonth) -> {
                            // Formato de fecha requerido por la base de datos (YYYY-MM-DD)
                            // Se incrementa el mes en 1 debido a que Calendar indexa los meses desde 0
                            String fechaFormateada = String.format("%04d-%02d-%02d", yearSeleccionado, (monthOfYear + 1), dayOfMonth);
                            etFecha.setText(fechaFormateada);
                        },
                        year, month, day);

                datePickerDialog.show();
            }
        });

        // Configuración del evento de registro de usuario
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Extracción y limpieza de los datos de entrada
                String nom = etNombre.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String fechaTxt = etFecha.getText().toString().trim();
                String sangre = autoSangre.getText().toString();
                String alergias = etAlergias.getText().toString().trim();
                String historial = etHistorial.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                // Validaciones de integridad de datos
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(RegisterActivity.this, "Formato de correo electrónico inválido", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(nom.isEmpty() || email.isEmpty() || pass.isEmpty() || fechaTxt.isEmpty() || sangre.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Por favor, complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validación de longitud de la contraseña (Mínimo 8, Máximo 12)
                if(pass.length() < 8 || pass.length() > 12){
                    Toast.makeText(RegisterActivity.this, "La contraseña debe tener entre 8 y 12 caracteres", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validación de complejidad mediante Expresiones Regulares (Regex)
                // Reglas: Al menos una mayúscula, al menos un número y al menos un símbolo especial
                String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).+$";
                if (!pass.matches(passwordRegex)) {
                    Toast.makeText(RegisterActivity.this, "La contraseña debe incluir al menos una mayúscula, un número y un símbolo especial", Toast.LENGTH_LONG).show();
                    return;
                }

                // Llamada al método para la transmisión de datos
                enviarDatosAPI(nom, email, fechaTxt, sangre, alergias, historial, pass);
            }
        });

        // Evento para finalizar la actividad actual y regresar a la pantalla anterior
        btnVolver.setOnClickListener(v -> finish());
    }

    /**
     * Construye y envía el payload JSON al servicio REST para el registro de un nuevo usuario.
     */
    private void enviarDatosAPI(String nombre, String email, String fechaNacimiento, String sangre, String alergias, String historial, String password) {
        String url = BuildConfig.API_BASE_URL + ":3001/auth/registrar";

        JSONObject json = new JSONObject();
        try {
            json.put("nombreCompleto", nombre);
            json.put("email", email);
            json.put("password", password);
            json.put("fechaNacimiento", fechaNacimiento);
            json.put("tipoSangre", sangre);
            json.put("alergias", alergias);
            json.put("historialMedico", historial);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, "Error de conexión con el servidor: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Perfil creado exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this, "Error: El correo electrónico ya se encuentra registrado", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}