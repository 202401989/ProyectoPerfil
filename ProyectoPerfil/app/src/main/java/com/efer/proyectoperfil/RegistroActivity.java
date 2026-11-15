package com.efer.proyectoperfil;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistroActivity extends AppCompatActivity {

    EditText txtNombre, txtApellido, txtCorreoRegistro, txtPasswordRegistro;
    Button btnRegistrar, btnVolver;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        txtNombre = findViewById(R.id.txtNombre);
        txtApellido = findViewById(R.id.txtApellido);
        txtCorreoRegistro = findViewById(R.id.txtCorreoRegistro);
        txtPasswordRegistro = findViewById(R.id.txtPasswordRegistro);

        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnVolver = findViewById(R.id.btnVolver);

        btnRegistrar.setOnClickListener(v -> registrarUsuario());

        btnVolver.setOnClickListener(v -> finish());
    }

    private void registrarUsuario() {

        String nombre = txtNombre.getText().toString().trim();
        String apellido = txtApellido.getText().toString().trim();
        String correo = txtCorreoRegistro.getText().toString().trim();
        String password = txtPasswordRegistro.getText().toString();

        if (nombre.isEmpty() || apellido.isEmpty()
                || correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegistrar.setEnabled(false);

        executor.execute(() -> {

            HttpURLConnection conexion = null;

            try {
                URL url = new URL("http://10.0.2.2:3000/api/auth/registro");

                conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Content-Type", "application/json");
                conexion.setDoOutput(true);
                conexion.setConnectTimeout(10000);
                conexion.setReadTimeout(10000);

                JSONObject datos = new JSONObject();
                datos.put("nombre", nombre);
                datos.put("apellido", apellido);
                datos.put("correo", correo);
                datos.put("password", password);

                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(datos.toString().getBytes(StandardCharsets.UTF_8));
                }

                int codigo = conexion.getResponseCode();

                runOnUiThread(() -> {
                    btnRegistrar.setEnabled(true);

                    if (codigo == 201) {
                        Toast.makeText(this,
                                "Usuario registrado correctamente",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else if (codigo == 409) {
                        Toast.makeText(this,
                                "El correo ya está registrado",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "No se pudo registrar. Código: " + codigo,
                                Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {

                runOnUiThread(() -> {
                    btnRegistrar.setEnabled(true);
                    Toast.makeText(this,
                            "Error de conexión: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });

            } finally {
                if (conexion != null) {
                    conexion.disconnect();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}