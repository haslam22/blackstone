[![Build Status](https://travis-ci.org/haslam22/gomoku.svg?branch=master)](https://travis-ci.org/haslam22/gomoku)

A [**Gomoku**](https://en.wikipedia.org/wiki/Gomoku) (Five in a Row) client featuring a powerful AI written in `Java`. Download the latest build [here](target/gomoku-2.0.jar) (runnable jar).

There are two main components in this project:

* A strong AI player based on Minimax with α-β pruning, alongside many performance optimisations ([players/negamax](src/main/java/players/negamax))
* An interface to setup and create new games, created using JavaFX ([gui](src/main/java/gui))

<p align="center"><img width="80%" src="http://i.imgur.com/XRh8hDB.png" /></p>

## Install
Open in any IDE that supports Maven projects or install [Maven](https://maven.apache.org/download.cgi) yourself and use `mvn install` from the command line in the root of the project to build the project in one step. The output can be found in the target directory.

## Adding your own AI
Adding your own AI is really simple. Follow these steps:

### 1. Create a new Player class
Add a new class which extends [Player](src/main/java/players/Player.java). Look at [RandomPlayer](src/main/java/players/random/RandomPlayer.java) for a simple implementation which only makes a random move on the board:

```
    private Random random;

    /**
     * Create a new player which makes a random move each time.
     * @param info Game information
     */
    public RandomPlayer(GameInfo info) {
        super(info);
        this.random = new Random();
    }

    @Override
    public Move getMove(GameState state) {
        List<Move> moves = state.getAvailableMoves();
        return moves.get(random.nextInt(moves.size()));
    }
```

Each player is instantiated with a GameInfo object telling the player vital game information, e.g. the board size and time limits (per game and per move). Use the constructor to initialize your player and do any initial setup.

When a move is requested, a state object is given to the player containing a few utility functions to determine the current status of the game - moves made, moves available, etc.

### 2. Register your player 

Now register your player name and class in [PlayerRegistry](src/main/java/players/PlayerRegistry.java). PlayerRegistry contains two methods, one for retrieving available players, and one for mapping each player to a class:

```
    public static List<String> getAvailablePlayers() {
        return Arrays.asList(
                "Negamax",
                "Human",
                "Random"
        );
    }

    public static Player getPlayer(GameInfo gameInfo, String playerClassName) {
        switch(playerClassName) {
            case "Negamax":
                return new NegamaxPlayer(gameInfo);
            case "Human":
                return new HumanPlayer(gameInfo);
            case "Random":
                return new RandomPlayer(gameInfo);
            ...
        }
    }
```

Add an entry to `getAvailablePlayers()` and then add another entry to `getPlayer()` mapping the name to the Player class you created.
