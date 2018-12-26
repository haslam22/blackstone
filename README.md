# Blackstone [![Build Status](https://travis-ci.org/haslam22/gomoku.svg?branch=master)](https://travis-ci.org/haslam22/gomoku)

A [**Gomoku**](https://en.wikipedia.org/wiki/Gomoku) (Five in a Row) game manager featuring a powerful AI written in `Java`. Download the latest release [**here**](https://github.com/haslam22/gomoku/releases) (runnable jar).

There are two main components in this project:

* A strong AI player based on Minimax with α-β pruning, alongside many performance optimisations ([haslam.blackstone.players/negamax](src/main/java/haslam/blackstone/players/negamax))
* An easy to use game manager with a minimal GUI created using JavaFX (see below for features) ([haslam.blackstone.gui](src/main/java/haslam/blackstone/gui))

<p align="center"><img width="80%" src="http://i.imgur.com/XRh8hDB.png" /></p>

## Features
- Loading of external AI's supporting the [Piskvork protocol](https://github.com/haslam22/gomoku/wiki/Piskvork-Gomocup-Protcol) ([Download page](http://gomocup.org/download-gomoku-ai/))
- Freestyle Gomoku games against the built-in Negamax AI
- Beautiful, fully resizable and flexible Gomoku board, supporting high DPI displays
- Configurable game settings including time per move, time per game and board size
- Easy saving and loading of positions with move order maintained

## Install
Clone the project and open in any IDE that supports Maven projects or install [Maven](https://maven.apache.org/download.cgi) directly and use `mvn clean install` from the command line in the root directory to build the project in one step.
