package edu.upc.epsevg.prop.oust;

import java.awt.Point;
import java.util.*;

public class GameStatusTunned extends GameStatus {

    private MyStatus info;
    /**
     * Constructor que crea un nou estat optimitzat a partir d'un GameStatus existent.
     * Realitza un càlcul inicial complet de les estadístiques.
     *
     * @param gs L'estat base del joc (GameStatus).
     */
    public GameStatusTunned(GameStatus gs) {
        super(gs);
        info = new MyStatus();
        recomputeAll();
    }
    /**
     * Retorna l'objecte amb la informació estadística actualitzada del tauler.
     *
     * @return Instància de {@link MyStatus} amb les mètriques actuals.
     */
    public MyStatus getInfo() {
        return info;
    }
    /**
     * Col·loca una fitxa al tauler i actualitza immediatament les estadístiques
     * de joc. Detecta si s'ha produït una captura comparant el recompte de
     * fitxes abans i després.
     *
     * @param point Coordenades on es vol col·locar la fitxa.
     */
    @Override
    public void placeStone(Point point) {
        int before1 = info.stonesP1;
        int before2 = info.stonesP2;

        super.placeStone(point);

        recomputeAll();

        // detectar captura
        if (info.stonesP1 < before1 || info.stonesP2 < before2) {
            info.lastMoveWasCapture = true;
        } else {
            info.lastMoveWasCapture = false;
        }
    }

    // =============================
    // Recalcular toda la heurística
    // =============================
    /**
     * Recalcula totes les estadístiques del tauler (recompte de fitxes i
     * grups). Es crida després de cada modificació del tauler.
     */
    private void recomputeAll() {
        info.stonesP1 = countStones(PlayerType.PLAYER1);
        info.stonesP2 = countStones(PlayerType.PLAYER2);

        info.biggestGroupP1 = biggestGroup(PlayerType.PLAYER1);
        info.biggestGroupP2 = biggestGroup(PlayerType.PLAYER2);
    }

    // =============================
    // Funciones auxiliares
    // =============================
    /**
     * Compta el nombre total de fitxes d'un jugador específic.
     *
     * @param p El tipus de jugador (PLAYER1 o PLAYER2).
     * @return Nombre de fitxes.
     */
    private int countStones(PlayerType p) {
        int count = 0;
        int size = getSquareSize();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (isInBounds(new Point(x, y)) && getColor(x, y) == p) {
                    count++;
                }
            }
        }
        return count;
    }
    /**
     * Troba la mida del grup més gran de fitxes connectades per a un jugador.
     *
     * @param p El jugador a avaluar.
     * @return La mida (nombre de fitxes) del grup més gran.
     */ 
    private int biggestGroup(PlayerType p) {
        int size = getSquareSize();
        boolean[][] visited = new boolean[size][size];
        int best = 0;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {

                Point start = new Point(x, y);
                if (!isInBounds(start)) continue;
                if (visited[x][y]) continue;
                if (getColor(x, y) != p) continue;

                int group = flood(start, p, visited);
                best = Math.max(best, group);
            }
        }
        return best;
    }

    /**
     * Algorisme d'inundació (Flood Fill) per comptar la mida d'un grup
     * connectat.
     *
     * @param start Punt d'inici del grup.
     * @param p Jugador propietari de les fitxes.
     * @param visited Matriu de visitats per evitar bucles.
     * @return Nombre de fitxes en el grup actual.
     */
    private int flood(Point start, PlayerType p, boolean[][] visited) {
        Stack<Point> st = new Stack<>();
        st.push(start);
        visited[start.x][start.y] = true;

        int total = 0;

        while (!st.isEmpty()) {
            Point u = st.pop();
            total++;

            for (Dir d : Dir.values()) {
                Point v = d.add(u);
                if (!isInBounds(v)) continue;
                if (visited[v.x][v.y]) continue;
                if (getColor(v.x, v.y) != p) continue;

                visited[v.x][v.y] = true;
                st.push(v);
            }
        }
        return total;
    }
}
