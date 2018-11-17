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
- Loading of external AI's supporting the Piskvork protocol ([Download page](http://gomocup.org/download-gomoku-ai/))

## Install
Clone the project and open in any IDE that supports Maven projects or install [Maven](https://maven.apache.org/download.cgi) directly and use `mvn install` from the command line in the root directory to build the project in one step.

## Creating your own AI
### Piskvork Protocol (Recommended)
This project supports loading AI programs which implement the Piskvork protocol. The AI can be programmed in any language of your choice, as long as it responds to commands outlined in the [Piskvork Protocol document](http://petr.lastovicka.sweb.cz/protocl2en.htm) created by Petr Laštovička. The advantage of using this protocol is that you can write the AI in any language of your choice, and also use it in other game managers e.g. the [Piskvork game manager](http://gomocup.org/download-gomocup-manager/), or enter it in tournaments such as [Gomocup](http://gomocup.org/). Communication occurs through standard input/output streams.

### Internal Java interface
If you want, you can implement your AI without having to deal with standard input/output, although the drawback is that you can only use it in this project. Have a look at the [Player](src/main/java/players/Player.java) interface and create your own implementation. Add an entry to [PlayerRegistry](src/main/java/players/PlayerRegistry.java) to make this player available in the GUI.
