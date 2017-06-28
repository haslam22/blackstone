[![Build Status](https://travis-ci.org/haslam22/gomoku.svg?branch=master)](https://travis-ci.org/haslam22/gomoku)

A [**Gomoku**](https://en.wikipedia.org/wiki/Gomoku) (Five in a Row) client featuring a powerful AI written in `Java`. Download the latest build [here](target/gomoku-2.0.jar).

There are two main components in this project:

* A strong AI player based on Minimax with α-β pruning, alongside many performance optimisations ([/players/Minimax](src/main/java/players/minimax))
* An interface to setup and create new games, created using JavaFX ([/gui](src/main/java/gui))

<p align="center"><img src="http://i.imgur.com/XRh8hDB.png" width="800" height="600" /></p>

## Install
Open in any IDE that supports Maven projects or install [Maven](https://maven.apache.org/download.cgi) and use `mvn package` to build the project in one step.
