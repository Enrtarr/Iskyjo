package game;

import java.util.ArrayList;

import jokers.*;
import jokers.Joker.JokerCategory;
import game.actions.Action;

public class GameController {

    private ArrayList<Player> players;
    private Pile drawPile;
    private Pile discardPile;
    private int currentPlayerIndex;
    private int startingPlayerIndex;
    private int round;
    private boolean isRoundEnding;
    private int roundScore;
    private Card lastDrawnCard;

    public GameController(ArrayList<Player> players, int nbrOfCards) {
        this.players = players;
        this.drawPile = new Pile(nbrOfCards);
        this.discardPile = new Pile(0);
        this.currentPlayerIndex = 0;
        this.startingPlayerIndex = 0;
        this.round = 1;
        this.isRoundEnding = true;
        this.roundScore = 50;
        this.lastDrawnCard = null;
    }

    public void execute(Action action) {
        action.execute(this);
    }

    public Player getCurrentPlayer() {
        return this.players.get(this.currentPlayerIndex);
    }

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

    public Card getDiscardPileTop() {
        if (this.discardPile.getAllCards().size() > 0) {
            return this.discardPile.getAllCards().getLast();
        }
        else {
            return null;
        }
    }

    public Card[] getBothPilesTop() {
        return new Card[] {this.drawPile.getAllCards().getLast(), this.discardPile.getAllCards().getLast()};
    }

    public Card getLastDrawnedCard() {
        return this.lastDrawnCard;
    }

    public void flipCard(Player player, int[] cardCoords) {
        if (player != this.getCurrentPlayer()) {
            return;
        }

        player.getDeck().flipCard(cardCoords);
    }

    public void replaceCard(Player player, int[] cardCoords, Card newCard) {
        if (player != this.getCurrentPlayer()) {
            return;
        }

        // AJOUTER DES CHECKS DE PRÉSENCE DANS LA PILE

        Card oldCard = player.getDeck().replaceCard(cardCoords, newCard);
        this.discardCard(player, oldCard);

        this.endTurn();
    }

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

    public void discardCard(Player player, Card card) {
        if (player != this.getCurrentPlayer()) {
            return;
        }

        this.discardPile.addCard(card);

        // We'll assume the card was already removed from the pile
        // so we don't try to remove duplicates

        this.endTurn();
    }

    private boolean endTurn() {
        if (!this.getCurrentPlayer().getDeck().hasHiddenCard()) {
            this.endRound();
        }

        // VERIFIER LES COLONNES / LIGNES, ET LES SUPPRIMER SI BESOIN
        // (il faut rajouter une méthode à Deck pour ça)
        this.getCurrentPlayer().getDeck().removeColumns();

        this.currentPlayerIndex = (this.currentPlayerIndex + 1) % this.players.size();

        // Check if the current player has reached the point quota, if yes, endRound()
        // ^^ nvm ignore that

        if (this.startingPlayerIndex == this.currentPlayerIndex && this.isRoundEnding) {
            // will return false if the game is lost, true otherwise
            return this.prepareNewRound();
        }
        
        return true; //return true if the game can continue (not lost)
    }

    private void beginRound() {
        this.isRoundEnding = false;
    }

    private void endRound() {
        this.round = this.round + 1;
        this.isRoundEnding = true;
    }

    private boolean prepareNewRound() {
        if (!this.isRoundEnding) {
            return true;
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

            // AJOUTER LES POINTS ET TOUT, LÀ JE LE FAIT PAS HISTOIRE
            // DE POUVOIR TESTER AVANT 3H DU MAT

            for (Card c : plr.getDeck().getAllCards()) {
                this.discardPile.addCard(c);
            }
            plr.getDeck().clear();
        }

        if (plrPoints < this.roundScore) {
            // player didn't get enough point, he lost this game
            return false;
        }

        // METTRE LE JOUEUR AVEC LE PLUS/MOINS DE POINTS EN 1ER (PEUT-ÊTRE)

        // Y A MOYEN DE FAIRE ÇA AVEC POP AUSSI, JE PENSE
        for (Card c : this.discardPile.getAllCards()) {
            this.drawPile.addCard(c);
        }
        this.discardPile.clear();

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

        return true;
    }

    public void beginGame() {
        prepareNewRound();
    }
}