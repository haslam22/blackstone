[![Build Status](https://travis-ci.org/haslam22/gomoku.svg?branch=master)](https://travis-ci.org/haslam22/gomoku)

A [**Gomoku**](https://en.wikipedia.org/wiki/Gomoku) (Five in a Row) client featuring a powerful AI written in `Java`. Download the latest release [**here**](https://github.com/haslam22/gomoku/releases) (runnable jar).

There are two main components in this project:

* A strong AI player based on Minimax with α-β pruning, alongside many performance optimisations ([players/negamax](src/main/java/players/negamax))
* An easy to use minimal GUI created using JavaFX (see below for features) ([gui](src/main/java/gui))

<p align="center"><img width="80%" src="http://i.imgur.com/XRh8hDB.png" /></p>

## Features
- Freestyle Gomoku games against the built-in Negamax AI
- Beautiful, fully resizable and flexible Gomoku board, supporting high DPI displays, easy stone placement, etc
- Configurable game settings including time per move, time per game and board size
- Easy saving and loading of positions with move order maintained

## Install
Clone the project and open in any IDE that supports Maven projects or install [Maven](https://maven.apache.org/download.cgi) directly and use `mvn install` from the command line in the root directory to build the project in one step.

## Adding your own AI
Follow these [steps](https://github.com/haslam22/gomoku/wiki/Adding-your-own-AI). External engines using the Piskvork protcol are not currently supported (this is coming soon). 
