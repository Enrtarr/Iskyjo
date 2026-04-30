package com.neuilleprime.gui.components;

import com.neuilleprime.game.Card;
import com.neuilleprime.game.Pile;

import javafx.scene.layout.StackPane;

/**
 * JavaFX component that displays the top card of a {@link Pile}.
 * <p>
 * Automatically updates whenever the pile's size changes, replacing the
 * displayed {@link CardView} with the new top card.
 * </p>
 */
public class PileView extends StackPane {

    /** The underlying game model pile this view is bound to. */
    private Pile pileElem;

    /**
     * Constructs a {@code PileView} for the given pile.
     * Displays the top card immediately, or a default card if the pile is empty.
     * Registers a listener to refresh the view whenever the pile size changes.
     *
     * @param pile the pile to display
     */
    public PileView(Pile pile) {
        this.pileElem = pile;

        if (this.pileElem.size() == 0) {
            CardView topCardView = new CardView(new Card());
            this.getChildren().add(topCardView);
        }
        else {
            CardView topCardView = new CardView(this.pileElem.getTop());
            this.getChildren().add(topCardView);
        }

        // PROBLEME IMPORTANT : PAS DE MAJ LORS D'UN SHUFFLE

        this.pileElem.sizeProperty().addListener((obs, oldVal, newVal) -> {
            this.getChildren().clear();
            
            CardView newtopCardView = new CardView(this.pileElem.getTop());
            this.getChildren().add(newtopCardView);
        });
    }
}
