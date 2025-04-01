package com.example.rompe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.rompe.R;

public class MenuFragment extends Fragment {
    private int puzzleSize = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar componentes
        Button btnNormal = view.findViewById(R.id.btnNormal);
        Button btnSubirImagen = view.findViewById(R.id.btnSubirImagen);
        Button btnTomarFoto = view.findViewById(R.id.btnTomarFoto);
        Button btnPuntuaciones = view.findViewById(R.id.btnPuntuaciones);
        Spinner sizeSpinner = view.findViewById(R.id.sizeSpinner);

        // Configurar Spinner (igual que antes)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), // Cambiar "this" por requireContext()
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

        // Listeners actualizados CORREGIDOS
        btnNormal.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("puzzleSize", puzzleSize);
            args.putString("target_fragment", "NormalFragment");
            Navigation.findNavController(v).navigate(R.id.action_menu_to_loading, args);
        });

        btnSubirImagen.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("puzzleSize", puzzleSize);
            args.putString("modo", "galeria");
            args.putString("target_fragment", "FotoFragment"); // AÑADIR ESTA LÍNEA
            Navigation.findNavController(v).navigate(R.id.action_menu_to_loading, args);
        });

        btnTomarFoto.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("puzzleSize", puzzleSize);
            args.putString("modo", "camara");
            args.putString("target_fragment", "FotoFragment"); // AÑADIR ESTA LÍNEA
            Navigation.findNavController(v).navigate(R.id.action_menu_to_loading, args);
        });

        btnPuntuaciones.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_menu_to_scores)
        );
    }
}