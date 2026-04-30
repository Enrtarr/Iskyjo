package com.neuilleprime.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.neuilleprime.jokers.*;
import com.neuilleprime.jokers.Joker.JokerCategory;
import com.neuilleprime.game.actions.Action;
import com.neuilleprime.game.events.GameEventListener;
import com.neuilleprime.game.events.JokerSoldEvent;
import com.neuilleprime.game.events.RoundEndedEvent;
import com.neuilleprime.game.events.ShopRerolledEvent;
import com.neuilleprime.game.events.TurnStartedEvent;

/**
 * Central controller that manages the entire game lifecycle.
 * <p>
 * Responsibilities include:
 * <ul>
 *   <li>Maintaining the turn order and advancing through players</li>
 *   <li>Managing the draw and discard piles</li>
 *   <li>Triggering round-end evaluation (scoring, joker effects, shop reroll)</li>
 *   <li>Dispatching game events to registered {@link GameEventListener}s</li>
 *   <li>Delegating shop operations (buy, sell, reroll) to the {@link Shop}</li>
 * </ul>
 * </p>
 */
public class GameController {

    /**
     * Score thresholds that must be beaten, one per round (round 1 = index 0).
     */
    private static final int[] scorePerRounds = new int[] {
        20,
        50,
        70,
        120,
        200,
        300,
        500,
        700,
        1000,
        1500,
        2200,
        3000,
        4500,
        6900,
        10000
    };

    /** List of all players in the game. */
    private ArrayList<Player> players;

    /** The pile from which players draw cards. */
    private Pile drawPile;

    /** The pile onto which discarded cards are placed. */
    private Pile discardPile;

    /** Index into {@link #players} of the player whose turn it currently is. */
    private int currentPlayerIndex;

    /** Index into {@link #players} of the player who started the current round. */
    private int startingPlayerIndex;

    /** Current round number (1-based). */
    private int round;

    /** Whether the current round is in its ending phase. */
    private boolean isRoundEnding;

    /** Number of players who have pressed "ready" at the shop screen. */
    private int nbrPlayersReady;

    /** Score that all players combined must reach to survive this round. */
    private int roundScore;

    /** Amount of money awarded to a player for beating their share of the quota. */
    private int moneyPerRound;

    /** The last card drawn by a player (used to pass it back to the GUI). */
    private Card lastDrawnCard;

    /**
     * Current game state code.
     * <ul>
     *   <li>0 = loss (game over)</li>
     *   <li>1 = next turn</li>
     *   <li>2 = next round (open shop)</li>
     * </ul>
     */
    private int gameState;

    /** The shop instance used for buying, selling, and rerolling jokers. */
    private Shop shop = new Shop();

    /**
     * Constructs a new {@code GameController} with the given players and card count.
     *
     * @param players    the list of players participating in the game
     * @param nbrOfCards total number of cards to populate the draw pile with
     */
    public GameController(ArrayList<Player> players, int nbrOfCards) {
        this.players = players;
        this.drawPile = new Pile(nbrOfCards);
        this.discardPile = new Pile(0);
        this.currentPlayerIndex = 0;
        this.startingPlayerIndex = 0;
        this.round = 1;
        this.isRoundEnding = true;
        this.nbrPlayersReady = this.players.size();
        this.roundScore = scorePerRounds[this.round-1];
        this.moneyPerRound = 3;
        this.lastDrawnCard = null;
        this.gameState = 1;
    }

    /**
     * Executes the given {@link Action} against this controller.
     * This is the primary client-to-server communication entry point.
     *
     * @param action the action to execute
     */
    public void execute(Action action) {
        action.execute(this);
    }

    /** Map from player to their list of registered event listeners. */
    private Map<Player, List<GameEventListener>> listeners = new HashMap<>();

    /**
     * Registers a {@link GameEventListener} for the specified player.
     * Multiple listeners can be registered per player.
     *
     * @param player   the player to register the listener for
     * @param listener the listener to add
     */
    public void addListener(Player player, GameEventListener listener) {
        listeners.computeIfAbsent(player, p -> new ArrayList<>()).add(listener);
    }

    /**
     * Notifies all listeners of the given player with the provided event.
     *
     * @param player the player whose listeners should be notified
     * @param event  a consumer that invokes the appropriate callback on each listener
     */
    private void notifyPlayer(Player player, Consumer<GameEventListener> event) {
        List<GameEventListener> playerListeners = listeners.get(player);
        if (playerListeners != null) playerListeners.forEach(event::accept);
    }

    /**
     * Notifies all registered listeners across all players with the provided event.
     *
     * @param event a consumer that invokes the appropriate callback on each listener
     */
    private void notifyAll(Consumer<GameEventListener> event) {
        listeners.values().forEach(list -> list.forEach(event::accept));
    }

    /**
     * Returns the player whose turn it currently is.
     *
     * @return the current {@link Player}
     */
    public Player getCurrentPlayer() {
        return this.players.get(this.currentPlayerIndex);
    }

    /**
     * Returns the total number of players in the game.
     *
     * @return player count
     */
    public int getPlayerCount() {
        return this.players.size();
    }

    /**
     * Returns the top card of the draw pile without removing it, or {@code null}
     * if the pile is empty.
     *
     * @return top card of the draw pile, or {@code null}
     */
    public Card getDrawPileTop() {
        if (this.drawPile.getAllCards().size() > 0) {
            return this.drawPile.getAllCards().getLast();
        }
        else {
            return null;
        }
    }

    /**
     * Returns the top card of the discard pile without removing it, or {@code null}
     * if the pile is empty.
     *
     * @return top card of the discard pile, or {@code null}
     */
    public Card getDiscardPileTop() {
        if (this.discardPile.getAllCards().size() > 0) {
            return this.discardPile.getAllCards().getLast();
        }
        else {
            return null;
        }
    }

    /**
     * Returns both the draw and discard piles as an array.
     *
     * @return array of {@code [drawPile, discardPile]}
     */
    public Pile[] getBothPiles() {
        return new Pile[] {this.drawPile, this.discardPile};
    }

    /**
     * Returns the top cards of both piles as an array.
     *
     * @return array of {@code [drawPileTop, discardPileTop]}
     */
    public Card[] getBothPilesTop() {
        return new Card[] {this.drawPile.getAllCards().getLast(), this.discardPile.getAllCards().getLast()};
    }

    /**
     * Returns the last card that was drawn (from either pile).
     *
     * @return the last drawn {@link Card}
     */
    public Card getLastDrawnedCard() {
        return this.lastDrawnCard;
    }

    // TURN LOGIC

    /**
     * Flips the card at the given coordinates in the given player's deck.
     * Only the current player is allowed to flip cards.
     *
     * @param player     the player attempting to flip a card
     * @param cardCoords {@code [row, col]} coordinates of the card to flip
     */
    public void flipCard(Player player, int[] cardCoords) {
        if (player != this.getCurrentPlayer()) {
            return;
        }

        player.getDeck().flipCard(cardCoords);
    }

    /**
     * Replaces the card at the given coordinates in the player's deck with a new card.
     * The old card is automatically sent to the discard pile.
     * Only the current player is allowed to replace cards.
     *
     * @param player     the player attempting the replacement
     * @param cardCoords {@code [row, col]} coordinates of the card to replace
     * @param newCard    the new card to place at those coordinates
     */
    public void replaceCard(Player player, int[] cardCoords, Card newCard) {
        if (player != this.getCurrentPlayer()) {
            return;
        }

        Card oldCard = player.getDeck().replaceCard(cardCoords, newCard);
        this.discardCard(player, oldCard);

        // no need to call this.endTurn() because discardCard alr does it
    }

    /**
     * Draws the top card from the named pile and stores it as the last drawn card.
     *
     * @param pileName {@code "draw"} or {@code "discard"} (case-insensitive)
     * @return the card that was drawn
     * @throws IllegalArgumentException if {@code pileName} is neither "draw" nor "discard"
     */
    public Card drawCard(String pileName) {
        if (pileName.toLowerCase().equals("draw")) {
            Card card = this.drawPile.pop();
            this.lastDrawnCard = card;
            return card;
        } else if (pileName.toLowerCase().equals("discard")) {
            Card card = this.discardPile.pop();
            this.lastDrawnCard = card;
            return card;
        } else {
            throw new IllegalArgumentException("Unknown pile: " + pileName);
        }
    }

    /**
     * Sends the given card to the discard pile and ends the current player's turn.
     * Only the current player is allowed to discard.
     *
     * @param player the player discarding the card
     * @param card   the card to discard
     */
    public void discardCard(Player player, Card card) {
        if (player != this.getCurrentPlayer()) {
            return;
        }

        this.discardPile.addCard(card);

        // We'll assume the card was already removed from the pile
        // so we don't try to remove duplicates

        this.endTurn();
    }

    /**
     * Advances the game to the next player's turn, handling round endings and
     * full-column removal from the current player's deck.
     */
    private void endTurn() {
        if (!this.getCurrentPlayer().getDeck().hasHiddenCard()) {
            this.endRound();
        }

        this.discardPile.addCards(this.getCurrentPlayer().getDeck().removeColumns());

        this.currentPlayerIndex = (this.currentPlayerIndex + 1) % this.players.size();

        // if the round is ending
        if (this.startingPlayerIndex == this.currentPlayerIndex && this.isRoundEnding) {
            this.prepareNewRound(false);
        }
        else {
            // else we simply begin a new turn
            notifyPlayer(getCurrentPlayer(), l -> l.onTurnStarted(new TurnStartedEvent(
                this.getCurrentPlayer(), this.getDrawPileTop(), this.getDiscardPileTop(), this.round, this.roundScore)
            ));
        }
    }

    // SHOP BINDING AND LOGIC

    /**
     * Sells a joker from the given player's collection, adding its sell value to
     * their money and notifying listeners.
     *
     * @param player the player selling the joker
     * @param joker  the joker to sell
     */
    public void sellJoker(Player player, Joker joker) {
        System.out.println("tel aviv impressed");
        this.shop.sellJoker(player, joker);
        notifyPlayer(player, l -> l.onJokerSold(new JokerSoldEvent(player)));
    }

    /**
     * Purchases the given joker for the player, deducting its cost from their money
     * and notifying listeners with the updated shop contents.
     *
     * @param player the player buying the joker
     * @param joker  the joker to buy
     */
    public void buyJoker(Player player, Joker joker) {
        ArrayList<Joker> jokers = this.shop.buyJoker(player, joker);
        notifyPlayer(player, l -> l.onShopRerolledEvent(new ShopRerolledEvent(jokers)));
    }

    /**
     * Rerolls the shop for the given player, deducting the reroll cost from their
     * money and notifying listeners with the new joker offerings.
     *
     * @param player the player requesting the reroll
     */
    public void rerollShop(Player player) {
        ArrayList<Joker> jokers = this.shop.rerollShop(player);
        notifyPlayer(player, l -> l.onShopRerolledEvent(new ShopRerolledEvent(jokers)));
    }

    /**
     * Marks one more player as ready to proceed to the next round.
     * Once all players are ready, the new round begins.
     */
    public void readyUp() {
        this.nbrPlayersReady += 1;
        System.out.println("Number of players ready: "+this.nbrPlayersReady+"/"+this.players.size());
        if (this.nbrPlayersReady >= this.players.size()) {
            this.nbrPlayersReady = this.players.size();
            beginRound(false);
        }
    }

    // NEW ROUND LOGIC

    /**
     * Begins a new round by resetting the game state and notifying the current
     * player that their turn has started.
     *
     * @param setup {@code true} if this is the very first round of a new game
     */
    private void beginRound(boolean setup) {
        this.isRoundEnding = false;
        this.gameState = 1;

        // we start a new round from here only if it's the 1st one of the game
        // actually maybe not, we'll have to see how to implement the shop in here
        if (setup || this.nbrPlayersReady == this.players.size()) {
            notifyPlayer(getCurrentPlayer(), l -> l.onTurnStarted(new TurnStartedEvent(
            getCurrentPlayer(), getDrawPileTop(), getDiscardPileTop(), this.round, this.roundScore)
        ));
    }
    }

    /**
     * Marks the current round as ending and increments the round counter.
     * The actual cleanup and scoring happen in {@link #prepareNewRound(boolean)}.
     */
    private void endRound() {
        this.round = this.round + 1;
        this.isRoundEnding = true;
    }

    /**
     * Scores the finished round, distributes money and interest, shuffles and
     * redeals the draw pile, and fires the {@code onRoundEnded} event.
     * <p>
     * If the combined player score is below the quota, the game state is set to
     * {@code 0} (loss). Otherwise it is set to {@code 2} (proceed to shop).
     * </p>
     *
     * @param setup {@code true} when called as part of initial game setup (first round)
     */
    private void prepareNewRound(boolean setup) {
        if (!this.isRoundEnding) {
            return;
        }

        if (setup) {
            this.currentPlayerIndex = 0;
            this.startingPlayerIndex = 0;
            this.round = 1;
            this.roundScore = scorePerRounds[this.round-1];
            this.moneyPerRound = 3;
            this.lastDrawnCard = null;
            this.gameState = 1;

            for (Player plr : this.players) {
                plr.resetPlayer();
            }
        }

        System.out.println("\nPreparing new round...");

        int plrPoints = 0;

        Map<Player, Deck> playerDecks = new HashMap<>();
        Map<Player, Integer> playerMoneys = new HashMap<>();

        for (Player plr : this.players) {
            // AJOUTER LES JOKERS LÀ

            playerDecks.put(plr, plr.getDeck().getFreshDeck());
            playerMoneys.put(plr, plr.getMoney());

            // Remove full columns
            this.discardPile.addCards(plr.getDeck().removeColumns(true));

            for (Joker j : plr.getJokers()) {
                if (j.getCategory() == JokerCategory.DECK) {
                    j.apply(plr.getDeck());
                }
            }

            ArrayList<int[]> combos = plr.getDeck().scanCombos();
            // OU LÀ

            for (Joker j : plr.getJokers()) {
                if (j.getCategory() == JokerCategory.COMBO) {
                    j.apply(combos);
                }
            }

            for (Joker j : plr.getUpgrades()) {
                if (j.getCategory() == JokerCategory.COMBO) {
                    j.apply(combos);
                }
            }

            int totalCardValue = 0;
            for (Card c : plr.getDeck().getAllCards()) {
                totalCardValue += c.getValue();
            }

            int totalComboValue = 0;
            for (int[] c : combos) {
                totalComboValue += c[0] * c[1];
            }

            int totalValue = totalCardValue + totalComboValue;

            plr.setPoints(plr.getPoints() + totalValue);
            plrPoints += totalValue;

            // we get the money used for interests calculation now
            int plrMoney = plr.getMoney();

            // if the player beated his fraction of the quota, give him money
            if (totalValue >= this.roundScore/this.players.size()) {
                plr.setMoney(plr.getMoney() + this.moneyPerRound);
                // we'll also ward him bonus money based on how well he performed this round
                int moneyToAdd = (((totalValue)/(this.roundScore/this.players.size()))-1)*plr.getBonusMoneyRate();
                    if (moneyToAdd >= 0) {
                    plr.setMoney(plr.getMoney() + moneyToAdd);
                }
            }

            int[] plrInterests = plr.getInterests();
            
            for (int i=0;i<plrInterests[2];i++) {
                if ((plrMoney - plrInterests[1]) >= 0) {
                    plrMoney -= plrInterests[1];
                    plr.setMoney(plr.getMoney() + plrInterests[0]);
                }
                else {
                    break;
                }
            }
            
            ArrayList<Joker> jokers = this.shop.rerollShop(plr, true);
            notifyPlayer(plr, l -> l.onShopRerolledEvent(new ShopRerolledEvent(jokers)));

            for (Card c : plr.getDeck().getAllCards()) {
                this.discardPile.addCard(c);
            }
            plr.getDeck().clear();
        }

        if (plrPoints < this.roundScore && !setup) {
            // player didn't get enough point, he lost this game
            this.gameState = 0;
            System.out.println("sad1");
        }
        else {
            this.gameState = 2;
        }

        // transfer all the cards from the discard to the draw pile
        for (int i=0;i<this.discardPile.size();i++) {
            this.drawPile.addCard(this.discardPile.pop());
        }

        System.out.println("Shuffling draw pile...");
        drawPile.shuffle();
        drawPile.hideAll();

        for (Player plr : this.players) {
            Deck deck = plr.getDeck();
            int height = deck.getMaxHeight();
            int length = deck.getMaxLength();
            for (int i=0;i<height;i++) {
                ArrayList<Card> row = new ArrayList<>();
                for (int j=0;j<length;j++) {
                    row.add(drawPile.pop());
                }
                deck.addRow(row);
            }
        }

        System.out.println("Draw pile size: "+this.drawPile.size());

        // set up the new score to beat
        this.roundScore = scorePerRounds[this.round - 1];

        notifyAll(l -> l.onRoundEnded(new RoundEndedEvent(
            this.roundScore, this.gameState, setup, playerDecks, playerMoneys))
        );

        // only if we didn't lose, we start a new round once everyone is done shopping
        if (this.gameState > 0) {
            this.nbrPlayersReady = 0;
            beginRound(setup);
        }
    }

    /**
     * Starts the game by running initial round preparation.
     * This should be called once via a {@link com.neuilleprime.game.actions.BeginGameAction}.
     */
    public void beginGame() {
        prepareNewRound(true);
    }

    /**
     * Returns the current game state code.
     *
     * @return {@code 0} = loss, {@code 1} = next turn, {@code 2} = next round (shop)
     */
    public int getGameState() {
        return this.gameState;
    }
}
