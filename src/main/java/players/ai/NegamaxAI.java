package players.ai;

import core.GameInfo;
import core.GameState;
import core.Move;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import players.Player;

/**
 * Negamax player, with alpha-beta pruning and further optimisations
 * @author Hasan
 */
public class NegamaxAI extends Player {
    
    private static final Logger LOGGER = Logger.getGlobal();

    private final ThreatReducer threatReducer;
    private final Evaluator staticEvaluator;
    private final LRUCache<Long, MoveEntry> moveTable;
    private State state;

    private final int intersections;
    private final long time;
    private long startTime;
    private int nodes;
    private int averageCutoff;
    private int hashMoveCutoff;
    private int fullNodes;

    public NegamaxAI(GameInfo info) {
        super(info);
        this.state = new State(info.getIntersections());
        this.threatReducer = new ThreatReducer();
        this.staticEvaluator = new Evaluator();
        this.intersections = info.getIntersections();
        this.time = (long) 2000 * 1000000;
        this.moveTable = new LRUCache<>(200000);
    }

    private class ScoredMove {
        public Move move;
        public int score;
        public ScoredMove(Move move, int score) {
            this.move = move;
            this.score = score;
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
    
    @Override
    public String toString() {
        return "AI";
    }

    /**
     * Generate a list of sorted and pruned moves for this state. Moves are
     * pruned when they are too far away from existing stones, and also when
     * threats are found which require an immediate response. Moves are
     * sorted using an evaluation applied to a single field
     * @see Evaluator
     * @param state State to get moves for
     * @return A list of moves, sorted and pruned
     */
    private List<Move> getSortedMoves(State state) {
        // Board is empty, return a move in the middle of the board
        if(state.moves == 0) {
            List<Move> moves = new ArrayList<>();
            moves.add(new Move(state.board.length / 2, state.board.length / 2));
            return moves;
        }

        // Check if threats exist, reduce moves to threat responses only
        List<Move> threatResponses = threatReducer.reduceMoves(state);
        if(threatResponses != null) return threatResponses;

        List<ScoredMove> scoredMoves = new ArrayList<>();

        MoveEntry entry = moveTable.get(state.getZobristHash());
        // Grab closest moves
        List<Move> moves = new ArrayList<>();
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                // Ignore hash move
                if(entry != null &&
                        (i == entry.move.getRow() && j == entry.move.getCol())) {
                    continue;
                }
                if(state.board[i][j].index == 0) {
                    if(state.hasAdjacent(i, j, 2)) {
                        int score = staticEvaluator.evaluateField(state
                                .board[i][j], state.currentIndex);
                        scoredMoves.add(new ScoredMove(new Move(i, j), score));
                    }
                }
            }
        }

        // Sort based on move evaluation
        scoredMoves.sort((move1, move2) -> move2.score - move1.score);
        for(ScoredMove move : scoredMoves) {
            moves.add(move.move);
        }
        return moves;
    }

    /**
     * Run the negamax algorithm for a given node in the game tree
     * @param state Node to search
     * @param depth Depth to search to
     * @param alpha Alpha bound
     * @param beta Beta bound
     * @return Score of the node
     * @throws InterruptedException Timeout or interrupted by the user
     */
    private int negamax(State state, int depth, int alpha, int beta)
            throws InterruptedException {
        fullNodes++;
        if(Thread.interrupted() || (System.nanoTime() - startTime) > time) {
            throw new InterruptedException();
        }
        if(depth == 0 || state.terminal() != 0) {
            return staticEvaluator.evaluate(state, depth);
        }
        nodes++;

        int value;
        int best = Integer.MIN_VALUE;
        int count = 0;

        Move bestMove = new Move();

        // Try the move from a previous search
        MoveEntry hashMoveEntry = moveTable.get(state.getZobristHash());
        if (hashMoveEntry != null) {
            state.makeMove(hashMoveEntry.move);
            value = -negamax(state, depth - 1, -beta, -alpha);
            state.undoMove(hashMoveEntry.move);
            if (value > best) {
                bestMove = hashMoveEntry.move;
                best = value;
            }
            if (best > alpha) alpha = best;
            if (best >= beta) {
                hashMoveCutoff++;
                return best;
            }
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
        averageCutoff += count;
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
            return;
        } else {
            if(depth > moveEntry.depth) {
                moveTable.put(key, new MoveEntry(move, depth));
            }
        }
    }

    /**
     * Run a depth-limited negamax search on a set of moves, sorting them by
     * score
     *
     * Note: This is the same as regular negamax, except we take out the
     * first call and hold it inside this function. This way we can save the
     * best move found at the topmost level of the game tree.
     *
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
    public Move iterativeDeepening(int startDepth, int endDepth)  {
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
        this.nodes = 0;
        this.fullNodes = 0;
        this.averageCutoff = 0;
        this.hashMoveCutoff = 0;
        System.out.println(moveTable.size());
        moveTable.clear();
        // Create a new internal state object, sync with the game state
        this.state = new State(info.getIntersections());
        List<Move> moves = gameState.getMoves();
        moves.forEach((move) -> {
            state.makeMove(move);
        });
        Move best = iterativeDeepening(2, 10);
        printPerformanceInfo();
        return best;
    }
    
    /**
     * Print performance information, including the amount of nodes traversed
     * in the game tree and the nodes traversed per millisecond.
     */
    private void printPerformanceInfo() {
        if(nodes > 0) {
            long duration = (System.nanoTime() - startTime) / 1000000;
            LOGGER.log(Level.INFO, "Time: {0}ms", duration);
            LOGGER.log(Level.INFO, "Nodes: {0}", fullNodes);
            LOGGER.log(Level.INFO, "Nodes/ms: {0}",
                    fullNodes / (duration > 0 ? duration : 1));
            LOGGER.log(Level.INFO, String.format("Branches explored (avg): %.2f ",
                    (double) averageCutoff / (double) nodes));
            LOGGER.log(Level.INFO, "Non-leaf nodes: " + nodes);
            LOGGER.log(Level.INFO, "Hash move cut-offs: " + hashMoveCutoff);
        }
    }
    
    /**
     * Print the result of a search, including the best move found, depth
     * searched, and the evaluation score.
     */
    private void printSearchInfo(Move bestMove, int score, int depth) {
        String bestMoveString = "[" + convertCol(bestMove.getCol())
                + convertRow(bestMove.getRow()) + "]";
        LOGGER.log(Level.INFO, String.format("Depth: %d, Evaluation: %d, "
                + "Best move: %s", depth, score, bestMoveString));
    }
    
    /**
     * Convert a board row to its board representation (15, 14, 13, 12..)
     * @param row
     * @return
     */
    private int convertRow(int row) {
        return this.intersections - row;
    }
    
    /**
     * Convert a board column to its board representation (A, B, C, D...)
     * @param col
     * @return
     */
    private String convertCol(int col) {
        return String.valueOf((char)((col + 1) + 'A' - 1));
    }
}