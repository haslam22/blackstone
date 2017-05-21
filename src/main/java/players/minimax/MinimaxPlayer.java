package players.minimax;

import gomoku.GomokuGame;
import gomoku.GomokuMove;
import gomoku.GomokuState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import players.GomokuPlayer;

/**
 * Minimax player, with alpha-beta pruning.
 * @author Hassan
 */
public class MinimaxPlayer extends GomokuPlayer {

    private final MinimaxThreatReducer threatReducer;
    private final MinimaxEvaluator staticEvaluator;
    
    private final int intersections;
    private MinimaxState state;
    private final int time;
    
    private long startTime;
    private int nodes;
    
    public MinimaxPlayer(GomokuGame game, int playerIndex, int opponentIndex, 
            int time) {
        super(game, playerIndex, opponentIndex);
        this.state = new MinimaxState(game.getIntersections());
        this.threatReducer = new MinimaxThreatReducer();
        this.staticEvaluator = new MinimaxEvaluator();
        this.intersections = game.getIntersections();
        this.time = time;
    }
    
    @Override
    public String toString() {
        return "Minimax";
    }

    // Compare two moves based on the evaluation of the field
    Comparator<GomokuMove> fieldComparator = new Comparator<GomokuMove>() {
        @Override
        public int compare(GomokuMove move1, GomokuMove move2) {
            return staticEvaluator.evaluateField(state.board[move2.row]
                    [move2.col], state.currentIndex) - 
                    staticEvaluator.evaluateField(state.board[move1.row]
                            [move1.col], state.currentIndex);
        }
    };
    
    /**
     * Generate a list of moves for a state. Only returns moves near to other
     * existing stones and reduces the search space when threats are found.
     * @param state State to get moves for
     * @return A list of moves
     */
    private List<GomokuMove> getMoves(MinimaxState state) {
        // Board is empty, return a move in the middle of the board
        if(state.moves == 0) {
            List<GomokuMove> moves = new ArrayList<>();
            moves.add(new GomokuMove(
                    state.board.length / 2, state.board.length / 2));
            return moves;
        }
        List<GomokuMove> reducedMoves = threatReducer.reduceMoves(state);
        if(reducedMoves != null) {
            return reducedMoves;
        }
        
        // Prune moves by only focusing on moves that are within 2 intersections
        // of an occupied field
        ArrayList<GomokuMove> pruned = new ArrayList<>(225);
        
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == 0 && 
                        state.hasAdjacent(i, j, 2)) {
                    pruned.add(new GomokuMove(i, j));
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
            List<GomokuMove> moves = getMoves(state);
            for(GomokuMove move : moves) {
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
            List<GomokuMove> prunedMoves = getMoves(state);
            for(GomokuMove move : prunedMoves) {
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
    private List<GomokuMove> search(List<GomokuMove> moves, int depth) 
            throws InterruptedException {
        
        // Helper class to associate a move with a minimax score
        class ScoredMove {
            GomokuMove move;
            int score;
            ScoredMove(GomokuMove move, int score) {
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
                state.makeMove(move.move);
                move.score = minimax(state, depth - 1, alpha, beta);
                state.undoMove(move.move);
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
    public GomokuMove getMove(GomokuState gameState) {
        // Create a new internal state object, sync with the game state
        this.state = new MinimaxState(game.getIntersections());
        ArrayList<GomokuMove> moves = new ArrayList(gameState.getMoveHistory());
        moves.forEach((move) -> {
            state.makeMove(move);
        });
        
        this.nodes = 0;
        this.startTime = System.currentTimeMillis();
        
        // Return the initial moves from this state
        List<GomokuMove> initialMoves = getMoves(state);
        
        // Only one move available, return it
        if(initialMoves.size() == 1) return initialMoves.get(0);
        
        GomokuMove bestMove = new GomokuMove();
        
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
        game.writeLog("Time: " + duration + "ms");
        game.writeLog("Nodes: " + nodes);
        game.writeLog("Nodes/ms: " + (nodes / (duration > 0 ? duration : 1)));
    }
    
    /**
     * Print the result of a search, including the best move found, depth
     * searched, and the evaluation score.
     */
    private void printSearchInfo(GomokuMove bestMove, int score, int depth) {
        String bestMoveString = "[" + convertCol(bestMove.col) 
                + convertRow(bestMove.row) + "]";
        
        game.writeLog(String.format("Depth: %d, Evaluation: %d, Best move: %s",
                depth, score, bestMoveString));
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