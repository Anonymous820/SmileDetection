package com.example.smiledetection.model;

public class SuitcaseFaceDetection {

    int id;
    String text;

    public SuitcaseFaceDetection() {
    }

    public SuitcaseFaceDetection(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "SuitcaseFaceDetection{" +
                "id=" + id +
                ", text='" + text + '\'' +
                '}';
    }
}
