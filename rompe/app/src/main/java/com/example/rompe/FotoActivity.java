package com.example.rompe;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;


import android.Manifest;
import java.util.Comparator;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;




import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import android.graphics.Canvas;
import android.graphics.Paint;


public class FotoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    private GridLayout gridLayout;
    private ImageView fullImageView;
    private TextView timerTextView;
    private List<PuzzlePiece> puzzlePieces;
    private List<PuzzlePiece> solvedPuzzle;
    private int emptyPosition;
    private int seconds = 0;
    private boolean isTimerRunning = false;
    private Handler handler = new Handler();
    private Uri photoUri;
    private int puzzleSize;
    private Button btnResolver;
    private Bitmap originalBitmap;
    private int emptyPositionSolved;

    private static class PuzzlePiece {
        Bitmap bitmap;
        int originalPosition;

        PuzzlePiece(Bitmap bitmap, int originalPosition) {
            this.bitmap = bitmap;
            this.originalPosition = originalPosition;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto);

        gridLayout = findViewById(R.id.gridLayout);
        fullImageView = findViewById(R.id.fullImageView);
        timerTextView = findViewById(R.id.timerTextView);
        btnResolver = findViewById(R.id.btnResolver);

        puzzleSize = getIntent().getIntExtra("puzzleSize", 3);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }

        String modo = getIntent().getStringExtra("modo");
        if (modo != null) {
            if (modo.equals("galeria")) {
                abrirGaleria();
            } else if (modo.equals("camara")) {
                abrirCamara();
            }
        }

        btnResolver.setOnClickListener(v -> resolverRompecabezas());
    }

    private void createPuzzle(Bitmap bitmap) {
        int totalPieces = puzzleSize * puzzleSize;
        int pieceSize = bitmap.getWidth() / puzzleSize;
        puzzlePieces = new ArrayList<>();
        solvedPuzzle = new ArrayList<>();

        // Generar posición vacía aleatoria para el estado resuelto
        emptyPositionSolved = new Random().nextInt(totalPieces);

        // Crear estado resuelto con vacío en posición aleatoria
        for (int i = 0; i < totalPieces; i++) {
            if (i == emptyPositionSolved) {
                solvedPuzzle.add(new PuzzlePiece(null, i));
            } else {
                int row = i / puzzleSize;
                int col = i % puzzleSize;
                Bitmap piece = Bitmap.createBitmap(bitmap, col * pieceSize, row * pieceSize, pieceSize, pieceSize);
                solvedPuzzle.add(new PuzzlePiece(piece, i));
            }
        }

        // Crear imagen de referencia con espacio vacío
        Bitmap solvedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(solvedBitmap);
        Paint emptyPaint = new Paint();
        emptyPaint.setColor(Color.TRANSPARENT);

        for (int i = 0; i < solvedPuzzle.size(); i++) {
            PuzzlePiece piece = solvedPuzzle.get(i);
            int row = i / puzzleSize;
            int col = i % puzzleSize;

            if (piece.bitmap == null) {
                canvas.drawRect(col * pieceSize, row * pieceSize,
                        (col + 1) * pieceSize, (row + 1) * pieceSize, emptyPaint);
            } else {
                canvas.drawBitmap(piece.bitmap, col * pieceSize, row * pieceSize, null);
            }
        }
        runOnUiThread(() -> fullImageView.setImageBitmap(solvedBitmap));

        // Mezclar piezas desde el estado resuelto
        puzzlePieces = new ArrayList<>(solvedPuzzle);
        emptyPosition = emptyPositionSolved;
        Random random = new Random();
        int shuffleMoves = 1000;

        for (int i = 0; i < shuffleMoves; i++) {
            List<Integer> possibleMoves = new ArrayList<>();
            int row = emptyPosition / puzzleSize;
            int col = emptyPosition % puzzleSize;

            if (row > 0) possibleMoves.add(emptyPosition - puzzleSize);
            if (row < puzzleSize - 1) possibleMoves.add(emptyPosition + puzzleSize);
            if (col > 0) possibleMoves.add(emptyPosition - 1);
            if (col < puzzleSize - 1) possibleMoves.add(emptyPosition + 1);

            if (!possibleMoves.isEmpty()) {
                int newPos = possibleMoves.get(random.nextInt(possibleMoves.size()));
                Collections.swap(puzzlePieces, emptyPosition, newPos);
                emptyPosition = newPos;
            }
        }

        displayPuzzle(puzzlePieces);
        startTimer();
    }


    private void regeneratePuzzle() {
        if (originalBitmap != null) {
            createPuzzle(originalBitmap); // Usar la imagen original guardada
        }
    }




    private void resolverRompecabezas() {
        new Thread(() -> {
            PriorityQueue<Nodo> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.costoTotal));
            Set<String> visited = new HashSet<>();

            int[] estadoInicial = new int[puzzlePieces.size()];
            for (int i = 0; i < puzzlePieces.size(); i++) {
                PuzzlePiece piece = puzzlePieces.get(i);
                estadoInicial[i] = (piece.bitmap == null) ? -1 : piece.originalPosition;
            }

            Nodo inicial = new Nodo(estadoInicial, emptyPosition, 0, getCachedHeuristic(estadoInicial), null);
            openSet.add(inicial);
            visited.add(Arrays.toString(estadoInicial));

            while (!openSet.isEmpty()) {
                Nodo actual = openSet.poll();

                if (actual.heuristica == 0) {
                    mostrarSolucion(reconstruirCamino(actual));
                    return;
                }

                for (Nodo vecino : generarVecinos(actual)) {
                    String vecinoKey = Arrays.toString(vecino.estado);
                    if (!visited.contains(vecinoKey)) {
                        visited.add(vecinoKey);
                        openSet.add(vecino);
                    }
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Solución no encontrada", Toast.LENGTH_SHORT).show();
                regeneratePuzzle();
            });
        }).start();
    }

    private int calcularHeuristicaOptimizada(int[] estado) {
        int h = 0;
        int size = puzzleSize;

        for (int i = 0; i < estado.length; i++) {
            int val = estado[i];
            if (val == -1) continue; // Saltar vacío

            int targetRow = val / size;
            int targetCol = val % size;
            int currentRow = i / size;
            int currentCol = i % size;

            h += Math.abs(targetRow - currentRow) + Math.abs(targetCol - currentCol);

            // Conflictos lineales en fila
            if (currentRow == targetRow) {
                for (int j = i + 1; j < estado.length; j++) {
                    int otherVal = estado[j];
                    if (otherVal == -1 || otherVal / size != currentRow) continue;

                    if ((val % size > otherVal % size) && (currentCol < j % size)) {
                        h += 2;
                    }
                }
            }
        }
        return h;
    }

    private final Map<String, Integer> heuristicCache = new HashMap<>();

    private int getCachedHeuristic(int[] estado) {
        String key = Arrays.toString(estado);
        Integer h = heuristicCache.get(key);
        if (h == null) {
            h = calcularHeuristicaOptimizada(estado);
            heuristicCache.put(key, h);
        }
        return h;
    }

    private List<Nodo> generarVecinos(Nodo nodo) {
        List<Nodo> vecinos = new ArrayList<>(4);
        int emptyPos = nodo.emptyPos;
        int puzzleSize = this.puzzleSize;

        // Generar movimientos sin objetos temporales
        if (emptyPos % puzzleSize > 0) { // Izquierda
            agregarVecino(nodo, emptyPos - 1, vecinos);
        }
        if (emptyPos % puzzleSize < puzzleSize - 1) { // Derecha
            agregarVecino(nodo, emptyPos + 1, vecinos);
        }
        if (emptyPos >= puzzleSize) { // Arriba
            agregarVecino(nodo, emptyPos - puzzleSize, vecinos);
        }
        if (emptyPos < puzzleSize * (puzzleSize - 1)) { // Abajo
            agregarVecino(nodo, emptyPos + puzzleSize, vecinos);
        }
        return vecinos;
    }

    private void agregarVecino(Nodo padre, int nuevaPos, List<Nodo> vecinos) {
        int[] nuevoEstado = Arrays.copyOf(padre.estado, padre.estado.length);
        nuevoEstado[padre.emptyPos] = nuevoEstado[nuevaPos];
        nuevoEstado[nuevaPos] = -1; // Representación de vacío

        int heuristica = calcularHeuristicaOptimizada(nuevoEstado);
        vecinos.add(new Nodo(nuevoEstado, nuevaPos, padre.costo + 1, heuristica, padre));
    }

    private boolean esEstadoVisitado(Map<String, Integer> visitados, String claveEstado, int nuevoCosto) {
        return visitados.containsKey(claveEstado) && visitados.get(claveEstado) <= nuevoCosto;
    }

    private boolean isValidMove(int currentPos, int newPos) {
        if (newPos < 0 || newPos >= puzzleSize * puzzleSize) return false;
        int rowDiff = Math.abs((currentPos / puzzleSize) - (newPos / puzzleSize));
        int colDiff = Math.abs((currentPos % puzzleSize) - (newPos % puzzleSize));
        return (rowDiff + colDiff) == 1;
    }

    private void mostrarSolucion(List<Nodo> camino) {
        new Handler(Looper.getMainLooper()).post(() -> {
            stopTimer();
            for (int i = 0; i < camino.size(); i++) {
                final int index = i;
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Nodo nodo = camino.get(index);
                    List<PuzzlePiece> nuevoEstado = new ArrayList<>(puzzleSize * puzzleSize);

                    for (int pos : nodo.estado) {
                        if (pos == -1) {
                            nuevoEstado.add(new PuzzlePiece(null, emptyPositionSolved));
                        } else {
                            nuevoEstado.add(solvedPuzzle.get(pos));
                        }
                    }

                    puzzlePieces = nuevoEstado;
                    emptyPosition = nodo.emptyPos;
                    displayPuzzle(puzzlePieces);
                }, i * 500);
            }
        });
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

    private String estadoToString(List<PuzzlePiece> estado) {
        int[] stateArray = new int[estado.size()];
        for (int i = 0; i < estado.size(); i++) {
            stateArray[i] = estado.get(i).originalPosition;
        }
        return Arrays.toString(stateArray);
    }

    private void displayPuzzle(List<PuzzlePiece> pieces) {
        runOnUiThread(() -> {
            gridLayout.removeAllViews();
            gridLayout.setColumnCount(puzzleSize);
            gridLayout.setRowCount(puzzleSize);

            for (int i = 0; i < pieces.size(); i++) {
                ImageView imageView = new ImageView(this);
                PuzzlePiece piece = pieces.get(i);

                if (piece.bitmap != null) {
                    imageView.setImageBitmap(piece.bitmap);
                } else {
                    imageView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }

                final int position = i;
                imageView.setOnClickListener(v -> movePiece(position));

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = gridLayout.getWidth() / puzzleSize;
                params.height = gridLayout.getHeight() / puzzleSize;
                params.rowSpec = GridLayout.spec(i / puzzleSize);
                params.columnSpec = GridLayout.spec(i % puzzleSize);
                imageView.setLayoutParams(params);

                gridLayout.addView(imageView);
            }
        });
    }

    private void movePiece(int position) {
        if (isValidMove(emptyPosition, position)) {
            Collections.swap(puzzlePieces, position, emptyPosition);
            int temp = emptyPosition;
            emptyPosition = position;
            displayPuzzle(puzzlePieces);

            if (isPuzzleSolved()) {
                stopTimer();
                Toast.makeText(this, "¡Resuelto en " + seconds + " segundos!", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Método actualizado para verificar solución
    private boolean isPuzzleSolved() {
        for (int i = 0; i < puzzlePieces.size(); i++) {
            PuzzlePiece piece = puzzlePieces.get(i);
            if (piece.bitmap == null) {
                if (i != emptyPositionSolved) return false;
            } else if (piece.originalPosition != i) {
                return false;
            }
        }
        return true;
    }

    private void startTimer() {
        isTimerRunning = true;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isTimerRunning) {
                    seconds++;
                    timerTextView.setText(String.format(Locale.getDefault(),
                            "Tiempo: %02d:%02d", seconds / 60, seconds % 60));
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void stopTimer() {
        isTimerRunning = false;
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                photoUri = FileProvider.getUriForFile(this,
                        "com.example.rompe.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return File.createTempFile(
                "JPEG_" + timeStamp + "_",
                ".jpg",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            try {
                if (requestCode == PICK_IMAGE && data != null) {
                    Uri uri = data.getData();
                    bitmap = loadAndRotateBitmap(uri);
                } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    bitmap = loadAndRotateBitmap(photoUri);
                }

                if (bitmap != null) {
                    bitmap = processBitmap(bitmap);
                    originalBitmap = processBitmap(bitmap);;
                    fullImageView.setImageBitmap(bitmap);
                    createPuzzle(bitmap);
                    startTimer();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap loadAndRotateBitmap(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        ExifInterface exif = new ExifInterface(inputStream);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        return rotateBitmap(bitmap, orientation);
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap processBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap squared = Bitmap.createBitmap(bitmap,
                (bitmap.getWidth() - size) / 2,
                (bitmap.getHeight() - size) / 2,
                size,
                size
        );

        // Optimizar según tamaño del puzzle
        int targetSize = 200 * puzzleSize; // 400 para 2x2, 600 para 3x3, etc.
        return Bitmap.createScaledBitmap(squared, targetSize, targetSize, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            }
        }
    }

    // Modificar la clase Nodo para usar primitivos
    private static class Nodo {
        int[] estado; // Array de originalPositions (-1 = vacío)
        int emptyPos;
        int costo;
        int heuristica;
        int costoTotal;
        Nodo padre;

        Nodo(int[] estado, int emptyPos, int costo, int heuristica, Nodo padre) {
            this.estado = Arrays.copyOf(estado, estado.length);
            this.emptyPos = emptyPos;
            this.costo = costo;
            this.heuristica = heuristica;
            this.costoTotal = costo + heuristica;
            this.padre = padre;
        }
    }
}