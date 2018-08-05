package gui.controllers;

import core.GameController;
import core.Move;
import events.GameEventAdapter;
import events.SettingsListener;
import gui.Controller;
import gui.views.BoardPane;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import java.util.List;

public class BoardPaneController implements Controller {

    private EventHandler<MouseEvent> mouseListener;
    private final BoardPane boardView;
    private GameController game;

    /**
     * Create a new BoardPaneController.
     * @param view Board pane
     */
    public BoardPaneController(BoardPane view) {
        this.boardView = view;
    }

    @Override
    public void initialise(GameController game) {
        this.game = game;
        game.addListener(new GameEventAdapter() {
            EventHandler<MouseEvent> mouseListener;
            @Override
            public void gameStarted() {
                handleGameStarted();
            }
            @Override
            public void moveAdded(int playerIndex, Move move) {
                handleMoveAdded(playerIndex, move);
            }
            @Override
            public void moveRemoved(Move move) {
                handleMoveRemoved(move);
            }
            @Override
            public void gameFinished() {
                handleGameFinished();
            }
            @Override
            public void userMoveRequested(int playerIndex) {
                handleUserMoveRequested(playerIndex);
            }
            @Override
            public void positionLoaded(List<Move> orderedMoves) {
                handlePositionLoaded(orderedMoves);
            }
            @Override
            public void positionCleared() {
                handlePositionCleared();
            }
        });
        game.getSettings().addListener(new SettingsListener() {
            @Override
            public void settingsChanged() {
                handleSettingsChanged();
            }
        });
    }

    /**
     * Handle a position being loaded.
     * @param orderedMoves
     */
    private void handlePositionLoaded(List<Move> orderedMoves) {
        Platform.runLater(() -> boardView.clear());
        int playerIndex = 1;
        for(Move move : orderedMoves) {
            int finalPlayerIndex = playerIndex;
            Platform.runLater(() -> boardView.addStone(
                    finalPlayerIndex, move.row, move.col, false));
            playerIndex = playerIndex == 1 ? 2 : 1;
        }
    }

    /**
     * Handle a position being cleared.
     */
    private void handlePositionCleared() {
        Platform.runLater(() -> boardView.clear());
    }

    /**
     * Handle the settingsChanged() event.
     */
    private void handleSettingsChanged() {
        // Update the board size and clear
        Platform.runLater(() -> boardView.setIntersections(game.getSettings()
                .getSize()));
        Platform.runLater(() -> boardView.clear());
    }

    /**
     * Handle the userMoveRequested() event from the game.
     * @param playerIndex Player index to retrieve move for
     */
    private void handleUserMoveRequested(int playerIndex) {
        // Enable the picker on the board to aid the user when picking a move
        Platform.runLater(() -> boardView.enableStonePicker(playerIndex));
        // Listener submits a move to the game, which can be declined if
        // invalid, if accepted the listener is removed and the picker is
        // disabled
        this.mouseListener = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int row = boardView.getClosestRow(event.getY());
                int col = boardView.getClosestCol(event.getX());
                if(game.setUserMove(new Move(row, col))) {
                    boardView.removeEventHandler(MouseEvent
                            .MOUSE_CLICKED, this);
                    Platform.runLater(() -> boardView.disableStonePicker());
                }
            }
        };
        boardView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                mouseListener);
    }

    /**
     * Handle the gameFinished() event from the game.
     */
    private void handleGameFinished() {
        if(mouseListener != null) {
            boardView.removeEventHandler(MouseEvent.MOUSE_CLICKED,
                    mouseListener);
        }
        Platform.runLater(() -> boardView.disableStonePicker());
    }

    /**
     * Handle the moveAdded() event from the game.
     * @param playerIndex Player identifier
     * @param move Move added to the state
     */
    private void handleMoveAdded(int playerIndex, Move move) {
        Platform.runLater(() -> boardView.addStone(playerIndex, move.row,
                move.col, false));
    }

    /**
     * Handle the moveRemoved() event from the game.
     * @param move Move removed from the state
     */
    private void handleMoveRemoved(Move move) {
        Platform.runLater(() -> boardView.removeStone(move.row, move.col));
        Platform.runLater(() -> boardView.disableStonePicker());
    }

    /**
     * Handle the gameStarted() event from the game instance.
     */
    private void handleGameStarted() {
        // Clear the board in case of a previous game
        Platform.runLater(() -> boardView.clear());
    }

}
