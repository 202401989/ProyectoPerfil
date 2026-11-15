package com.efer.proyectoperfil;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class PerfilActivity extends AppCompatActivity {

    ImageView imgPerfil;
    TextView lblNombre, lblApellido, lblCorreo;
    Button btnTomarFoto, btnGaleria, btnCerrarSesion;

    ActivityResultLauncher<Void> tomarFotoLauncher;

    ActivityResultLauncher<String> galeriaLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        imgPerfil = findViewById(R.id.imgPerfil);
        lblNombre = findViewById(R.id.lblNombre);
        lblApellido = findViewById(R.id.lblApellido);
        lblCorreo = findViewById(R.id.lblCorreo);

        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnGaleria = findViewById(R.id.btnGaleria);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        // Recibir información del usuario
        String nombre = getIntent().getStringExtra("nombre");
        String apellido = getIntent().getStringExtra("apellido");
        String correo = getIntent().getStringExtra("correo");

        lblNombre.setText(nombre);
        lblApellido.setText(apellido);
        lblCorreo.setText(correo);

        // Abrir cámara
        tomarFotoLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        imgPerfil.setImageBitmap(bitmap);
                    }
                }
        );

        // Abrir galería
        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        mostrarImagen(uri);
                    }
                }
        );

        btnTomarFoto.setOnClickListener(v -> {
            tomarFotoLauncher.launch(null);
        });

        btnGaleria.setOnClickListener(v -> {
            galeriaLauncher.launch("image/*");
        });

        btnCerrarSesion.setOnClickListener(v -> {
            finish();
        });
    }

    private void mostrarImagen(Uri uri) {
        try {
            Bitmap bitmap;

            if (Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.Source source =
                        ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), uri);
            }

            imgPerfil.setImageBitmap(bitmap);

        } catch (IOException e) {
            Toast.makeText(this,
                    "No se pudo cargar la imagen",
                    Toast.LENGTH_SHORT).show();
        }
    }
}