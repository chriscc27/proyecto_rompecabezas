package com.example.rompe;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import android.widget.EditText;


import androidx.appcompat.app.AlertDialog;



import android.view.ViewTreeObserver;
import android.view.View;
import android.graphics.drawable.GradientDrawable;



import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView[][] grid;
    private int emptyRow, emptyCol;
    private int puzzleSize;
    private TextView timerTextView;
    private Handler handler = new Handler();
    private int seconds = 0;
    private boolean isTimerRunning = false;
    private Button btnResolver;
    private List<String> solution = new ArrayList<>();
    private List<String> currentState = new ArrayList<>();

    private int moves = 0;
    private boolean scoreSaved = false;


    private TextView txtNumeroFaltante;
    private int missingNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar nuevo componente
        txtNumeroFaltante = findViewById(R.id.txtNumeroFaltante);

        // Validar y obtener el tamaño del puzzle
        Intent intent = getIntent();
        puzzleSize = 3; // Valor por defecto
        if (intent != null && intent.hasExtra("puzzleSize")) {
            puzzleSize = intent.getIntExtra("puzzleSize", 3);
        }
        puzzleSize = Math.max(2, Math.min(puzzleSize, 7)); // Permite 2x2 hasta 6x6

        // Inicializar componentes
        gridLayout = findViewById(R.id.gridLayout);
        timerTextView = findViewById(R.id.timerTextView);
        btnResolver = findViewById(R.id.btnResolver);

        // Configurar el botón resolver
        btnResolver.setOnClickListener(v -> {
            if (!isTimerRunning) {
                Toast.makeText(this, "Primero inicia el juego", Toast.LENGTH_SHORT).show();
                return;
            }
            btnResolver.setEnabled(false);
            resolverRompecabezas();
        });

        // Inicialización diferida del grid
        inicializarGrid();
    }




    private void inicializarGrid() {
        gridLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Crear el grid dinámicamente
                grid = new TextView[puzzleSize][puzzleSize];
                gridLayout.removeAllViews();
                gridLayout.setColumnCount(puzzleSize);
                gridLayout.setRowCount(puzzleSize);

                int width = gridLayout.getWidth() - gridLayout.getPaddingLeft() - gridLayout.getPaddingRight();
                int height = gridLayout.getHeight() - gridLayout.getPaddingTop() - gridLayout.getPaddingBottom();
                int tileSize = Math.min(width, height) / puzzleSize;

                for (int i = 0; i < puzzleSize; i++) {
                    for (int j = 0; j < puzzleSize; j++) {
                        TextView textView = new TextView(MainActivity.this);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = tileSize - 8;
                        params.height = tileSize - 8;
                        params.setMargins(4, 4, 4, 4);
                        params.rowSpec = GridLayout.spec(i);
                        params.columnSpec = GridLayout.spec(j);

                        textView.setLayoutParams(params);
                        textView.setGravity(Gravity.CENTER);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, tileSize / 4);
                        textView.setTextColor(Color.WHITE);
                        textView.setShadowLayer(4f, 0f, 2f, Color.BLACK);

                        grid[i][j] = textView;
                        gridLayout.addView(textView);
                    }
                }

                // Inicializar el resto después de crear el grid
                generarSolucion();
                mezclarFichas();
                actualizarEstadoActual();
                configurarListeners();
                startTimer();
            }
        });
    }

    private void generarSolucion() {
        solution.clear();
        List<String> numeros = new ArrayList<>();

        // Generar números del 1 al N*N
        for (int i = 1; i <= puzzleSize * puzzleSize; i++) {
            numeros.add(String.valueOf(i));
        }

        // Calcular posición vacía y número faltante
        int emptyIndex = (puzzleSize * puzzleSize) / 2;
        missingNumber = emptyIndex + 1; // Número que debería estar en la posición vacía
        numeros.set(emptyIndex, "");

        solution.addAll(numeros);

        // Actualizar UI
        runOnUiThread(() -> txtNumeroFaltante.setText("Falta el: " + missingNumber));
    }

    private void mezclarFichas() {
        moves = 0;
        scoreSaved = false;
        seconds = 0;

        txtNumeroFaltante.setVisibility(View.VISIBLE);

        // Mezclar usando movimientos válidos (simula jugadas reales)
        List<String> numeros = new ArrayList<>(solution);
        Random random = new Random();

        int emptyIndex = numeros.indexOf("");
        int emptyRow = emptyIndex / puzzleSize;
        int emptyCol = emptyIndex % puzzleSize;

        // Realizar 1000 movimientos aleatorios válidos
        for (int i = 0; i < 1000; i++) {
            List<int[]> movimientosValidos = new ArrayList<>();
            if (emptyRow > 0) movimientosValidos.add(new int[]{emptyRow - 1, emptyCol}); // Arriba
            if (emptyRow < puzzleSize - 1) movimientosValidos.add(new int[]{emptyRow + 1, emptyCol}); // Abajo
            if (emptyCol > 0) movimientosValidos.add(new int[]{emptyRow, emptyCol - 1}); // Izquierda
            if (emptyCol < puzzleSize - 1) movimientosValidos.add(new int[]{emptyRow, emptyCol + 1}); // Derecha

            int[] movimiento = movimientosValidos.get(random.nextInt(movimientosValidos.size()));
            int newRow = movimiento[0];
            int newCol = movimiento[1];
            int newIndex = newRow * puzzleSize + newCol;

            // Intercambiar posiciones
            Collections.swap(numeros, emptyIndex, newIndex);

            // Actualizar posición vacía
            emptyIndex = newIndex;
            emptyRow = newRow;
            emptyCol = newCol;
        }

        // Aplicar al grid
        int index = 0;
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                String numero = numeros.get(index++);
                grid[i][j].setText(numero);
                asignarColor(numero, grid[i][j]);
                if (numero.isEmpty()) {
                    this.emptyRow = i;
                    this.emptyCol = j;
                }
            }
        }
    }

    private void asignarColor(String numero, TextView tv) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(16f);

        if (numero.isEmpty()) {
            shape.setColor(Color.TRANSPARENT);
            shape.setStroke(2, Color.WHITE);
        } else {
            int num = Integer.parseInt(numero);
            float hue = (num * 30f) % 360f; // Colores distintos por número
            float saturation = 0.7f + (num % 3) * 0.1f; // Variar saturación
            float brightness = 0.8f - (num % 2) * 0.1f; // Variar brillo

            int color = Color.HSVToColor(new float[]{hue, saturation, brightness});
            shape.setColor(color);
        }

        tv.setBackground(shape);
        tv.setTextColor(Color.WHITE);
        tv.setShadowLayer(6f, 0f, 2f, Color.BLACK);
    }

    private void resolverRompecabezas() {

        if (currentState.equals(solution)) {
            Toast.makeText(this, "El puzzle ya está resuelto", Toast.LENGTH_SHORT).show();
            btnResolver.setEnabled(true);
            return;
        }

        new Thread(() -> {
            PriorityQueue<Nodo> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.costoTotal));
            Set<String> visited = new HashSet<>();

            List<String> estadoInicial = new ArrayList<>(currentState);
            Nodo inicial = new Nodo(estadoInicial, emptyRow, emptyCol, 0, calcularHeuristica(estadoInicial), null);
            openSet.add(inicial);
            visited.add(estadoToString(estadoInicial));

            while (!openSet.isEmpty()) {
                Nodo actual = openSet.poll();

                if (actual.heuristica == 0) {
                    mostrarSolucion(reconstruirCamino(actual));
                    return;
                }

                for (Nodo vecino : generarVecinos(actual)) {
                    String estadoStr = estadoToString(vecino.estado);
                    if (!visited.contains(estadoStr)) {
                        visited.add(estadoStr);
                        openSet.add(vecino);
                    }
                }
            }

            runOnUiThread(() -> Toast.makeText(this, "No se encontró solución", Toast.LENGTH_SHORT).show());
        }).start();
    }

    private int calcularHeuristica(List<String> estado) {
        int h = 0;
        for (int i = 0; i < estado.size(); i++) {
            String letra = estado.get(i);
            if (letra.isEmpty()) continue;

            // Posición correcta según la solución dinámica
            int posicionCorrecta = solution.indexOf(letra);
            int filaActual = i / puzzleSize;
            int colActual = i % puzzleSize;
            int filaObjetivo = posicionCorrecta / puzzleSize;
            int colObjetivo = posicionCorrecta % puzzleSize;

            h += Math.abs(filaActual - filaObjetivo) + Math.abs(colActual - colObjetivo);
        }
        return h;
    }

    private List<Nodo> generarVecinos(Nodo nodo) {
        List<Nodo> vecinos = new ArrayList<>();
        int[][] direcciones = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : direcciones) {
            int newRow = nodo.emptyRow + dir[0];
            int newCol = nodo.emptyCol + dir[1];

            if (newRow >= 0 && newRow < puzzleSize && newCol >= 0 && newCol < puzzleSize) {
                List<String> nuevoEstado = new ArrayList<>(nodo.estado);
                int oldPos = nodo.emptyRow * puzzleSize + nodo.emptyCol;
                int newPos = newRow * puzzleSize + newCol;

                Collections.swap(nuevoEstado, oldPos, newPos);
                int heuristica = calcularHeuristica(nuevoEstado);
                vecinos.add(new Nodo(nuevoEstado, newRow, newCol, nodo.costo + 1, heuristica, nodo));
            }
        }
        return vecinos;
    }

    private void mostrarSolucion(List<Nodo> camino) {
        new Handler(Looper.getMainLooper()).post(() -> {
            stopTimer();
            btnResolver.setEnabled(true); // Reactivar el botón
            for (int i = 0; i < camino.size(); i++) {
                final int index = i;
                handler.postDelayed(() -> {
                    Nodo nodo = camino.get(index);
                    actualizarGrid(nodo.estado, nodo.emptyRow, nodo.emptyCol);
                }, i * 500L);
            }
        });
    }

    private void actualizarGrid(List<String> estado, int emptyRow, int emptyCol) {
        int index = 0;
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                String letra = estado.get(index++);
                grid[i][j].setText(letra);
                asignarColor(letra, grid[i][j]);
            }
        }
        this.emptyRow = emptyRow;
        this.emptyCol = emptyCol;
    }

    private List<Nodo> reconstruirCamino(Nodo nodoFinal) {
        List<Nodo> camino = new ArrayList<>();
        Nodo actual = nodoFinal;
        while (actual != null) {
            camino.add(actual);
            actual = actual.padre;
        }
        Collections.reverse(camino);
        return camino;
    }

    private String estadoToString(List<String> estado) {
        return String.join(",", estado);
    }

    private void actualizarEstadoActual() {
        currentState.clear();
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                currentState.add(grid[i][j].getText().toString());
            }
        }
    }


    private void configurarListeners() {
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                final int row = i;
                final int col = j;
                grid[i][j].setOnClickListener(v -> moverFicha(row, col));
            }
        }
    }

    private void moverFicha(int fila, int columna) {
        if (esAdyacente(fila, columna, emptyRow, emptyCol)) {
            moves++;
            intercambiar(fila, columna, emptyRow, emptyCol);
            emptyRow = fila;
            emptyCol = columna;
            actualizarEstadoActual();

            if (esResuelto()) {
                stopTimer();
                Toast.makeText(this, "¡Resuelto en " + seconds + " segundos!", Toast.LENGTH_LONG).show();
                if (!scoreSaved) {
                    showNameDialog();
                    scoreSaved = true;
                }
            }
        }
    }



    private void showNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Guardar puntuación");

        // Inflar el layout personalizado
        View view = getLayoutInflater().inflate(R.layout.dialog_name, null);
        EditText etName = view.findViewById(R.id.etName);

        builder.setView(view);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                name = "Anónimo";
            }
            saveScoreToDatabase(name);
        });
        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveScoreToDatabase(String name) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Score score = new Score(
                0, // El ID se autoincrementa
                name,
                seconds,
                moves,
                puzzleSize + "x" + puzzleSize, // Tipo de puzzle
                null // La fecha se genera automáticamente



        );
        dbHelper.saveScore(score);
        Toast.makeText(this, "Puntuación guardada", Toast.LENGTH_SHORT).show();
    }

    private boolean esAdyacente(int fila1, int col1, int fila2, int col2) {
        return Math.abs(fila1 - fila2) + Math.abs(col1 - col2) == 1;
    }


    private void intercambiar(int fila1, int col1, int fila2, int col2) {
        TextView ficha1 = grid[fila1][col1];
        TextView ficha2 = grid[fila2][col2];

        String texto1 = ficha1.getText().toString();
        String texto2 = ficha2.getText().toString();

        ficha1.setText(texto2);
        ficha2.setText(texto1);

        asignarColor(texto2, ficha1);
        asignarColor(texto1, ficha2);
    }

    private boolean esResuelto() {
        // Verificar si el número faltante está en su posición
        int emptyIndex = emptyRow * puzzleSize + emptyCol;
        boolean resuelto = currentState.equals(solution);

        if (resuelto) {
            txtNumeroFaltante.setVisibility(View.INVISIBLE);
        }
        return resuelto;
    }

    private void startTimer() {
        isTimerRunning = true;
        seconds = 0;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isTimerRunning) {
                    seconds++;
                    timerTextView.setText(String.format("Tiempo: %02d:%02d", seconds / 60, seconds % 60));
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void stopTimer() {
        isTimerRunning = false;
    }

    private static class Nodo implements Comparable<Nodo> {
        List<String> estado;
        int emptyRow;
        int emptyCol;
        int costo;
        int heuristica;
        int costoTotal;
        Nodo padre;

        Nodo(List<String> estado, int emptyRow, int emptyCol, int costo, int heuristica, Nodo padre) {
            this.estado = new ArrayList<>(estado);
            this.emptyRow = emptyRow;
            this.emptyCol = emptyCol;
            this.costo = costo;
            this.heuristica = heuristica;
            this.costoTotal = costo + heuristica;
            this.padre = padre;
        }

        @Override
        public int compareTo(Nodo otro) {
            return Integer.compare(this.costoTotal, otro.costoTotal);
        }
    }
}