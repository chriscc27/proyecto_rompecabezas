package com.example.rompe;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.rompe.R;
import java.util.Random;

public class LoadingFragment extends Fragment {

    private final String[] datosCuriosos = {
            "El primer crucigrama moderno se publicó en 1913 en el New York World.",
            "El Sudoku tiene sus raíces en un juego matemático del siglo XVIII llamado 'Cuadrados Latinos'.",
            "El cubo de Rubik fue inventado en 1974 por el húngaro Ernő Rubik.",
            "Los puzzles de escape room se inspiraron en videojuegos de aventuras gráficas de los 90.",
            "El Tangram, un puzzle chino de 7 piezas, existe desde el siglo III a.C.",
            "El récord del laberinto de maíz más grande del mundo cubre 65 hectáreas (Reino Unido).",
            "Los puzles 3D de monumentos son los favoritos entre arquitectos y viajeros.",
            "El juego 'Las Torres de Hanói' se usa en psicología para estudiar la resolución de problemas.",
            "El acertijo del lobo, la cabra y la col es un clásico de la lógica medieval.",
            "Los puzzles de espejos se usan en óptica para enseñar principios de reflexión de la luz.",
            "El ajedrez es considerado el 'rey de los puzzles estratégicos'.",
            "El término 'enigma' proviene del griego 'ainigma', que significa 'declaración oscura'.",
            "El puzzle más vendido de la historia es el cubo de Rubik (350 millones de unidades).",
            "Los puzzles de lógica tipo 'nonogram' son populares en revistas japonesas desde los 80.",
            "El juego 'Myst' (1993) revolucionó los puzzles en videojuegos con su mundo interactivo.",
            "Los puzzles de hielo se usan en entrenamientos para expediciones polares.",
            "La frase 'pensamiento lateral' fue acuñada por Edward de Bono para resolver acertijos complejos.",
            "Los rompecabezas mecánicos de metal son populares desde la era victoriana.",
            "El puzzle de Einstein: un famoso acertijo lógico que solo el 2% de la población resuelve.",
            "Los juegos de escape digitales suelen incluir puzzles basados en códigos y patrones.",
            "El récord de resolución de un cubo de Rubik con los pies es 16.96 segundos.",
            "Los puzzles de sonido se usan en terapias cognitivas para pacientes con Alzheimer.",
            "El juego 'Portal' (2007) popularizó los puzzles espaciales con portales interdimensionales.",
            "Los crucigramas diarios mejoran la memoria a largo plazo según estudios de neurociencia.",
            "El puzzle más antiguo conocido es un laberinto tallado en una tablilla cretense del 1200 a.C.",
            "Los puzzles táctiles son cruciales en el desarrollo infantil de la motricidad fina.",
            "El cubo de Rubik tiene 43 trillones de combinaciones posibles pero solo 1 solución correcta.",
            "Los acertijos de 'lógica difusa' permiten respuestas parcialmente verdaderas.",
            "Los puzzles de realidad aumentada combinan elementos físicos y digitales.",
            "Resolver puzzles libera dopamina, la hormona asociada con la satisfacción y el aprendizaje."
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Mostrar dato curioso
        TextView txtDato = view.findViewById(R.id.txtDatoCurioso);
        int indiceAleatorio = new Random().nextInt(datosCuriosos.length);
        txtDato.setText(datosCuriosos[indiceAleatorio]);

        // Obtener y preparar argumentos
        final Bundle args = getArguments();
        final String targetFragment = (args != null) ? args.getString("target_fragment", "") : "";

        // Redirección después de 1 segundo
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            NavController navController = Navigation.findNavController(view);

            // Usar when en lugar de switch para mejor legibilidad
            if (!targetFragment.isEmpty()) {
                switch (targetFragment) {
                    case "NormalFragment":
                        navController.navigate(R.id.action_loading_to_normal, args);
                        break;
                    case "FotoFragment":
                        navController.navigate(R.id.action_loading_to_foto, args);
                        break;
                    case "ScoreFragment":
                        //navController.navigate(R.id.action_loading_to_scores, args);
                        break;
                    default:
                        navController.navigate(R.id.action_loading_to_menu, args);
                        break;
                }
            } else {
                navController.navigate(R.id.action_loading_to_menu, args);
            }
        }, 1000);
    }
}