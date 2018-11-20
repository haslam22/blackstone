package players.negamax;

import core.Move;

import java.util.*;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import players.Player;

/**
 * Negamax player, with alpha-beta pruning and further optimisations
 */
public class NegamaxPlayer implements Player {

    private static final Logger LOGGER =
            LogManager.getLogger(NegamaxPlayer.class.getName());

    private long timeNanos;
    private long startTime;

    private int totalNodeCount;
    private int nonLeafCount;
    private int branchesExploredSum;

    private State state;
    private int size;
    private List<Move> moves;

    /**
     * Determines if we need to respond to any threats on the board, if so,
     * we use a reduced set of moves in the search.
     * @param state State to check
     * @return List of defensive threat moves
     */
    private List<Move> getThreatResponses(State state) {
        int playerIndex = state.currentIndex;
        int opponentIndex = state.currentIndex == 2 ? 1 : 2;

        HashSet<Move> fours = new HashSet<>();
        HashSet<Move> threes = new HashSet<>();
        HashSet<Move> refutations = new HashSet<>();

        HashSet<Move> opponentFours = new HashSet<>();
        HashSet<Move> opponentThrees = new HashSet<>();
        HashSet<Move> opponentRefutations = new HashSet<>();

        // Check for threats first and respond to them if they exist
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == opponentIndex) {
                    opponentFours.addAll(ThreatUtils.getFours(state,
                            state.board[i][j], opponentIndex));
                    opponentThrees.addAll(ThreatUtils.getThrees(state,
                            state.board[i][j], opponentIndex));
                    opponentRefutations.addAll(ThreatUtils.getRefutations
                            (state, state.board[i][j], opponentIndex));
                }
                else if(state.board[i][j].index == playerIndex) {
                    fours.addAll(ThreatUtils.getFours(state, state.board[i][j],
                            playerIndex));
                    threes.addAll(ThreatUtils.getThrees(state, state
                            .board[i][j], playerIndex));
                    refutations.addAll(ThreatUtils.getRefutations(state, state
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

        // We have a three that we can play to win.
        // Either we play the three and win, or our opponent has a refutation
        // that leads to their win. So we only consider our three and the
        // opponents refutations.
        if(!threes.isEmpty()) {
            opponentRefutations.addAll(threes);
            return new ArrayList<>(threes);
        }

        // Opponent has a three, defend against it and add refutation moves
        if(!opponentThrees.isEmpty()) {
            opponentThrees.addAll(refutations);
            return new ArrayList<>(opponentThrees);
        }

        return new ArrayList<>();
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

        List<Move> threatResponses = getThreatResponses(state);
        if(!threatResponses.isEmpty()) {
            return threatResponses;
        }

        List<ScoredMove> scoredMoves = new ArrayList<>();

        // Grab closest moves
        List<Move> moves = new ArrayList<>();
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == 0) {
                    if(state.hasAdjacent(i, j, 2)) {
                        int score = Evaluator.evaluateField(state, i, j,
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
        if(Thread.interrupted() || (System.nanoTime() - startTime) > timeNanos) {
            throw new InterruptedException();
        }
        if(depth == 0 || state.terminal() != 0) {
            return Evaluator.evaluateState(state, depth);
        }
        nonLeafCount++;

        int value;
        int best = Integer.MIN_VALUE;
        int countBranches = 0;

        List<Move> moves = getSortedMoves(state);

        for (Move move : moves) {
            countBranches++;
            state.makeMove(move);
            value = -negamax(state, depth - 1, -beta, -alpha);
            state.undoMove(move);
            if(value > best) {
                best = value;
            }
            if(best > alpha) alpha = best;
            if(best >= beta) {
                break;
            }
        }
        branchesExploredSum += countBranches;
        return best;
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
            LOGGER.info("Time: {}ms", duration);
            LOGGER.info( "Nodes: {}", totalNodeCount);
            LOGGER.info("Nodes/ms: {}", nodesPerMs);
            LOGGER.info("Branches explored (avg): {} ",
                    String.format("%.2f", avgBranches));
        }
    }
    
    /**
     * Print the result of a search. Includes the best move found, depth
     * searched, and the evaluation score.
     */
    private void printSearchInfo(Move bestMove, int score, int depth) {
        String moveAlgebraic = bestMove.getAlgebraicString(size);
        LOGGER.info("Depth: {}, Evaluation: {}, Best move: {}", depth, score,
                moveAlgebraic);
    }

    @Override
    public void setupGame(int index, int boardSize, long moveTimeMillis, long gameTimeMillis) {
        this.size = boardSize;
        this.timeNanos = (moveTimeMillis) * 1000000;
        this.moves = new ArrayList<>();
    }

    @Override
    public Move loadBoard(List<Move> orderedMoves, long gameTimeRemainingMillis) {
        this.moves = orderedMoves;
        Move bestMove = getBestMove();
        moves.add(bestMove);
        return bestMove;
    }

    @Override
    public Move getMove(Move opponentsMove, long gameTimeRemainingMillis) {
        moves.add(opponentsMove);
        Move bestMove = getBestMove();
        moves.add(bestMove);
        return bestMove;
    }

    private Move getBestMove() {
        // Reset performance counts, clear the hash table
        this.totalNodeCount = 0;
        this.nonLeafCount = 0;
        this.branchesExploredSum = 0;

        // Create a new internal state object, sync with the game state
        this.state = new State(size);
        moves.forEach((move) -> {
            state.makeMove(move);
        });

        // Run a depth increasing search
        Move best = iterativeDeepening(2, 8);
        printPerformanceInfo();
        return best;
    }

    @Override
    public Move beginGame(long gameTimeRemainingMillis) {
        Move move = new Move(size / 2, size / 2);
        moves.add(move);
        return move;
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
}