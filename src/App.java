import java.util.ArrayList;
import java.util.Scanner;

import game.*;
// import jokers.*;
import game.actions.*;
import jokers.AddXCardJoker;
import jokers.ComboRightJoker;
import jokers.Joker;

public class App {

    public static Scanner scanner;
    public static void main(String[] args) throws Exception {
        int nbrOfCards = 150;
        int nbrOfPlayers = 1;

         scanner = new Scanner(System.in);

        GameController game = setupGame(nbrOfPlayers, nbrOfCards);
        // FIND A WAY TO START THE GAME CLEANLY
        Action beginGame = new BeginGameAction();
        game.execute(beginGame);
        
        while (true) {
            Player curPlayer = game.getCurrentPlayer();

            System.out.println("");
            System.out.println("Player "+curPlayer.getName()+"'s turn.");
            System.out.println("Here is you deck:");
            curPlayer.getDeck().printAll();
            System.out.println("Here are the top cards of both piles:");
            if (game.getDrawPileTop() != null) {
                System.out.println("Draw pile: "+game.getDrawPileTop().getValue());
            }
            else {
                System.out.println("Draw pile: empty");
            }

            if (game.getDiscardPileTop() != null) {
                System.out.println("Discard pile: "+game.getDiscardPileTop().getValue());
            }
            else {
                System.out.println("Discard pile: empty");
            }
            boolean isDrawPileEmpty = (game.getDiscardPileTop() != null || nbrOfPlayers == 1) ? true : false;

            if (!isDrawPileEmpty) {
                System.out.println("What would you want to do?");
                System.out.println("1) Pick the card at the top of the draw pile");
                System.out.println("2) Pick the card at the top of the discard pile");
            }
            else {
                System.out.println("Picking the card at the top of the draw pile");
            }

            int answer = 1;
            if (!isDrawPileEmpty) {
                scanner.nextInt();
            }

            Card card = null;

            switch (answer) {
                case 1:
                    // System.out.println("Drawing from draw pile...");
                    Action pickDraw = new DrawCardAction("draw");
                    game.execute(pickDraw);
                    card = game.getLastDrawnedCard();
                    break;
                case 2:
                    // System.out.println("Drawing from discard pile...");
                    Action pickDiscard = new DrawCardAction("discard");
                    game.execute(pickDiscard);
                    card = game.getLastDrawnedCard();
                    break;
                default:
            }

            System.out.println("What would you want to do?");
            System.out.println("1) Replace a card from your deck with the card you just drew");
            System.out.println("2) Throw it to the discard card and flip one of your cards");
            answer = scanner.nextInt();

            System.out.println("Which card? (Enter the coordinates separated by a space, top left is 1 1, length height)");
            int l = scanner.nextInt() - 1;
            int h = scanner.nextInt() - 1;

            int[] coords = new int[]{h,l};

            switch (answer) {
                case 1:
                    // System.out.println("Drawing from draw pile...");
                    Action replaceCard = new ReplaceCardAction(coords, card);
                    card.show();
                    game.execute(replaceCard);
                    break;
                case 2:
                    // System.out.println("Drawing from discard pile...");
                    Action flipCard = new FlipCardAction(coords);
                    Action discardCard = new DiscardCardAction(card);
                    game.execute(flipCard);
                    game.execute(discardCard);
                    break;
                default:
            }

            Class<? extends Joker> jokerType = Joker.getRandomType();

            if (jokerType == AddXCardJoker.class) {
                new AddXCardJoker(5, true);
            } else if (jokerType == ComboRightJoker.class) {
                new ComboRightJoker(3, 4);
            }
        }

        // scanner.close();

        // System.out.println("Generating a draw pile with " + nbrOfCards + " cards");
        // Pile drawPile = new Pile(nbrOfCards);
        // drawPile.printAll(true);

        // Scanner scanner = new Scanner(System.in);

        // System.out.print("Enter two integers separated by a space: ");

        // int a = scanner.nextInt();
        // int b = scanner.nextInt();
        // System.out.println("1st: "+a+" 2nd: "+b);
        // scanner.close();

        // GameController game = setupGame();
        // Scanner scanner = new Scanner(System.in);

        // while (!game.isGameOver()) {
        //     Player player = game.getCurrentPlayer();

        //     System.out.println("Your turn: " + player.getName());
        //     int choice = scanner.nextInt();

        //     game.playCard(player, player.getHand().get(choice));
        //     game.endTurn();
        // }
    }

    public static GameController setupGame(int nbrPlr, int nbrCard) {

        ArrayList<Player> playerList = new ArrayList<>();

        for (int i=1;i<nbrPlr+1;i++) {
            // System.out.print("Please enter player "+i+"'s name: ");
            // String name = scanner.nextLine();
            // playerList.add(new Player(name));
            playerList.add(new Player());
        }

        return new GameController(playerList, nbrCard);
    }
}
