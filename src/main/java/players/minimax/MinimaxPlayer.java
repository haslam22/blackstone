package players.minimax;

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
 * Minimax player, with alpha-beta pruning.
 * @author Hassan
 */
public class MinimaxPlayer extends Player {
    
    private static final Logger LOGGER = Logger.getGlobal();

    private final MinimaxThreatReducer threatReducer;
    private final MinimaxEvaluator staticEvaluator;
    private MinimaxState minimaxState;

    private final int opponentIndex;
    private final int playerIndex;
    private final int intersections;
    private final int time;
    
    private long startTime;
    private int nodes;
    
    public MinimaxPlayer(GameInfo info) {
        super(info);
        this.minimaxState = new MinimaxState(info.getIntersections());
        this.threatReducer = new MinimaxThreatReducer();
        this.staticEvaluator = new MinimaxEvaluator();
        this.intersections = info.getIntersections();
        this.time = Math.min(2000, info.getMoveTimeout());
        this.opponentIndex = info.getOpponentIndex();
        this.playerIndex = info.getPlayerIndex();
    }
    
    @Override
    public String toString() {
        return "Minimax";
    }

    // Compare two moves based on the evaluation of the field
    Comparator<Move> fieldComparator = new Comparator<Move>() {
        @Override
        public int compare(Move move1, Move move2) {
            return staticEvaluator.evaluateField(minimaxState.board[move2.getRow()]
                    [move2.getCol()], minimaxState.currentIndex) -
                    staticEvaluator.evaluateField(minimaxState.board[move1.getRow()]
                            [move1.getCol()], minimaxState.currentIndex);
        }
    };
    
    /**
     * Generate a list of moves for a state. Only look at moves close to other
     * stones on the board and reduce moves when threats from the opponent
     * are found.
     * @param state State to get moves for
     * @return A list of moves
     */
    private List<Move> getMoves(MinimaxState state) {
        // Board is empty, return a move in the middle of the board
        if(state.moves == 0) {
            List<Move> moves = new ArrayList<>();
            moves.add(new Move(state.board.length / 2, state.board.length / 2));
            return moves;
        }
        
        // Search for threats, return reduced set of moves if threats from the
        // opponent exist
        List<Move> reducedMoves = threatReducer.reduceMoves(state);
        if(reducedMoves != null) {
            return reducedMoves;
        }
        
        // Prune moves by only focusing on moves close to existing stones
        ArrayList<Move> pruned = new ArrayList<>(225);
        // Add moves adjacent to other stones (max distance of 2)
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == 0 && 
                        state.hasAdjacent(i, j, 2)) {
                    pruned.add(new Move(i, j));
                }
            }
        }
        
        // Sort the moves using a heuristic (static evaluator applied to a 
        // single move)
        pruned.sort(fieldComparator);
        return pruned;
    }
    
    /**
     * Run the minimax algorithm up to a certain depth, with alpha-beta
     * pruning.
     * @param state Starting state
     * @param depth How deep to search the tree before evaluating
     * @param alpha Best possible value for the maximising player so far
     * @param beta Best possible value for the minimising player so far
     * @return
     */
    private int minimax(MinimaxState state, int depth, int alpha, int beta) 
            throws InterruptedException {
        if(Thread.interrupted() || 
                (System.currentTimeMillis() - startTime) > time) {
            throw new InterruptedException();
        }
        nodes++;
        // Check if terminal node, return loss/win based on depth
        int terminal = state.terminal();
        if(terminal == this.playerIndex) {
            return 10000 + depth;
        }
        else if(terminal == this.opponentIndex) {
            return -10000 - depth;
        }
        // Reached leaf node, return evaluation
        if(depth == 0) {
            return staticEvaluator.evaluate(state, playerIndex, opponentIndex);
        }
        // Not terminal or leaf node, continue recursing
        if(state.currentIndex == this.playerIndex) {
            int maximum = Integer.MIN_VALUE;
            List<Move> moves = getMoves(state);
            for(Move move : moves) {
                state.makeMove(move);
                int score = minimax(state, depth - 1, alpha, beta);
                state.undoMove(move);
                if(score > maximum) {
                    maximum = score;
                }
                alpha = Math.max(alpha, maximum);
                if(beta <= alpha) {
                    break;
                }
            }
            return maximum;
        }
        else {
            int minimum = Integer.MAX_VALUE;
            List<Move> prunedMoves = getMoves(state);
            for(Move move : prunedMoves) {
                state.makeMove(move);
                int score = minimax(state, depth - 1, alpha, beta);
                state.undoMove(move);
                if(score < minimum) {
                    minimum = score;
                }
                beta = Math.min(beta, minimum);
                if(beta <= alpha) {
                    break;
                }
            }
            return minimum;
        }
    }
    
    /**
     * Run minimax for a list of initial moves and sort them after the search
     * based on their scores.
     * @param moves Initial moves
     * @param depth Depth to search moves
     * @return List of moves sorted by highest score first
     */
    private List<Move> search(List<Move> moves, int depth)
            throws InterruptedException {
        
        // Helper class to associate a move with a minimax score
        class ScoredMove {
            Move move;
            int score;
            ScoredMove(Move move, int score) {
                this.move = move;
                this.score = score;
            }
        }
        
        // For each initial move, create a ScoredMove with an initial score of
        // -infinity. These moves and their scores get updated after every 
        // minimax search and are sorted before being searched again
        List<ScoredMove> scoredMoves = new ArrayList<>();
        moves.forEach((move) -> {
            scoredMoves.add(new ScoredMove(move, Integer.MIN_VALUE));
        });
        
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int bestScore = alpha;

        // Run minimax for all the initial moves
        try {
            for(ScoredMove move : scoredMoves) {
                minimaxState.makeMove(move.move);
                move.score = minimax(minimaxState, depth - 1, alpha, beta);
                minimaxState.undoMove(move.move);
                if(move.score > bestScore) {
                    bestScore = move.score;
                }
                alpha = Math.max(alpha, bestScore);
                if(beta <= alpha) break;
            }
        } catch(InterruptedException ex) {
            // Search was interrupted
            throw(ex);
        }
        
        scoredMoves.sort(new Comparator<ScoredMove>() {
            @Override
            public int compare(ScoredMove move1, ScoredMove move2) {
                return move2.score - move1.score;
            }
        });
        
        moves.clear();
        
        // Found a winning move, return it and ignore anything else
        if(scoredMoves.get(0).score >= 10000) {
            moves.add(scoredMoves.get(0).move);
            printSearchInfo(moves.get(0), scoredMoves.get(0).score, depth);
            return moves;
        } else {
            // Else add all the moves, sorted by best score first
            for(int i = 0; i < scoredMoves.size(); i++) {
                moves.add(scoredMoves.get(i).move);
            }
        }
        printSearchInfo(moves.get(0), scoredMoves.get(0).score, depth);
        return moves;
    }
    
    @Override
    public Move getMove(GameState gameState) {
        // Create a new internal state object, sync with the game state
        this.minimaxState = new MinimaxState(info.getIntersections());
        List<Move> moves = gameState.getMoves();
        moves.forEach((move) -> {
            minimaxState.makeMove(move);
        });
        
        this.nodes = 0;
        this.startTime = System.currentTimeMillis();
        
        // Return the initial moves from this state
        List<Move> initialMoves = getMoves(minimaxState);
        
        // Only one move available, return it
        if(initialMoves.size() == 1) return initialMoves.get(0);

        Move bestMove = new Move(0, 0);
        
        // Run a minimax search up to a maximum depth of 10 for all the moves
        int depth = 10;
        for(int i = 2; i <= depth; i++) {
            try {
                // Search and sort moves based on score
                initialMoves = search(initialMoves, i);
                // If the size is 1, must be a winning move, return it
                if(initialMoves.size() == 1) {
                    bestMove = initialMoves.get(0);
                    break;
                }
            } catch (InterruptedException ex) {
                break;
            }
            // Update the best move
            bestMove = initialMoves.get(0);
        }
        
        printPerformanceInfo();
        return bestMove;
    }
    
    /**
     * Print performance information, including the amount of nodes traversed
     * in the game tree and the nodes traversed per millisecond.
     */
    private void printPerformanceInfo() {
        long duration = System.currentTimeMillis() - startTime;
        LOGGER.log(Level.INFO, "Time: {0}ms", duration);
        LOGGER.log(Level.INFO, "Nodes: {0}", nodes);
        LOGGER.log(Level.INFO, "Nodes/ms: {0}", 
                nodes / (duration > 0 ? duration : 1));
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