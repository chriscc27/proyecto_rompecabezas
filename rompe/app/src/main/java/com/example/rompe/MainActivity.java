package com.example.rompe;

import android.os.Bundle;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements MenuFragment.OnMenuInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MenuFragment())
                    .commit();
        }
    }

    @Override
    public void onNormalPuzzleSelected(int size) {
        Intent loadingIntent = new Intent(this, LoadingActivity.class);
        loadingIntent.putExtra("target_activity", MainActivity.class.getName());
        loadingIntent.putExtra("puzzleSize", size);
        startActivity(loadingIntent);
    }

    @Override
    public void onUploadImageSelected(int size) {
        Intent loadingIntent = new Intent(this, LoadingActivity.class);
        loadingIntent.putExtra("target_activity", FotoActivity.class.getName());
        loadingIntent.putExtra("modo", "galeria");
        loadingIntent.putExtra("puzzleSize", size);
        startActivity(loadingIntent);
    }

    @Override
    public void onTakePhotoSelected(int size) {
        Intent loadingIntent = new Intent(this, LoadingActivity.class);
        loadingIntent.putExtra("target_activity", FotoActivity.class.getName());
        loadingIntent.putExtra("modo", "camara");
        loadingIntent.putExtra("puzzleSize", size);
        startActivity(loadingIntent);
    }

    @Override
    public void onScoresSelected() {
        startActivity(new Intent(this, ScoreActivity.class));
    }
}