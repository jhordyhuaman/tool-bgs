/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pe.innobyte.toosanalizer.utils;

/**
 *
 * @author nik_1
 */
public class ExelTable {
    private String date; 
    private String hours;
    private float intencity;
    private int kind;
    private int heart;
    private int steps;

  

    public ExelTable(String date, String hours, float intencity, int kind, int heart, int steps) {
        this.date = date;
        this.hours = hours;
        this.intencity = intencity;
        this.kind = kind;
        this.heart = heart;
        this.steps = steps;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }
   
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public float getIntencity() {
        return intencity;
    }

    public void setIntencity(float intencity) {
        this.intencity = intencity;
    }

    public int getHeart() {
        return heart;
    }

    public void setHeart(int heart) {
        this.heart = heart;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }


}
