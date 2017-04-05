# Gomoku
A powerful Gomoku (Renju, Pente, Five in a Row) AI written in Java, using the Minimax algorithm with alpha-beta pruning and a strong heuristic evaluation function.

Latest build (runnable jar): [dist/Gomoku.jar](dist/Gomoku.jar)

![alt text](http://i.imgur.com/zI2FdPu.png)

## Current Features
- Minimax AI (up to depth 8) with alpha-beta pruning, simple move ordering and evaluation caching
- Fully resizable Gomoku board interface, with interactive stone picking and board customisation

## Coming Soon
- Transposition table to save subtree evaluations
- Faster evaluation by precomputing heuristic scores
- Undo/Redo controls for the interface
