# Iskyjo

A single-player (soon multiplayer) card game built in Java with JavaFX, loosely inspired by Skyjo and Balatro. The goal each round is to score enough points with your deck before time runs out, using jokers you collect from a shop between rounds.

> Work in progress. Core gameplay loop is functional; multiplayer and additional jokers are planned. asdly we won't be able to add all them before the project's deadline.

## How it works

Each round, cards are dealt face-down into a personal grid. On your turn you draw from either the draw pile or the discard pile, then choose to replace one of your cards or flip a hidden one and throw the drawn card away. Full columns of matching values are cleared automatically.

At the end of a round, all remaining card values are summed, combos (matching streaks across rows, columns and diagonals) are scored, and your jokers apply their bonuses on top. If the combined score beats the round quota you move on to the shop, otherwise the game ends.

## Project structure

```
iskyjo/src/main/java/com/neuilleprime/
  Main.java                  - remanent of the old CLI version
  game/                      - core game logic (cards, deck, piles, players, controller)
    actions/                 - command pattern for all player actions
    events/                  - event types dispatched by GameController
  jokers/                    - joker base class and all joker implementations
  gui/
    main/                    - JavaFX entry point (MainGui)
    components/              - reusable UI components (CardView, DeckView, JokerView, ...)
    screens/                 - one class per screen (Menu, Game, Result, Shop, ...)
    utils/                   - shared helpers (AssetLoader, ScreenManager, SideBarsHelper)
```

## Getting started

**Requirements:** Java 21+, Maven, JavaFX 21

```bash
cd iskyjo
mvn javafx:run
```

## Jokers

Jokers are passive items bought in the shop between rounds. They come in five rarities (Common, Uncommon, Rare, Epic, Legendary) and belong to one of three categories:

- **DECK** - applied to every card in your grid before scoring
- **COMBO** - applied to detected streaks during scoring
- **CARD** - applied to a single card

Current jokers:

| Name | Rarity | Effect |
|---|---|---|
| Add X Card | Common | Adds X to one card's value |
| Add X Deck | Uncommon | Adds X to every card in the deck |
| Combo Left | Common | Adds X to the streak multiplier of combos of a given size |
| Combo Right | Common | Adds X to the card value of combos matching a given value |
| Combo Left All | Uncommon | Adds X to the streak multiplier of all combos |

## Credits

- Code by [Enrtarr](https://github.com/Enrtarr)
- Assets by [maaple](https://github.com/maapleuh)
