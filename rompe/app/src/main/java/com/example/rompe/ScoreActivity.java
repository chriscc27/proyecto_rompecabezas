// ScoreActivity.java
package com.example.rompe;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ScoreActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ScoreAdapter adapter;
    private DatabaseHelper dbHelper;
    private Spinner sizeSpinner, modalitySpinner;
    private TextView tvNoRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        dbHelper = new DatabaseHelper(this);
        sizeSpinner = findViewById(R.id.sizeSpinner);
        modalitySpinner = findViewById(R.id.modalitySpinner);
        recyclerView = findViewById(R.id.rvScores);
        tvNoRecords = findViewById(R.id.tvNoRecords);


        recyclerView = findViewById(R.id.rvScores);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScoreAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        setupSpinners();

        setupBackButton();
    }

    private void setupSpinners() {
        // Spinner para tamaños
        ArrayAdapter<CharSequence> sizeAdapter = ArrayAdapter.createFromResource(
                this, R.array.puzzle_sizes, android.R.layout.simple_spinner_item);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(sizeAdapter);

        // Spinner para modalidades
        ArrayAdapter<CharSequence> modalityAdapter = ArrayAdapter.createFromResource(
                this, R.array.modalities, android.R.layout.simple_spinner_item);
        modalityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modalitySpinner.setAdapter(modalityAdapter);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadScores();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        sizeSpinner.setOnItemSelectedListener(listener);
        modalitySpinner.setOnItemSelectedListener(listener);
    }

    private void loadScores() {
        String selectedSize = sizeSpinner.getSelectedItem().toString();
        String modality = modalitySpinner.getSelectedItem().toString().toLowerCase();
        int puzzleSize = Integer.parseInt(selectedSize.split("x")[0]);

        List<Score> scores = dbHelper.getTopScores(
                puzzleSize + "x" + puzzleSize,
                modality,
                10
        );

        updateUI(scores);
    }

    private void updateUI(List<Score> scores) {
        if (scores.isEmpty()) {
            tvNoRecords.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoRecords.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter.updateData(scores);
    }


    private void setupBackButton() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}