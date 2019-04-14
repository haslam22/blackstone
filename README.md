# blackstone [![Build Status](https://travis-ci.org/haslam22/blackstone.svg?branch=master)](https://travis-ci.org/haslam22/blackstone)

A [**Gomoku**](https://en.wikipedia.org/wiki/Gomoku) (Five in a Row) game manager featuring a powerful AI written in `Java`. Download the latest release [**here**](https://github.com/haslam22/gomoku/releases) (runnable jar). **Compatible with Java 12 +**.

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

## Known limitations of AI performance
- Position evaluation is slow. The evaluation is computed in real time, when it could be computed using a lookup table. This lookup table also has the potential to be very small (around 256x256 entries). Similarly, threat calculation can be achieved via the lookup table. This would decrease the amount of computation performed per position - leading to a much higher number of positions evaluated per second. There's a branch in progress for improving both position evaluation and threat calculation [here](https://github.com/haslam22/blackstone/tree/feature/fast-pattern-lookup).
- No detection of double threats. Double threats are basically a win, and this would reduce the prevalence of a [Horizon effect](https://en.wikipedia.org/wiki/Horizon_effect) where we can't see a win/loss just over our depth limit.
- No transposition table. This could be used to cut off large subtrees in the alpha-beta search.
- No VCT (Victory by Continuous Threats) search. See academic paper [here](https://pdfs.semanticscholar.org/f476/00662cadb0975f9cfd7867389efedda6f873.pdf) describing this search algorithm that works in the space of threats. Can be very powerful in determining a win/loss in a given scenario.
