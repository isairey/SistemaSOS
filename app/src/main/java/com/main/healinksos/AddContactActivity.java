package com.main.healinksos;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddContactActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();
    private int idUsuarioActual; // Estandarizado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        // Obtenemos el ID enviado desde el Dashboard
        idUsuarioActual = getIntent().getIntExtra("id_usuario", 0);

        TextInputEditText etNombre = findViewById(R.id.etNombreCont);
        TextInputEditText etTel = findViewById(R.id.etTelCont);
        AutoCompleteTextView autoPrio = findViewById(R.id.autoPrio);
        MaterialButton btnGuardar = findViewById(R.id.btnGuardarCont);

        String[] niveles = {"1", "2", "3", "4", "5"};
        autoPrio.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, niveles));

        btnGuardar.setOnClickListener(v -> {
            String nom = etNombre.getText().toString().trim();
            String tel = etTel.getText().toString().trim();
            String prio = autoPrio.getText().toString();

            if (nom.isEmpty() || tel.isEmpty() || prio.isEmpty()) {
                Toast.makeText(this, "Llene todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            enviarContactoAPI(nom, tel, Integer.parseInt(prio));
        });

        findViewById(R.id.btnCancelarCont).setOnClickListener(v -> finish());
    }

    private void enviarContactoAPI(String nom, String tel, int prio) {
        String url = BuildConfig.API_BASE_URL + ":3002/admin/contacto";

        JSONObject json = new JSONObject();
        try {
            json.put("idUsuario", idUsuarioActual); // Usamos la variable estandarizada
            json.put("nombreContacto", nom);
            json.put("telefono", tel);
            json.put("prioridad", prio);
        } catch (JSONException e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(json.toString(),
                MediaType.parse("application/json; charset=utf-8"));

        client.newCall(new Request.Builder().url(url).post(body).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AddContactActivity.this, "Error de red", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddContactActivity.this, "Contacto guardado", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }
        });
    }
}