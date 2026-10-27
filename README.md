# Kasino

A Java implementation of the card game **Kasino** (Casino), with human and computer players.

## Features

- 2, 3 and 4 player modes (including 2v2 partners)
- 40-card numbered deck
- Capturing, trailing, building, raising , adding cards to builds and standing builds

[//]: # (- Two difficulty levels: with or without pack points)
- Computer (AI) players, with human/computer seats assigned randomly

## Game Modes

| Mode | Format | Deal |
|------|--------|------|
| 2 players | Individual, played in 2 halves | 10 cards each per half |
| 3 players | Individual | 13 cards each, plus 1 on the floor |
| 4 players | Individual | 10 cards each |
| 4 players | Partners (2v2) | 10 cards each |

In 2v2, partners are treated as a single player, but each player can make their own build.

## Scoring

| Item | Points |
|------|--------|
| Each Ace | 1 |
| 2 of Spades | 1 |
| 10 of Diamonds | 2 |
| Most cards in your home (pack points level only) | +5 |

If there is a tie for most cards, nobody gets the +5.

## Rules

### Turn actions
A player may **capture** or **trail** (place a card on the floor), or **build**.

### Builds
- A build is a stack of cards that sum to a value, which you must hold in your hand as a card.
- You cannot make another build, or increase the value of your own build.
- In the 2-player game's first half, a player with a build may not trail; they must capture their build or keep building if possible.

### Raising an opponent's build
- A build can be raised any number of times, e.g. a 9 build can be raised to 10 by a player holding a 10; a 5+2 build can be raised by adding a 2 if you hold a 9.
- A build of 10 cannot be raised.
- Once a player adds another set of cards summing to the built number, the build is **full** and cannot be raised.
- A player who already owns a build cannot raise another player's build.
- When you raise another player's build, it becomes yours, but anyone can still capture it.

### Capturing a build
An opponent can capture a build with a card of the build's value. Captured builds go to the capturing player's **home** (capture pile).

### Difficulty levels
1. **Standard:** no pack points
2. **Pack points:** the player with the most cards in their home at the end gets +5 points

### End of game
The game ends when all players are out of cards. The last player to capture a card or build takes every card left on the floor.

[//]: # (## Getting Started)

[//]: # (```bash)

[//]: # (    )
[//]: # (```)

> Update the repository URL and project paths to match your setup.

## Roadmap

- [x] Rules for 2, 3 and 4 player modes
- [ ] Computer (AI) player
- [ ] Random assignment of human and computer seats

## License

TBD
