/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.oust;

public class MyStatus {

    // Conteo de fichas
    public int stonesP1;
    public int stonesP2;

    // Tamaño del grupo más grande
    public int biggestGroupP1;
    public int biggestGroupP2;

    // Última jugada fue captura
    public boolean lastMoveWasCapture;

    public MyStatus() {
        stonesP1 = 0;
        stonesP2 = 0;
        biggestGroupP1 = 0;
        biggestGroupP2 = 0;
        lastMoveWasCapture = false;
    }
}
