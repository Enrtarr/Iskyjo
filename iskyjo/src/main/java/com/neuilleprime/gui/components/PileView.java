package com.neuilleprime.gui.components;

import com.neuilleprime.game.Card;
import com.neuilleprime.game.Pile;

import javafx.scene.layout.StackPane;

public class PileView extends StackPane {

    private Pile pileElem;

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
