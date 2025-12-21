/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.oust.players.RamosLopez;

import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.IAuto;
import edu.upc.epsevg.prop.oust.IPlayer;
import edu.upc.epsevg.prop.oust.PlayerMove;
import edu.upc.epsevg.prop.oust.SearchType;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * MiniMax IDS: profundidades 1,2,3... hasta timeout.
 * Reutiliza PlayerMiniMax creando instancias por profundidad.
 */
public class PlayerMiniMaxIDS implements IPlayer, IAuto {

    private volatile boolean timeout = false;
    private final String name = "MiniMaxIDS";

    public PlayerMiniMaxIDS() {}

    @Override
    public PlayerMove move(GameStatus s) {
        timeout = false;

        // Mejor resultado de la última profundidad completada
        List<Point> bestPathSoFar = new ArrayList<>();
        int depth = 1;

        // Iterative deepening: subimos hasta que nos corten por timeout
        while (!timeout) {
            PlayerMiniMax mm = new PlayerMiniMax(depth);

            List<Point> candidate = mm.bestPath(s);

            if (!timeout) {
                bestPathSoFar = candidate;
            }

            depth++;
        }

        return new PlayerMove(bestPathSoFar, 0, depth - 1, SearchType.MINIMAX_IDS);
    }

    @Override
    public void timeout() {
        timeout = true;
    }

    @Override
    public String getName() {
        return name;
    }
}
