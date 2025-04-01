package com.example.rompe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rompe.DatabaseHelper;
import com.example.rompe.R;
import com.example.rompe.Score;
import com.example.rompe.ScoreAdapter;
import java.util.ArrayList;
import java.util.List;
import androidx.navigation.Navigation;

public class ScoreFragment extends Fragment {

    private RecyclerView recyclerView;
    private ScoreAdapter adapter;
    private DatabaseHelper dbHelper;
    private Spinner sizeSpinner, modalitySpinner;
    private TextView tvNoRecords;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scores, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());
        sizeSpinner = view.findViewById(R.id.sizeSpinner);
        modalitySpinner = view.findViewById(R.id.modalitySpinner);
        recyclerView = view.findViewById(R.id.rvScores);
        tvNoRecords = view.findViewById(R.id.tvNoRecords);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ScoreAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Opcional: Agregar divisor entre items
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        setupSpinners();
        setupBackButton(view);
    }

    private void setupSpinners() {
        // Spinner para tamaños
        ArrayAdapter<CharSequence> sizeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.puzzle_sizes,
                android.R.layout.simple_spinner_item
        );
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(sizeAdapter);

        // Spinner para modalidades
        ArrayAdapter<CharSequence> modalityAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.modalities,
                android.R.layout.simple_spinner_item
        );
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

    private void setupBackButton(View view) {
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );
    }
}