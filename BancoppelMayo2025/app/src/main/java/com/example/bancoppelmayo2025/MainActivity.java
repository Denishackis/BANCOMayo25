package com.example.bancoppelmayo2025;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast; // Para mensajes emergentes
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt; // Importar BiometricPrompt
import androidx.core.content.ContextCompat; // Importar ContextCompat
import java.util.concurrent.Executor;
import androidx.biometric.BiometricManager;

public class MainActivity extends AppCompatActivity {

    private Button btnAuthenticate;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAuthenticate = findViewById(R.id.inicioSesion);

        // 1. Inicializar el Executor
        executor = ContextCompat.getMainExecutor(this);

        // 2. Configurar el BiometricPrompt Callback
        biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // Este método se llama cuando ocurre un error (ej. usuario cancela, no hay hardware)
                Toast.makeText(getApplicationContext(),
                                "Error de autenticación: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // ¡Autenticación exitosa!
                Toast.makeText(getApplicationContext(),
                                "Autenticación exitosa!", Toast.LENGTH_SHORT)
                        .show();

                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Este método se llama cuando la huella no coincide
                Toast.makeText(getApplicationContext(), "Huella no coincide.",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });

        // 3. Configurar el PromptInfo (el diálogo que le aparece al usuario)
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación Biométrica")
                .setSubtitle("Inicia sesión usando tu huella dactilar")
                .setNegativeButtonText("Usar contraseña") // Ofrece una alternativa
                .build();

        // 4. Configurar el OnClickListener para el botón
        btnAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Comprobar si la biometría está disponible en el dispositivo
                if (checkBiometricSupport()) {
                    biometricPrompt.authenticate(promptInfo);
                } else {
                    Toast.makeText(MainActivity.this,
                            "La biometría no está disponible en este dispositivo o no está configurada.",
                            Toast.LENGTH_LONG).show();
                    // Opcional: Aquí podrías redirigir directamente si no hay biometría
                    // Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    // startActivity(intent);
                }
            }
        });
    }

    // Método auxiliar para verificar si el dispositivo soporta biometría
    private boolean checkBiometricSupport() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // El dispositivo tiene biometría disponible y configurada
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                // No hay hardware de biometría en el dispositivo
                return false;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                // El hardware biométrico no está disponible (ej. por algún problema temporal)
                return false;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // El usuario no ha registrado ninguna huella dactilar
                Toast.makeText(this, "No hay huellas registradas. Por favor, configura una huella en los ajustes del dispositivo.", Toast.LENGTH_LONG).show();
                return false;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                // Se requiere una actualización de seguridad
                return false;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                // No se soporta el tipo de autenticación
                return false;
            default:
                return false;
        }
    }
}