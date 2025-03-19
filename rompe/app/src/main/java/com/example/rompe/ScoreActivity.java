package com.example.rompe;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.widget.TextView;


import com.example.rompe.Score;
import com.example.rompe.ScoreAdapter;
import com.example.rompe.DatabaseHelper;


public class ScoreActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ScoreAdapter adapter;
    private DatabaseHelper dbHelper;
    private Spinner sizeSpinner;
    private TextView tvNoRecords; // TextView para mostrar el mensaje de "No hay registros"


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        dbHelper = new DatabaseHelper(this);
        sizeSpinner = findViewById(R.id.sizeSpinner);
        recyclerView = findViewById(R.id.rvScores);
        tvNoRecords = findViewById(R.id.tvNoRecords); // Referencia al TextView en el XML

        setupSpinner();
        setupRecyclerView();
        setupBackButton();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.puzzle_sizes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(adapter);

        sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSize = parent.getItemAtPosition(position).toString();
                int puzzleSize = Integer.parseInt(selectedSize.split("x")[0]);
                loadScores(puzzleSize);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScoreAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void loadScores(int puzzleSize) {
        List<Score> scores = dbHelper.getTopScores(puzzleSize + "x" + puzzleSize, 10);

        if (scores.isEmpty()) {
            tvNoRecords.setVisibility(View.VISIBLE); // Muestra el mensaje
            recyclerView.setVisibility(View.GONE);   // Oculta la lista
        } else {
            tvNoRecords.setVisibility(View.GONE);    // Oculta el mensaje
            recyclerView.setVisibility(View.VISIBLE); // Muestra la lista
        }

        adapter.updateData(scores);
    }

    private void setupBackButton() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}