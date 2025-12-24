package edu.upc.epsevg.prop.oust.players.ramos47262693Q_lopez46497328E;

import edu.upc.epsevg.prop.oust.*;
import java.awt.Point;
import java.util.*;

public class IDSMinimaxPlayer implements IPlayer, IAuto {

    private PlayerType me;
    private long timeLimit;
    private long startTime;

    private static final int TT_SIZE = 100_000;

    private final HashMap<Long, TTEntry> transTable = new HashMap<>(TT_SIZE);

    public IDSMinimaxPlayer(long milliseconds) {
        this.timeLimit = milliseconds;
    }

    @Override
    public String getName() {
        return "IDSMinimaxPlayer";
    }

    // =====================================================
    // MOVE PRINCIPAL
    // =====================================================

    @Override
    public PlayerMove move(GameStatus gs) {

        startTime = System.currentTimeMillis();
        me = gs.getCurrentPlayer();
        transTable.clear();

        GameStatusTunned root = new GameStatusTunned(gs);

        List<Point> bestSequence = new ArrayList<>();

        for (int depth = 1; ; depth++) {
            try {
                List<Point> candidate = searchDepth(root, depth);
                if (!candidate.isEmpty()) bestSequence = candidate;
            } catch (TimeUp e) {
                break;
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        return new PlayerMove(bestSequence, elapsed, 0, SearchType.MINIMAX);
    }

    // =====================================================

    private List<Point> searchDepth(GameStatusTunned root, int depth) {
        GameStatusTunned state = new GameStatusTunned(root);
        List<Point> seq = new ArrayList<>();

        while (true) {
            checkTime();

            Result r = alphabeta(state, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

            if (r.move == null) break;

            seq.add(r.move);
            state.placeStone(r.move);

            if (!state.getInfo().lastMoveWasCapture) break;
        }

        return seq;
    }

    // =====================================================
    // ALPHA BETA + TT
    // =====================================================

    private Result alphabeta(GameStatusTunned s, int depth, int alpha, int beta, boolean maximizing) {

        checkTime();

        long key = computeHash(s);
        TTEntry entry = transTable.get(key);

        if (entry != null && entry.depth >= depth) {
            return new Result(entry.value, entry.bestMove);
        }

        if (depth == 0 || s.isGameOver()) {
            int val = evaluate(s);
            transTable.put(key, new TTEntry(val, null, depth));
            return new Result(val, null);
        }

        List<Point> moves = generateMoves(s);
        if (moves.isEmpty()) {
            int val = evaluate(s);
            transTable.put(key, new TTEntry(val, null, depth));
            return new Result(val, null);
        }

        // Ordenar por capturas
        moves.sort((a, b) -> captureGain(s, b) - captureGain(s, a));

        Point bestMove = null;
        int bestVal = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Point m : moves) {

            GameStatusTunned next = new GameStatusTunned(s);
            next.placeStone(m);

            int val = alphabeta(next, depth - 1, alpha, beta, !maximizing).value;

            if (maximizing) {
                if (val > bestVal) { bestVal = val; bestMove = m; }
                alpha = Math.max(alpha, bestVal);
            } else {
                if (val < bestVal) { bestVal = val; bestMove = m; }
                beta = Math.min(beta, bestVal);
            }

            if (beta <= alpha) break;
        }

        transTable.put(key, new TTEntry(bestVal, bestMove, depth));
        return new Result(bestVal, bestMove);
    }

    // =====================================================
    // TRANSPOSICIÓN
    // =====================================================

    private long computeHash(GameStatusTunned s) {
        long h = 1469598103934665603L;
        int size = s.getSquareSize();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                PlayerType c = s.getColor(x, y);
                long v = (c == null ? 0 : (c == PlayerType.PLAYER1 ? 1 : 2));
                h = (h ^ (v + 31 * x + 131 * y)) * 1099511628211L;
            }
        }
        return h;
    }

    private static class TTEntry {
        int value;
        Point bestMove;
        int depth;

        TTEntry(int v, Point m, int d) {
            value = v;
            bestMove = m;
            depth = d;
        }
    }

    private List<Point> generateMoves(GameStatus s) {
    List<Point> list = new ArrayList<>();
    int size = s.getSquareSize();

    for (int x = 0; x < size; x++) {
        for (int y = 0; y < size; y++) {
            Point p = new Point(x, y);
            if (!s.isInBounds(p)) continue;

            try {
                GameStatus copy = new GameStatus(s);
                copy.placeStone(p);
                list.add(p);
            } catch (Exception e) {}
        }
    }
    return list;
}

    // =====================================================
    // HEURÍSTICA DE COMPETICIÓN
    // =====================================================

    private int evaluate(GameStatusTunned s) {

        MyStatus info = s.getInfo();
        PlayerType turn = s.getCurrentPlayer();

        int myStones  = (me == PlayerType.PLAYER1) ? info.stonesP1 : info.stonesP2;
        int oppStones = (me == PlayerType.PLAYER1) ? info.stonesP2 : info.stonesP1;

        int myBig  = (me == PlayerType.PLAYER1) ? info.biggestGroupP1 : info.biggestGroupP2;
        int oppBig = (me == PlayerType.PLAYER1) ? info.biggestGroupP2 : info.biggestGroupP1;

        int myGroups  = countGroups(s, me);
        int oppGroups = countGroups(s, opponentOf(me));

        int myBestCap  = (turn == me) ? bestCaptureFor(s, me) : 0;
        int oppBestCap = (turn != me) ? bestCaptureFor(s, opponentOf(me)) : 0;

        int myMob  = (turn == me) ? countLegalMovesFor(s, me) : 0;
        int oppMob = (turn != me) ? countLegalMovesFor(s, opponentOf(me)) : 0;

        int score = 0;

        score += (myBig - oppBig) * 60;
        score += (oppGroups - myGroups) * 15;
        score += (myMob - oppMob) * 8;
        score += (myBestCap - oppBestCap) * 50;
        score += (myStones - oppStones) * 2;

        if (info.lastMoveWasCapture && turn == me) score += 25;

        return score;
    }

    // =====================================================
    // AUXILIARES HEURÍSTICA
    // =====================================================

    private PlayerType opponentOf(PlayerType p) {
        return (p == PlayerType.PLAYER1) ? PlayerType.PLAYER2 : PlayerType.PLAYER1;
    }

    private int bestCaptureFor(GameStatusTunned s, PlayerType p) {
        if (s.getCurrentPlayer() != p) return 0;

        int best = 0;
        List<Point> moves = generateMoves(s);

        for (Point m : moves) {
            int cap = captureGain(s, m);
            if (cap > best) best = cap;
        }
        return best;
    }

    private int captureGain(GameStatusTunned s, Point m) {
        try {
            GameStatusTunned copy = new GameStatusTunned(s);
            PlayerType turn = copy.getCurrentPlayer();

            int before = (turn == PlayerType.PLAYER1)
                    ? copy.getInfo().stonesP2
                    : copy.getInfo().stonesP1;

            copy.placeStone(m);

            int after = (turn == PlayerType.PLAYER1)
                    ? copy.getInfo().stonesP2
                    : copy.getInfo().stonesP1;

            return Math.max(0, before - after);
        } catch (Exception e) {
            return 0;
        }
    }

    private int countGroups(GameStatus s, PlayerType p) {
        int size = s.getSquareSize();
        boolean[][] visited = new boolean[size][size];
        int groups = 0;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Point start = new Point(x, y);
                if (!s.isInBounds(start)) continue;
                if (visited[x][y]) continue;
                if (s.getColor(x, y) != p) continue;

                floodMark(s, start, p, visited);
                groups++;
            }
        }
        return groups;
    }

    private void floodMark(GameStatus s, Point start, PlayerType p, boolean[][] visited) {
        ArrayDeque<Point> q = new ArrayDeque<>();
        q.add(start);
        visited[start.x][start.y] = true;

        while (!q.isEmpty()) {
            Point u = q.poll();
            for (Dir d : Dir.values()) {
                Point v = d.add(u);
                if (!s.isInBounds(v)) continue;
                if (visited[v.x][v.y]) continue;
                if (s.getColor(v.x, v.y) != p) continue;

                visited[v.x][v.y] = true;
                q.add(v);
            }
        }
    }

    private int countLegalMovesFor(GameStatus s, PlayerType p) {
        int count = 0;
        int size = s.getSquareSize();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Point pt = new Point(x, y);
                if (!s.isInBounds(pt)) continue;

                try {
                    GameStatus copy = new GameStatus(s);
                    if (copy.getCurrentPlayer() != p) return count;
                    copy.placeStone(pt);
                    count++;
                } catch (Exception e) {}
            }
        }
        return count;
    }

    // =====================================================

    private void checkTime() {
        if (System.currentTimeMillis() - startTime >= timeLimit) {
            throw new TimeUp();
        }
    }

    private static class Result {
        int value;
        Point move;
        Result(int v, Point m) { value = v; move = m; }
    }

    private static class TimeUp extends RuntimeException {}

    @Override
    public void timeout() {}
}
