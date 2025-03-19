package com.example.rompe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    private int puzzleSize = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Inicializara componentes
        Button btnNormal = findViewById(R.id.btnNormal);
        Button btnSubirImagen = findViewById(R.id.btnSubirImagen);
        Button btnTomarFoto = findViewById(R.id.btnTomarFoto);
        Button btnPuntuaciones = findViewById(R.id.btnPuntuaciones);
        Spinner sizeSpinner = findViewById(R.id.sizeSpinner);

        // Configurar Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.puzzle_sizes,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(adapter);

        sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSize = parent.getItemAtPosition(position).toString();
                // Extraer el número de manera más robusta
                puzzleSize = Integer.parseInt(selectedSize.split("x")[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                puzzleSize = 3;
            }
        });

        // Configurar listeners
        btnNormal.setOnClickListener(v -> startGame(MainActivity.class));
        btnSubirImagen.setOnClickListener(v -> startFotoActivity("galeria"));
        btnTomarFoto.setOnClickListener(v -> startFotoActivity("camara"));
        btnPuntuaciones.setOnClickListener(v -> startActivity(new Intent(this, ScoreActivity.class)));
    }

    private void startGame(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("puzzleSize", puzzleSize);
        startActivity(intent);
    }

    private void startFotoActivity(String modo) {
        Intent intent = new Intent(this, FotoActivity.class);
        intent.putExtra("modo", modo);
        intent.putExtra("puzzleSize", puzzleSize);
        startActivity(intent);
    }
}