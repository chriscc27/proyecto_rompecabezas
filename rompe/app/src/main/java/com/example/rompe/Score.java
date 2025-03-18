package com.example.rompe;

public class Score {
    private int id;
    private String name;
    private int time;
    private int moves;
    private String type;
    private String date;



    // Constructor completo
    public Score(int id, String name, int time, int moves, String type, String date) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.moves = moves;
        this.type = type;
        this.date = date;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}