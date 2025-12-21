/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.oust.players.RamosLopez; // <-- CAMBIA ESTO

import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.IAuto;
import edu.upc.epsevg.prop.oust.IPlayer;
import edu.upc.epsevg.prop.oust.PlayerMove;
import edu.upc.epsevg.prop.oust.PlayerType;
import edu.upc.epsevg.prop.oust.SearchType;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * MiniMax (en realidad Negamax) con alpha-beta.
 * Profundidad fija. Ignora el tiempo (pero implementa timeout() por interfaz).
 */
public class PlayerMiniMax implements IPlayer, IAuto {

    private final String name;
    private final int maxDepth;

    // Aunque "ignore el tiempo", debe existir timeout() por interfaz.
    // Lo dejamos para compatibilidad. No lo usamos para cortar en este jugador.
    private volatile boolean timeoutFlag = false;

    public PlayerMiniMax(int profunditatMaxima) {
        this.name = "MiniMax(" + profunditatMaxima + ")";
        this.maxDepth = Math.max(1, profunditatMaxima);
    }

    @Override
    public PlayerMove move(GameStatus s){
        timeoutFlag = false;
        List<Point> path = bestPath(s);
        return new PlayerMove(path, 0, maxDepth, SearchType.MINIMAX);
    }

    @Override
    public void timeout() {
        timeoutFlag = true; // no lo usamos para cortar aquí
    }

    @Override
    public String getName() {
        return name;
    }

    // -------------------------
    // Negamax + alpha-beta
    // -------------------------
    private double negamax(GameStatus s, int depth, double alpha, double beta, PlayerType rootPlayer) {
        // Si quisieras cortar por timeout aquí, podrías:
        // if (timeoutFlag) return evaluate(s, rootPlayer);
        if (depth <= 0) return evaluate(s, rootPlayer);

        PlayerType current = s.getCurrentPlayer();

        // Generar acciones del turno del jugador actual
        List<List<Point>> actions = generateTurnActions(new GameStatus(s), current);

        // Si no hay acciones: es un "pass"
        if (actions.isEmpty()) {
            // Devuelve evaluación desde la perspectiva de rootPlayer
            return evaluate(s, rootPlayer);
        }

        double best = Double.NEGATIVE_INFINITY;

        for (List<Point> path : actions) {
            GameStatus child = new GameStatus(s);
            applyPath(child, path);

            double score = -negamax(child, depth - 1, -beta, -alpha, rootPlayer);

            if (score > best) best = score;
            if (best > alpha) alpha = best;
            if (alpha >= beta) break; // poda
        }

        return best;
    }

    // -------------------------
    // Generación de jugadas (paths completos de UN turno)
    // -------------------------
    private List<List<Point>> generateTurnActions(GameStatus s, PlayerType turnPlayer) {
        List<List<Point>> results = new ArrayList<>();
        List<Point> prefix = new ArrayList<>();

        // Si no hay moves, no hay acciones (pass)
        if (s.getMoves().isEmpty()) return results;

        genRec(s, turnPlayer, prefix, results);
        return results;
    }

    private void genRec(GameStatus s, PlayerType turnPlayer, List<Point> prefix, List<List<Point>> results) {
        List<Point> moves = s.getMoves();

        // Si ya cambió el turno, prefix es una jugada completa
        if (s.getCurrentPlayer() != turnPlayer) {
            results.add(new ArrayList<>(prefix));
            return;
        }

        // Si no hay moves, termina (se interpreta como fin/paso)
        if (moves.isEmpty()) {
            results.add(new ArrayList<>(prefix));
            return;
        }

        // Para cada colocación posible...
        for (Point m : moves) {
            GameStatus child = new GameStatus(s);
            child.placeStone(m);

            prefix.add(m);
            genRec(child, turnPlayer, prefix, results);
            prefix.remove(prefix.size() - 1);
        }
    }

    private void applyPath(GameStatus s, List<Point> path) {
        for (Point p : path) {
            s.placeStone(p);
        }
    }

    /**
     * Heurística simple, sin necesitar APIs extra:
     * - Movilidad del jugador actual y del rival aproximada.
     *
     * Importante: como no tenemos un método "setCurrentPlayer",
     * sólo medimos la movilidad del que está a turno en este estado.
     */
    private double evaluate(GameStatus s, PlayerType rootPlayer) {
        // movilidad del jugador que tiene el turno AHORA
        int mobility = s.getMoves().size();

        // si el turno actual es rootPlayer => positivo, si no => negativo
        return (s.getCurrentPlayer() == rootPlayer) ? mobility : -mobility;
    }
    public List<Point> bestPath(GameStatus s) {
    // Copiamos la lógica de move(), pero devolvemos solo el path
    PlayerType me = s.getCurrentPlayer();
    List<List<Point>> actions = generateTurnActions(new GameStatus(s), me);

    if (actions.isEmpty()) return new ArrayList<>();

    double bestVal = Double.NEGATIVE_INFINITY;
    List<Point> bestPath = actions.get(0);

    for (List<Point> path : actions) {
        GameStatus child = new GameStatus(s);
        applyPath(child, path);

        double val = -negamax(child, maxDepth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, me);

        if (val > bestVal) {
            bestVal = val;
            bestPath = path;
        }
    }
    return bestPath;
}

}
