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

public class NormalActivity extends AppCompatActivity {

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
    private String[] currentState;
    private int moves = 0;
    private boolean scoreSaved = false;
    private TextView txtNumeroFaltante;
    private int missingNumber;
    private int[] targetPositions;

    private boolean isSolving = false;
    private Handler solutionHandler = new Handler();
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);

        txtNumeroFaltante = findViewById(R.id.txtNumeroFaltante);
        Intent intent = getIntent();
        puzzleSize = intent.getIntExtra("puzzleSize", 3);
        puzzleSize = Math.max(2, Math.min(puzzleSize, 7));

        gridLayout = findViewById(R.id.gridLayout);
        timerTextView = findViewById(R.id.timerTextView);
        btnResolver = findViewById(R.id.btnResolver);

        btnResolver.setOnClickListener(v -> {
            if (!isTimerRunning) {
                Toast.makeText(this, "Primero inicia el juego", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isSolving) {
                Toast.makeText(this, "Ya se está resolviendo", Toast.LENGTH_SHORT).show();
                return;
            }
            btnResolver.setEnabled(false);
            isSolving = true;
            resolverRompecabezas();
        });

        inicializarGrid();
    }

    private void inicializarGrid() {
        gridLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                crearGrid();
                generarSolucion();
                mezclarFichas();
                actualizarEstadoActual();
                configurarListeners();
                startTimer();
            }
        });
    }

    private void crearGrid() {
        grid = new TextView[puzzleSize][puzzleSize];
        gridLayout.removeAllViews();
        gridLayout.setColumnCount(puzzleSize);
        gridLayout.setRowCount(puzzleSize);

        int width = gridLayout.getWidth() - gridLayout.getPaddingLeft() - gridLayout.getPaddingRight();
        int height = gridLayout.getHeight() - gridLayout.getPaddingTop() - gridLayout.getPaddingBottom();
        int tileSize = Math.min(width, height) / puzzleSize;

        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                TextView textView = new TextView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = params.height = tileSize - 8;
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
    }

    private void generarSolucion() {
        solution.clear();
        List<String> numeros = new ArrayList<>();
        for (int i = 1; i <= puzzleSize * puzzleSize; i++) numeros.add(String.valueOf(i));

        int emptyIndex = random.nextInt(numeros.size());
        missingNumber = Integer.parseInt(numeros.get(emptyIndex));
        numeros.set(emptyIndex, "");
        solution.addAll(numeros);

        targetPositions = new int[puzzleSize * puzzleSize + 1];
        Arrays.fill(targetPositions, -1);
        for (int i = 0; i < solution.size(); i++) {
            String num = solution.get(i);
            if (!num.isEmpty()) targetPositions[Integer.parseInt(num)] = i;
        }

        runOnUiThread(() -> txtNumeroFaltante.setText("Falta el número: " + missingNumber));
    }

    private void mezclarFichas() {
        generarSolucion();
        moves = seconds = 0;
        scoreSaved = false;
        txtNumeroFaltante.setVisibility(View.VISIBLE);

        String[] numeros = solution.toArray(new String[0]);
        int emptyIndex = Arrays.asList(numeros).indexOf("");
        int emptyRow = emptyIndex / puzzleSize;
        int emptyCol = emptyIndex % puzzleSize;

        for (int i = 0; i < 150; i++) {
            List<int[]> movimientos = new ArrayList<>(4);
            if (emptyRow > 0) movimientos.add(new int[]{emptyRow - 1, emptyCol});
            if (emptyRow < puzzleSize - 1) movimientos.add(new int[]{emptyRow + 1, emptyCol});
            if (emptyCol > 0) movimientos.add(new int[]{emptyRow, emptyCol - 1});
            if (emptyCol < puzzleSize - 1) movimientos.add(new int[]{emptyRow, emptyCol + 1});

            if (movimientos.isEmpty()) break;

            int[] movimiento = movimientos.get(random.nextInt(movimientos.size()));
            int newIndex = movimiento[0] * puzzleSize + movimiento[1];

            String temp = numeros[emptyIndex];
            numeros[emptyIndex] = numeros[newIndex];
            numeros[newIndex] = temp;
            emptyIndex = newIndex;
            emptyRow = movimiento[0];
            emptyCol = movimiento[1];
        }

        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                String num = numeros[i * puzzleSize + j];
                grid[i][j].setText(num);
                asignarColor(num, grid[i][j]);
                if (num.isEmpty()) {
                    this.emptyRow = i;
                    this.emptyCol = j;
                }
            }
        }
        actualizarEstadoActual();
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
            float hue = (num * 30f) % 360f;
            int color = Color.HSVToColor(new float[]{hue, 0.8f, 0.8f});
            shape.setColor(color);
        }

        tv.setBackground(shape);
        tv.setTextColor(Color.WHITE);
        tv.setShadowLayer(6f, 0f, 2f, Color.BLACK);
    }

    private void resolverRompecabezas() {
        if (Arrays.equals(currentState, solution.toArray())) {
            Toast.makeText(this, "El puzzle ya está resuelto", Toast.LENGTH_SHORT).show();
            btnResolver.setEnabled(true);
            return;
        }

        new Thread(() -> {
            PriorityQueue<Nodo> openSet = new PriorityQueue<>(10000, Comparator.comparingInt(n -> n.costoTotal));
            Set<String> visited = new HashSet<>(100000, 0.9f);

            String[] estadoInicial = Arrays.copyOf(currentState, currentState.length);
            Nodo inicial = new Nodo(estadoInicial, emptyRow, emptyCol, 0, calcularHeuristica(estadoInicial), null);

            openSet.add(inicial);
            visited.add(estadoToString(estadoInicial));

            while (!openSet.isEmpty() && !isFinishing()) {
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

            runOnUiThread(() -> {
                Toast.makeText(this, "No se encontró solución", Toast.LENGTH_SHORT).show();
                btnResolver.setEnabled(true);
                isSolving = false;
            });
        }).start();
    }

    private int calcularHeuristica(String[] estado) {
        int h = 0;

        for (int i = 0; i < estado.length; i++) {
            String numStr = estado[i];
            if (numStr.isEmpty()) continue;

            int num = Integer.parseInt(numStr);
            int posObj = targetPositions[num];

            int filaActual = i / puzzleSize;
            int colActual = i % puzzleSize;
            int filaObj = posObj / puzzleSize;
            int colObj = posObj % puzzleSize;

            h += Math.abs(filaActual - filaObj) + Math.abs(colActual - colObj);

            if (filaActual == filaObj) {
                for (int j = i + 1; j < estado.length; j++) {
                    if (estado[j].isEmpty()) continue;
                    int otroNum = Integer.parseInt(estado[j]);
                    if (targetPositions[otroNum] / puzzleSize == filaActual) {
                        if ((colActual > j % puzzleSize) != (posObj > targetPositions[otroNum])) {
                            h += 2;
                        }
                    }
                }
            }
        }
        return h;
    }

    private List<Nodo> generarVecinos(Nodo nodo) {
        List<Nodo> vecinos = new ArrayList<>(4);
        int emptyPos = nodo.emptyRow * puzzleSize + nodo.emptyCol;
        String[] estado = nodo.estado;

        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        for (int[] dir : dirs) {
            int newRow = nodo.emptyRow + dir[0];
            int newCol = nodo.emptyCol + dir[1];

            if (newRow >= 0 && newRow < puzzleSize && newCol >= 0 && newCol < puzzleSize) {
                String[] nuevoEstado = Arrays.copyOf(estado, estado.length);
                int newPos = newRow * puzzleSize + newCol;

                nuevoEstado[emptyPos] = nuevoEstado[newPos];
                nuevoEstado[newPos] = "";

                int heuristica = calcularHeuristica(nuevoEstado);
                vecinos.add(new Nodo(nuevoEstado, newRow, newCol, nodo.costo + 1, heuristica, nodo));
            }
        }
        return vecinos;
    }

    private void mostrarSolucion(List<Nodo> camino) {
        solutionHandler.removeCallbacksAndMessages(null);
        new Handler(Looper.getMainLooper()).post(() -> {
            stopTimer();
            btnResolver.setEnabled(true);
            isSolving = false;

            for (int i = 0; i < camino.size(); i++) {
                final int index = i;
                solutionHandler.postDelayed(() -> {
                    if (index < camino.size()) {
                        Nodo nodo = camino.get(index);
                        actualizarGrid(nodo.estado, nodo.emptyRow, nodo.emptyCol);
                    }
                }, i * 500L);
            }

            solutionHandler.postDelayed(() ->
                            mostrarDialogoReintentar(camino.size()),
                    camino.size() * 500L + 1000
            );
        });
    }

    private void actualizarGrid(String[] estado, int emptyRow, int emptyCol) {
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                String num = estado[i * puzzleSize + j];
                grid[i][j].setText(num);
                asignarColor(num, grid[i][j]);
            }
        }
        this.emptyRow = emptyRow;
        this.emptyCol = emptyCol;
        actualizarEstadoActual();
    }

    private List<Nodo> reconstruirCamino(Nodo nodoFinal) {
        List<Nodo> camino = new ArrayList<>();
        while (nodoFinal != null) {
            camino.add(nodoFinal);
            nodoFinal = nodoFinal.padre;
        }
        Collections.reverse(camino);
        return camino;
    }

    private String estadoToString(String[] estado) {
        return String.join(",", estado);
    }

    private void actualizarEstadoActual() {
        currentState = new String[puzzleSize * puzzleSize];
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                currentState[i * puzzleSize + j] = grid[i][j].getText().toString();
            }
        }
    }

    private void configurarListeners() {
        for (int i = 0; i < puzzleSize; i++) {
            for (int j = 0; j < puzzleSize; j++) {
                final int row = i, col = j;
                grid[i][j].setOnClickListener(v -> moverFicha(row, col));
            }
        }
    }

    private void moverFicha(int fila, int columna) {
        if (Math.abs(fila - emptyRow) + Math.abs(columna - emptyCol) == 1) {
            moves++;
            intercambiar(fila, columna, emptyRow, emptyCol);
            emptyRow = fila;
            emptyCol = columna;
            actualizarEstadoActual();

            if (esResuelto()) {
                stopTimer();
                Toast.makeText(this, "¡Resuelto en " + seconds + " segundos!", Toast.LENGTH_LONG).show();
                if (!scoreSaved) showNameDialog();
            }
        }
    }

    private void intercambiar(int fila1, int col1, int fila2, int col2) {
        TextView tv1 = grid[fila1][col1];
        TextView tv2 = grid[fila2][col2];

        String temp = tv1.getText().toString();
        tv1.setText(tv2.getText());
        tv2.setText(temp);

        asignarColor(tv1.getText().toString(), tv1);
        asignarColor(tv2.getText().toString(), tv2);
    }

    private boolean esResuelto() {
        if (Arrays.equals(currentState, solution.toArray())) {
            txtNumeroFaltante.setVisibility(View.INVISIBLE);
            stopTimer();
            if (!scoreSaved) {
                showNameDialog();
                scoreSaved = true;
            }
            return true;
        }
        return false;
    }

    private void mostrarDialogoReintentar(int movimientos) {
        new AlertDialog.Builder(this)
                .setTitle(movimientos > 0 ? "¡Solución encontrada!" : "No se encontró solución")
                .setMessage(movimientos > 0 ?
                        "Se resolvió en " + movimientos + " movimientos. ¿Reintentar?" :
                        "¿Deseas intentarlo de nuevo?")
                .setPositiveButton("Reintentar", (d, w) -> {
                    mezclarFichas();
                    startTimer();
                    txtNumeroFaltante.setVisibility(View.VISIBLE);
                    scoreSaved = false;
                })
                .setNegativeButton("No", (d, w) -> d.dismiss())
                .setCancelable(false)
                .show();
    }

    private void showNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Guardar puntuación");

        View view = getLayoutInflater().inflate(R.layout.dialog_name, null);
        EditText etName = view.findViewById(R.id.etName);

        builder.setView(view);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) name = "Anónimo";
            if (name.length() > 20) { // Limitar longitud máxima
                Toast.makeText(this, "Máximo 20 caracteres", Toast.LENGTH_SHORT).show();
                return;
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
                puzzleSize + "x" + puzzleSize,
                null,
                "normal"
        );
        dbHelper.saveScore(score);
        Toast.makeText(this, "Puntuación guardada", Toast.LENGTH_SHORT).show();

        navegarAPuntuaciones();
    }

    private void navegarAPuntuaciones() {
        Intent loadingIntent = new Intent(this, LoadingActivity.class);
        loadingIntent.putExtra("target_activity", ScoreActivity.class.getName());
        startActivity(loadingIntent);
        finish();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        solutionHandler.removeCallbacksAndMessages(null);
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
        final String[] estado;
        final int emptyRow, emptyCol;
        final int costo, heuristica, costoTotal;
        final Nodo padre;

        Nodo(String[] estado, int emptyRow, int emptyCol, int costo, int heuristica, Nodo padre) {
            this.estado = Arrays.copyOf(estado, estado.length);
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