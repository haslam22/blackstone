package players.ai;

import core.GameInfo;
import core.GameState;
import core.Move;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
    private State state;

    private final int intersections;
    private final long time;
    private long startTime;
    private int nodes;

    public NegamaxAI(GameInfo info) {
        super(info);
        this.state = new State(info.getIntersections());
        this.threatReducer = new ThreatReducer();
        this.staticEvaluator = new Evaluator();
        this.intersections = info.getIntersections();
        this.time = Math.min((long) 1995 * 1000000, (long) info.getGameTimeout
                () * 1000000);
    }
    
    @Override
    public String toString() {
        return "AI";
    }

    // Compare two moves based on the evaluation of the field
    Comparator<Move> fieldComparator = new Comparator<Move>() {
        @Override
        public int compare(Move move1, Move move2) {
            return staticEvaluator.evaluateField(state.board[move2.getRow()]
                            [move2.getCol()], state.currentIndex) -
                    staticEvaluator.evaluateField(state.board[move1.getRow()]
                            [move1.getCol()], state.currentIndex);
        }
    };
    
    /**
     * Generate a list of moves for a state. Only look at moves close to other
     * stones on the board and reduce moves when threats from the opponent
     * are found.
     *
     * Note: Should avoid calling this for every node. Maybe try killer moves
     * or a hash table move from a previous depth of the same position
     * @param state State to get moves for
     * @return A list of moves
     */
    private List<Move> getMoves(State state) {
        // Board is empty, return a move in the middle of the board
        if(state.moves == 0) {
            List<Move> moves = new ArrayList<>();
            moves.add(new Move(state.board.length / 2, state.board.length / 2));
            return moves;
        }

        // Check if threats exist, reduce moves to threat responses only
        List<Move> threatResponses = threatReducer.reduceMoves(state);
        if(threatResponses != null) return threatResponses;

        // Grab closest moves
        List<Move> moves = new ArrayList<>();
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == 0) {
                    if(state.hasAdjacent(i, j, 2)) {
                        moves.add(new Move(i, j));
                    }
                }
            }
        }

        // Sort based on move evaluation
        moves.sort(fieldComparator);
        return moves;
    }

    /**
     * Run the negamax algorithm for a given node in the game tree
     * @param state Node to searchMoves
     * @param depth Depth to searchMoves to
     * @param alpha Alpha bound
     * @param beta Beta bound
     * @return Score of the node
     * @throws InterruptedException Timeout or interrupted by the user
     */
    private int negamax(State state, int depth, int alpha, int beta)
            throws InterruptedException {
        int value;
        nodes++;
        if(Thread.interrupted() || (System.nanoTime() - startTime) > time) {
            throw new InterruptedException();
        }
        if(depth == 0 || state.terminal() != 0) {
            return staticEvaluator.evaluate(state, depth);
        }
        List<Move> moves = getMoves(state);
        int best = Integer.MIN_VALUE;

        for (Move move : moves) {
            state.makeMove(move);
            value = -negamax(state, depth - 1, -beta, -alpha);
            state.undoMove(move);
            if(value > best) best = value;
            if(best > alpha) alpha = best;
            if(best >= beta) break;
        }
        return best;
    }

    /**
     * Run a depth-limited negamax searchMoves on a set of moves, sorting
     * them by score
     *
     * Note: This is the same as regular negamax, except we take out the
     * first call and hold it inside this function. This way we can save the
     * best move found at the topmost level of the game tree.
     *
     * @param depth Depth to search to
     * @return Original move list, sorted by best score first
     */
    private List<Move> searchMoves(State state, List<Move> moves, int depth)
            throws InterruptedException {
        class ScoredMove {
            public Move move;
            public int score;
            public ScoredMove(Move move, int score) {
                this.move = move;
                this.score = score;
            }
        }

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
        List<Move> moves = getMoves(state);
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
        long duration = (System.nanoTime() - startTime) / 1000000;
        LOGGER.log(Level.INFO, "Time: {0}ms", duration);
        LOGGER.log(Level.INFO, "Nodes: {0}", nodes);
        LOGGER.log(Level.INFO, "Nodes/ms: {0}", 
                nodes / (duration > 0 ? duration : 1));
    }
    
    /**
     * Print the result of a searchMoves, including the best move found, depth
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