package com.example.rompe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.rompe.DatabaseHelper;
import com.example.rompe.R;
import com.example.rompe.Score;
import android.widget.EditText;
import android.app.Activity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class FotoFragment extends Fragment {
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
    private int moves;
    private File photoFile;

    private static class PuzzlePiece {
        Bitmap bitmap;
        int originalPosition;

        PuzzlePiece(Bitmap bitmap, int originalPosition) {
            this.bitmap = bitmap;
            this.originalPosition = originalPosition;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_foto, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gridLayout = view.findViewById(R.id.gridLayout);
        fullImageView = view.findViewById(R.id.fullImageView);
        timerTextView = view.findViewById(R.id.timerTextView);
        btnResolver = view.findViewById(R.id.btnResolver);

        // Validar que los argumentos existen
        if (getArguments() == null) {
            Toast.makeText(getContext(), "Error: Argumentos no proporcionados", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack(); // Regresar si no hay argumentos
            return;
        }

        puzzleSize = getArguments().getInt("puzzleSize", 3);
        String modo = getArguments().getString("modo");

        if (modo == null) {
            Toast.makeText(getContext(), "Error: Modo no especificado", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        checkPermissions(modo);

        btnResolver.setOnClickListener(v -> resolverRompecabezas());
    }


    private void checkPermissions(String modo) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            // Permisos ya concedidos, proceder
            if (modo.equals("galeria")) {
                abrirGaleria();
            } else if (modo.equals("camara")) {
                abrirCamara();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(getContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirGaleria();
            } else {
                Toast.makeText(getContext(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createPuzzle(Bitmap bitmap) {
        moves = 0; // Variable para contar los movimientos

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

        runOnUiThread(() -> {
            fullImageView.setImageBitmap(solvedBitmap);
            fullImageView.setBackgroundResource(R.drawable.image_border);
            fullImageView.setPadding(20, 20, 20, 20);
        });

        // Mezclar piezas desde el estado resuelto
        puzzlePieces = new ArrayList<>(solvedPuzzle);
        emptyPosition = emptyPositionSolved;
        Random random = new Random();
        int shuffleMoves = 125;
        int movesDone = 0;

        while (movesDone < shuffleMoves) {
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
                movesDone++;
            }
        }

        displayPuzzle(puzzlePieces);
        startTimer();
    }


    private int getCachedHeuristic(int[] estado) {
        return calcularHeuristicaOptimizada(estado) * 2;  // Prioriza nodos más profundos
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
                Toast.makeText(requireContext(), "Solución no encontrada", Toast.LENGTH_SHORT).show(); // Corregir contexto
                regeneratePuzzle();
            });
        }).start();
    }

    private void mostrarDialogoReintentar() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sigue intentándolo")
                .setMessage("¿Quieres probar de nuevo?")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    regeneratePuzzle();
                    startTimer();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }


    // Reemplazar runOnUiThread con:
    private void runOnUiThread(Runnable action) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(action);
        }
    }

    private void mostrarDialogoExito() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // Inicializar correctamente
        builder.setTitle("¡Felicidades!");
        String mensaje = "Resuelto en " + moves + " movimientos y " + seconds + " segundos!\n"
                + "Introduce tu nombre para guardar la puntuación:";
        builder.setMessage(mensaje);

        final EditText input = new EditText(requireContext()); // Corregir 'this' por requireContext()
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nombre = input.getText().toString().trim();
            if(nombre.isEmpty()) nombre = "Anónimo";
            saveScoreToDatabase(nombre);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.setCancelable(false)
                .create()
                .show();
    }

    private void navegarAPuntuaciones() {
        Navigation.findNavController(requireView()).navigate(R.id.action_fotoFragment_to_scoresFragment);
    }

    private void regeneratePuzzle() {
        seconds = 0;
        if (originalBitmap != null) {
            createPuzzle(originalBitmap);
        }
        timerTextView.setText("Tiempo: 00:00");
    }

    private void saveScoreToDatabase(String name) {
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        Score score = new Score(
                0,
                name,
                seconds,
                moves,
                puzzleSize + "x" + puzzleSize,
                null,
                "imagen"
        );
        dbHelper.saveScore(score);

        Toast.makeText(requireContext(), "Puntuación guardada", Toast.LENGTH_SHORT).show();
        navegarAPuntuaciones();
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



    private List<Nodo> generarVecinos(Nodo nodo) {
        List<Nodo> vecinos = new ArrayList<>(4);
        int emptyPos = nodo.emptyPos;
        int puzzleSize = this.puzzleSize;

        // Generar movimientos en orden aleatorio para mejor distribución
        List<Integer> direcciones = Arrays.asList(-1, 1, -puzzleSize, puzzleSize);
        Collections.shuffle(direcciones);

        for (int dir : direcciones) {
            int newPos = emptyPos + dir;
            if (isValidMove(emptyPos, newPos)) {
                agregarVecino(nodo, newPos, vecinos);
            }
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

            // Mostrar diálogo después de completar la animación
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                            mostrarDialogoReintentar(),
                    camino.size() * 500 + 1000
            );
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

    private void displayPuzzle(List<PuzzlePiece> pieces) {
        requireActivity().runOnUiThread(() -> {
            gridLayout.removeAllViews();
            gridLayout.setColumnCount(puzzleSize);
            gridLayout.setRowCount(puzzleSize);

            for (int i = 0; i < pieces.size(); i++) {
                // En displayPuzzle()
                ImageView imageView = new ImageView(requireContext()); // Corregir 'this' por requireContext()
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
                Toast.makeText(requireContext(), "¡Resuelto en " + seconds + " segundos!", Toast.LENGTH_LONG).show();
            }
        }
        moves++;
    }

    private boolean isPuzzleSolved() {
        for (int i = 0; i < puzzlePieces.size(); i++) {
            PuzzlePiece piece = puzzlePieces.get(i);
            if (piece.bitmap == null) {
                if (i != emptyPositionSolved) return false;
            } else if (piece.originalPosition != i) {
                return false;
            }
        }
        stopTimer();
        mostrarDialogoExito();
        return true;
    }

    private void startTimer() {
        isTimerRunning = true;
        // Limpiar cualquier callback pendiente
        handler.removeCallbacksAndMessages(null);

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isTimerRunning) {
                    seconds++;
                    timerTextView.setText(String.format(Locale.getDefault(),
                            "Tiempo: %02d:%02d", seconds / 60, seconds % 60));
                    // Programar siguiente ejecución
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void stopTimer() {
        isTimerRunning = false;
    }

    // Modificar métodos de captura de imagen
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
                photoUri = FileProvider.getUriForFile(requireContext(),
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

        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(
                "JPEG_" + timeStamp + "_",
                ".jpg",
                storageDir // Usar el directorio obtenido
        );
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = null;
            try {
                if (requestCode == PICK_IMAGE && data != null) {
                    Uri uri = data.getData();
                    bitmap = loadAndRotateBitmap(uri);
                } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    if (photoFile != null) {
                        ExifInterface exif = new ExifInterface(photoFile.getAbsolutePath());
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        bitmap = rotateBitmap(bitmap, orientation);
                    }
                }

                if (bitmap != null) {
                    bitmap = processBitmap(bitmap);
                    originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    fullImageView.setImageBitmap(originalBitmap);
                    createPuzzle(originalBitmap);
                    startTimer();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap loadAndRotateBitmap(Uri uri) throws IOException {
        InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        inputStream = requireActivity().getContentResolver().openInputStream(uri);
        ExifInterface exif = new ExifInterface(inputStream);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        inputStream.close();

        return rotateBitmap(bitmap, orientation);
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1, -1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(270);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                break;
        }

        try {
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    private Bitmap processBitmap(Bitmap bitmap) {
        int inSampleSize = bitmap.getWidth() > 2000 ? 4 : 2;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        int targetSize = 200 * puzzleSize;
        return Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true);
    }


    private static class Nodo {
        int[] estado;
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