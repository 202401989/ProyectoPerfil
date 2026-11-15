package com.efer.proyectoperfil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    EditText txtCorreo, txtPassword;
    Button btnLogin, btnRegistro;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtCorreo = findViewById(R.id.txtCorreo);
        txtPassword = findViewById(R.id.txtPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegistro = findViewById(R.id.btnRegistro);

        btnLogin.setOnClickListener(v -> iniciarSesion());

        btnRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,
                    RegistroActivity.class);
            startActivity(intent);
        });
    }

    private void iniciarSesion() {

        String correo = txtCorreo.getText().toString().trim();
        String password = txtPassword.getText().toString();

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingresa tu correo y contraseña",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        executor.execute(() -> {

            HttpURLConnection conexion = null;

            try {
                URL url = new URL("http://10.0.2.2:3000/api/auth/login");

                conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Content-Type", "application/json");
                conexion.setDoOutput(true);
                conexion.setConnectTimeout(10000);
                conexion.setReadTimeout(10000);

                JSONObject datos = new JSONObject();
                datos.put("correo", correo);
                datos.put("password", password);

                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(datos.toString().getBytes(StandardCharsets.UTF_8));
                }

                int codigo = conexion.getResponseCode();

                InputStream entrada = (codigo >= 200 && codigo < 300)
                        ? conexion.getInputStream()
                        : conexion.getErrorStream();

                BufferedReader lector = new BufferedReader(
                        new InputStreamReader(entrada, StandardCharsets.UTF_8));

                StringBuilder respuesta = new StringBuilder();
                String linea;

                while ((linea = lector.readLine()) != null) {
                    respuesta.append(linea);
                }

                lector.close();

                if (codigo == 200) {

                    JSONObject resultado = new JSONObject(respuesta.toString());
                    JSONObject usuario = resultado.getJSONObject("usuario");

                    String nombre = usuario.getString("nombre");
                    String apellido = usuario.getString("apellido");
                    String correoUsuario = usuario.getString("correo");
                    String token = resultado.getString("token");

                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);

                        Intent intent = new Intent(MainActivity.this,
                                PerfilActivity.class);

                        intent.putExtra("nombre", nombre);
                        intent.putExtra("apellido", apellido);
                        intent.putExtra("correo", correoUsuario);
                        intent.putExtra("token", token);

                        startActivity(intent);
                    });

                } else {

                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        Toast.makeText(this,
                                "Correo o contraseña incorrectos",
                                Toast.LENGTH_LONG).show();
                    });
                }

            } catch (Exception e) {

                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
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