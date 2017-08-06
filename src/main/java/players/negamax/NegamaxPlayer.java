package players.negamax;

import core.GameInfo;
import core.GameState;
import core.Move;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import players.Player;

import static gui.views.BoardPane.convertMoveAlgebraic;

/**
 * Negamax player, with alpha-beta pruning and further optimisations
 */
public class NegamaxPlayer extends Player {

    private final ThreatUtils reducer;
    private final Evaluator evaluator;
    private final Cache<Long, MoveEntry> moveTable;

    private long time;
    private long startTime;

    private int totalNodeCount;
    private int nonLeafCount;
    private int branchesExploredSum;

    private State state;

    public NegamaxPlayer(GameInfo info) {
        super(info);
        this.reducer = new ThreatUtils();
        this.evaluator = Evaluator.getInstance();
        this.time = (long) 2000 * 1000000;
        this.moveTable = new Cache<>(1000000);
    }

    /**
     * Generate a list of sorted and pruned moves for this state. Moves are
     * pruned when they are too far away from existing stones, and also when
     * threats are found which require an immediate response.
     * @param state State to get moves for
     * @return A list of moves, sorted and pruned
     */
    private List<Move> getSortedMoves(State state) {
        // Board is empty, return a move in the middle of the board
        if(state.getMoves() == 0) {
            List<Move> moves = new ArrayList<>();
            moves.add(new Move(state.board.length / 2, state.board.length / 2));
            return moves;
        }

        int playerIndex = state.currentIndex;
        int opponentIndex = state.currentIndex == 2 ? 1 : 2;

        HashSet<Move> fours = new HashSet<>();
        HashSet<Move> refutations = new HashSet<>();

        HashSet<Move> opponentFours = new HashSet<>();
        HashSet<Move> opponentThrees = new HashSet<>();

        // Check for threats first and respond to them if they exist
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == opponentIndex) {
                    opponentFours.addAll(reducer.getFours(state,
                            state.board[i][j], opponentIndex));
                    opponentThrees.addAll(reducer.getThrees(state,
                            state.board[i][j], opponentIndex));
                }
                else if(state.board[i][j].index == playerIndex) {
                    fours.addAll(reducer.getFours(state, state.board[i][j],
                            playerIndex));
                    refutations.addAll(reducer.getRefutations(state, state
                            .board[i][j], playerIndex));
                }
            }
        }

        // We have a four on the board, play it
        if(!fours.isEmpty()) {
            return new ArrayList<>(fours);
        }
        // Opponent has a four, defend against it
        if(!opponentFours.isEmpty()) {
            return new ArrayList<>(opponentFours);
        }
        // Opponent has a three, defend against it and add refutation moves
        if(!opponentThrees.isEmpty()) {
            opponentThrees.addAll(refutations);
            return new ArrayList<>(opponentThrees);
        }

        List<ScoredMove> scoredMoves = new ArrayList<>();

        MoveEntry entry = moveTable.get(state.getZobristHash());
        // Grab closest moves
        List<Move> moves = new ArrayList<>();
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                // Ignore hash move
                if(entry != null &&
                        (i == entry.move.row && j == entry.move.col)) {
                    continue;
                }
                if(state.board[i][j].index == 0) {
                    if(state.hasAdjacent(i, j, 2)) {
                        int score = evaluator.evaluateField(state, i, j,
                                state.currentIndex);
                        scoredMoves.add(new ScoredMove(new Move(i, j), score));
                    }
                }
            }
        }

        // Sort based on move score
        Collections.sort(scoredMoves);
        for(ScoredMove move : scoredMoves) {
            moves.add(move.move);
        }
        return moves;
    }

    /**
     * Run the negamax algorithm for a node in the game tree.
     * @param state Node to search
     * @param depth Depth to search to
     * @param alpha Alpha bound
     * @param beta Beta bound
     * @return Score of the node
     * @throws InterruptedException Timeout or interrupted by the user
     */
    private int negamax(State state, int depth, int alpha, int beta)
            throws InterruptedException {
        totalNodeCount++;
        if(Thread.interrupted() || (System.nanoTime() - startTime) > time) {
            throw new InterruptedException();
        }
        if(state.terminal() != 0 || depth == 0) {
            return evaluator.evaluateState(state, depth);
        }
        nonLeafCount++;

        int value;
        int best = Integer.MIN_VALUE;
        int count = 0;

        Move bestMove = null;

        // Try the move from a previous search
        MoveEntry hashMoveEntry = moveTable.get(state.getZobristHash());
        if (hashMoveEntry != null) {
            count++;
            state.makeMove(hashMoveEntry.move);
            value = -negamax(state, depth - 1, -beta, -alpha);
            state.undoMove(hashMoveEntry.move);
            if (value > best) {
                bestMove = hashMoveEntry.move;
                best = value;
            }
            if (best > alpha) alpha = best;
            if (best >= beta) return best;
        }

        // No cut-off from hash move, get sorted moves
        List<Move> moves = getSortedMoves(state);

        for (Move move : moves) {
            count++;
            state.makeMove(move);
            value = -negamax(state, depth - 1, -beta, -alpha);
            state.undoMove(move);
            if(value > best) {
                bestMove = move;
                best = value;
            }
            if(best > alpha) alpha = best;
            if(best >= beta) {
                break;
            }
        }
        branchesExploredSum += count;
        putMoveEntry(state.getZobristHash(), bestMove, depth);
        return best;
    }

    /**
     * Place the best move found from a state into the hash table, replacing
     * an existing entry if the state was searched to a higher depth
     * @param key Hash key of the state
     * @param move Move to save
     * @param depth Depth of the search
     */
    private void putMoveEntry(long key, Move move, int depth) {
        MoveEntry moveEntry = moveTable.get(key);
        if(moveEntry == null) {
            moveTable.put(key, new MoveEntry(move, depth));
        }
        else if(depth > moveEntry.depth) {
            moveTable.put(key, new MoveEntry(move, depth));
        }
    }

    /**
     * Run a depth-limited negamax search on a set of moves, sorting them by
     * score.
     * @param depth Depth to search to
     * @return Original move list sorted by best score first
     */
    private List<Move> searchMoves(State state, List<Move> moves, int depth)
            throws InterruptedException {

        List<ScoredMove> scoredMoves = new ArrayList<>();
        for(Move move : moves) {
            scoredMoves.add(new ScoredMove(move, Integer.MIN_VALUE));
        }

        int alpha = -11000;
        int beta = 11000;
        int best = Integer.MIN_VALUE;

        for(ScoredMove move : scoredMoves) {
            state.makeMove(move.move);
            move.score = -negamax(state, depth - 1, -beta, -alpha);
            state.undoMove(move.move);
            if(move.score > best) best = move.score;
            if(best > alpha) alpha = best;
            if(best >= beta) break;
        }

        scoredMoves.sort((move1, move2) -> move2.score - move1.score);
        printSearchInfo(scoredMoves.get(0).move, scoredMoves.get(0).score,
                depth);

        moves.clear();
        for(ScoredMove move : scoredMoves) moves.add(move.move);
        return moves;
    }

    /**
     * Run negamax for an increasing depth, sorting the moves after every
     * completed search
     * @param startDepth Start depth
     * @param endDepth Maximum depth
     * @return Best move found
     */
    private Move iterativeDeepening(int startDepth, int endDepth)  {
        this.startTime = System.nanoTime();
        List<Move> moves = getSortedMoves(state);
        if(moves.size() == 1) return moves.get(0);
        for(int i = startDepth; i <= endDepth; i++) {
            try {
                moves = searchMoves(state, moves, i);
            } catch (InterruptedException e) {
                break;
            }
        }
        return moves.get(0);
    }
    
    @Override
    public Move getMove(GameState gameState) {
        // Reset performance counts, clear the hash table
        this.totalNodeCount = 0;
        this.nonLeafCount = 0;
        this.branchesExploredSum = 0;
        moveTable.clear();

        // Create a new internal state object, sync with the game state
        this.state = new State(info.getSize());
        List<Move> moves = gameState.getMoves();
        moves.forEach((move) -> {
            state.makeMove(move);
        });

        // Run a depth increasing search
        Move best = iterativeDeepening(2, 10);
        printPerformanceInfo();
        return best;
    }

    /**
     * Print performance information, including the amount of nodes traversed
     * in the game tree and the nodes traversed per millisecond.
     */
    private void printPerformanceInfo() {
        if(totalNodeCount > 0) {
            long duration = (System.nanoTime() - startTime) / 1000000;
            double nodesPerMs = totalNodeCount / (duration > 0 ? duration : 1);
            double avgBranches = (double) branchesExploredSum / (double)
                    nonLeafCount;
            Logger.getGlobal().log(Level.INFO, "Time: {0}ms", duration);
            Logger.getGlobal().log(Level.INFO, "Nodes: {0}", totalNodeCount);
            Logger.getGlobal().log(Level.INFO, "Nodes/ms: {0}", nodesPerMs);
            Logger.getGlobal().log(Level.INFO, String.format(
                    "Branches explored (avg): %.2f ", avgBranches));
        }
    }
    
    /**
     * Print the result of a search. Includes the best move found, depth
     * searched, and the evaluation score.
     */
    private void printSearchInfo(Move bestMove, int score, int depth) {
        String moveAlgebraic = convertMoveAlgebraic(bestMove.row,
                bestMove.col, info.getSize());
        Logger.getGlobal().log(Level.INFO,
                String.format("Depth: %d, Evaluation: %d, "
                + "Best move: %s", depth, score, moveAlgebraic));
    }

    private class ScoredMove implements Comparable<ScoredMove> {
        public Move move;
        public int score;
        public ScoredMove(Move move, int score) {
            this.move = move;
            this.score = score;
        }

        @Override
        public int compareTo(ScoredMove move) {
            return move.score - this.score;
        }
    }

    private class MoveEntry {
        Move move;
        int depth;

        public MoveEntry(Move move, int depth) {
            this.move = move;
            this.depth = depth;
        }
    }
}