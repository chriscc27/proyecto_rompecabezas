package com.example.rompe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;
import java.util.ArrayList;





public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "puzzle.db";
    private static final int DATABASE_VERSION = 2;

    // Tabla de puntuaciones
    public static final String TABLE_SCORES = "scores";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_MOVES = "moves";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DATE = "date";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_SCORES + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_TIME + " INTEGER NOT NULL, " +
                    COLUMN_MOVES + " INTEGER NOT NULL, " +
                    COLUMN_TYPE + " TEXT NOT NULL, " +
                    COLUMN_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    public List<Score> getTopScores(String puzzleType, int limit) {
        List<Score> scores = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // En DatabaseHelper.java, modifica la consulta en getTopScores
        String query = "SELECT * FROM " + TABLE_SCORES +
                " WHERE `" + COLUMN_TYPE + "` = ?" +  // Usar backticks
                " ORDER BY `" + COLUMN_TIME + "` ASC, `" + COLUMN_MOVES + "` ASC" +
                " LIMIT ?";

        Cursor cursor = db.rawQuery(query, new String[]{puzzleType, String.valueOf(limit)});

        if (cursor.moveToFirst()) {
            do {
                Score score = new Score(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MOVES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                );
                scores.add(score);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return scores;
    }

    public void saveScore(Score score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, score.getName());
        values.put(COLUMN_TIME, score.getTime());
        values.put(COLUMN_MOVES, score.getMoves());
        values.put(COLUMN_TYPE, score.getType());

        db.insert(TABLE_SCORES, null, values);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES); // Usar la constante TABLE_SCORES
        onCreate(db);
    }

}
