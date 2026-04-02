package game;

import java.util.ArrayList;

import jokers.*;
import jokers.Joker.JokerCategory;
import game.actions.Action;

/**
 * Controls the overall game flow, including players, turns, rounds,
 * card piles, and scoring logic.
 */
public class GameController {

    private ArrayList<Player> players;
    private Pile drawPile;
    private Pile discardPile;
    private int currentPlayerIndex;
    private int startingPlayerIndex;
    private int round;
    private boolean isRoundEnding;
    private int roundScore;
    private int moneyPerRound;
    private Card lastDrawnCard;
    /**
     * Game state:
     * 1 = next turn
     * 2 = next round (shop)
     * 0 = loss
     */
    private int gameState;

    /**
     * Creates a new GameController instance.
     *
     * @param players list of players participating in the game
     * @param nbrOfCards number of cards to initialize in the draw pile
     */
    public GameController(ArrayList<Player> players, int nbrOfCards) {
        this.players = players;
        this.drawPile = new Pile(nbrOfCards);
        this.discardPile = new Pile(0);
        this.currentPlayerIndex = 0;
        this.startingPlayerIndex = 0;
        this.round = 1;
        this.isRoundEnding = true;
        this.roundScore = 50;
        this.moneyPerRound = 3;
        this.lastDrawnCard = null;
        this.gameState = 1;
    }

    /**
     * Executes a given action on this game controller.
     *
     * @param action the action to execute
     */
    public void execute(Action action) {
        action.execute(this);
    }

    /**
     * Returns the current player.
     *
     * @return the active player
     */
    public Player getCurrentPlayer() {
        return this.players.get(this.currentPlayerIndex);
    }

    /**
     * Returns the top card of the draw pile.
     *
     * @return the top card, or null if empty
     */
    public Card getDrawPileTop() {
        // Cas d'un joueur solo, on peut pas le laisser piocher
        // if (this.players.size() == 1) {
        //     return getDiscardPileTop();
        // }

        if (this.drawPile.getAllCards().size() > 0) {
            return this.drawPile.getAllCards().getLast();
        }
        else {
            return null;
        }
    }

    /**
     * Returns the top card of the discard pile.
     *
     * @return the top card, or null if empty
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
     * Returns the top cards of both piles.
     *
     * @return an array containing [drawPileTop, discardPileTop]
     */
    public Card[] getBothPilesTop() {
        return new Card[] {this.drawPile.getAllCards().getLast(), this.discardPile.getAllCards().getLast()};
    }

    /**
     * Returns the last drawn card.
     *
     * @return the last drawn card
     */
    public Card getLastDrawnedCard() {
        return this.lastDrawnCard;
    }

    /**
     * Flips a card in the player's deck if it is their turn.
     *
     * @param player the player performing the action
     * @param cardCoords coordinates of the card to flip
     */
    public void flipCard(Player player, int[] cardCoords) {
        if (player != this.getCurrentPlayer()) {
            return;
        }

        player.getDeck().flipCard(cardCoords);
    }

    /**
     * Replaces a card in the player's deck with a new one,
     * then discards the old card.
     *
     * @param player the player performing the action
     * @param cardCoords coordinates of the card to replace
     * @param newCard the new card
     */
    public void replaceCard(Player player, int[] cardCoords, Card newCard) {
        if (player != this.getCurrentPlayer()) {
            return;
        }

        // AJOUTER DES CHECKS DE PRÉSENCE DANS LA PILE

        Card oldCard = player.getDeck().replaceCard(cardCoords, newCard);
        this.discardCard(player, oldCard);

        this.endTurn();
    }

    /**
     * Draws a card from the specified pile.
     *
     * @param pileName name of the pile ("draw" or "discard")
     * @return the drawn card
     * @throws IllegalArgumentException if the pile name is unknown
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
     * Adds a card to the discard pile if it is the player's turn.
     *
     * @param player the player discarding the card
     * @param card the card to discard
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
     * Ends the current player's turn and handles round transitions.
     */
    private void endTurn() {
        if (!this.getCurrentPlayer().getDeck().hasHiddenCard()) {
            this.endRound();
        }

        this.getCurrentPlayer().getDeck().removeColumns();

        this.currentPlayerIndex = (this.currentPlayerIndex + 1) % this.players.size();

        if (this.startingPlayerIndex == this.currentPlayerIndex && this.isRoundEnding) {
            this.prepareNewRound();
        }
    }

    /**
     * Begins a new round.
     */
    private void beginRound() {
        this.isRoundEnding = false;
        this.gameState = 1;
    }

    /**
     * Ends the current round and increments the round counter.
     */
    private void endRound() {
        this.round = this.round + 1;
        this.isRoundEnding = true;
    }

    /**
     * Prepares the next round:
     * applies joker effects, calculates scores,
     * distributes rewards, resets decks and piles.
     */
    private void prepareNewRound() {
        if (!this.isRoundEnding) {
            return;
        }

        System.out.println("\nPreparing new round...");

        int plrPoints = 0;

        for (Player plr : this.players) {
            // AJOUTER LES JOKERS LÀ

            // Remove full columns
            plr.getDeck().removeColumns(true);

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

            // if the player beated his fraction of the quota, give him money
            if (totalValue >= this.roundScore/this.players.size()) {
                plr.setMoney(plr.getMoney() + this.moneyPerRound);
            }

            // the %-age of money to add to the player
            int moneyToAdd = (((totalValue*100)/(this.roundScore/this.players.size()))-1)*plr.getInterests();
            plr.setMoney((int) Math.floor(plr.getMoney() + (plr.getMoney() * moneyToAdd)));

            for (Card c : plr.getDeck().getAllCards()) {
                this.discardPile.addCard(c);
            }
            plr.getDeck().clear();
        }

        if (plrPoints < this.roundScore) {
            // player didn't get enough point, he lost this game
            this.gameState = 0;
        }
        else {
            this.gameState = 2;
        }

        // transfer all the cards from the discard to the draw pile
        for (int i=0;i<this.discardPile.size();i++) {
            this.drawPile.addCard(this.discardPile.pop());
        }

        // System.out.print("Draw pile: ");
        // this.drawPile.printAll();
        // System.out.print("Discard pile: ");
        // this.discardPile.printAll();

        System.out.println("\nShuffling draw pile...");
        drawPile.shuffle();
        // System.out.print("New draw pile: ");
        // this.drawPile.printAll();

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
            // System.out.println(plr.getName()+"'s deck:");
            // deck.printAll();
        }

        beginRound();
    }

    /**
     * Starts the game by preparing the first round.
     */
    public void beginGame() {
        prepareNewRound();
    }

    /**
     * Returns the current game state.
     *
     * @return the game state (0, 1, or 2)
     */
    public int getGameState() {
        return this.gameState;
    }
}
