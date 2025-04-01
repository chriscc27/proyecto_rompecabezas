package com.example.rompe;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar NavController desde el NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        // Inicializar el NavController
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
    }

    // Manejar el botón "Atrás" para navegar entre fragmentos
    @Override
    public void onBackPressed() {
        if (navController != null) {
            int currentDestination = navController.getCurrentDestination().getId();

            if (currentDestination == R.id.fotoFragment ||
                    currentDestination == R.id.normalFragment ||
                    currentDestination == R.id.scoresFragment) {

                // Ir primero a loadingFragment
                navController.navigate(R.id.loadingFragment);

                // Esperar 2 segundos y luego ir al menuFragment
                new Handler(Looper.getMainLooper()).postDelayed(() ->
                        navController.navigate(R.id.menuFragment), 2000);

            } else if (currentDestination == R.id.menuFragment) {
                // Si está en el menú, cierra la app
                finish();
            } else {
                // Comportamiento predeterminado de Android
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    // Manejar el botón "Up" en la ActionBar
    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }
}
