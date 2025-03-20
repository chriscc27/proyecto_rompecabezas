package com.example.rompe;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MenuFragment extends Fragment {

    private int puzzleSize = 3;
    private OnMenuInteractionListener listener;

    public interface OnMenuInteractionListener {
        void onNormalPuzzleSelected(int size);
        void onUploadImageSelected(int size);
        void onTakePhotoSelected(int size);
        void onScoresSelected();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnMenuInteractionListener) {
            listener = (OnMenuInteractionListener) context;
        } else {
            throw new ClassCastException(context + " debe implementar OnMenuInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnNormal = view.findViewById(R.id.btnNormal);
        Button btnSubirImagen = view.findViewById(R.id.btnSubirImagen);
        Button btnTomarFoto = view.findViewById(R.id.btnTomarFoto);
        Button btnPuntuaciones = view.findViewById(R.id.btnPuntuaciones);
        Spinner sizeSpinner = view.findViewById(R.id.sizeSpinner);

        // Configurar Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.puzzle_sizes,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(adapter);

        sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSize = parent.getItemAtPosition(position).toString();
                puzzleSize = Integer.parseInt(selectedSize.split("x")[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                puzzleSize = 3;
            }
        });

        // Configurar listeners
        btnNormal.setOnClickListener(v -> listener.onNormalPuzzleSelected(puzzleSize));
        btnSubirImagen.setOnClickListener(v -> listener.onUploadImageSelected(puzzleSize));
        btnTomarFoto.setOnClickListener(v -> listener.onTakePhotoSelected(puzzleSize));
        btnPuntuaciones.setOnClickListener(v -> listener.onScoresSelected());
    }
}