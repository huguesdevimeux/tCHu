package ch.epfl.tchu.gui.animation;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/** @author ${ */
public class IndicationAnimation implements AnimationAttacher{

    private final Duration cycleTime;
    private final float scalingX;
    private final float scalingY;
    private final int numberBounces;

    public IndicationAnimation (
            Duration cycleTime, float scalingX, float scalingY, int numberBounces) {
        this.cycleTime = cycleTime;
        this.scalingX = scalingX;
        this.scalingY = scalingY;
        this.numberBounces = numberBounces;
    }

    public tCHuAnimation attachTo(Node animated) {
        return new Indication(animated);
    }

    private class Indication implements tCHuAnimation {
        private final ScaleTransition scaleTransition;

        private Indication(Node animated) {
            this.scaleTransition = new ScaleTransition(cycleTime, animated);
            this.scaleTransition.setByX(scalingX);
            this.scaleTransition.setByY(scalingY);
            this.scaleTransition.setCycleCount(numberBounces);
            this.scaleTransition.setAutoReverse(true);
        }

        @Override
        public void play() {
            scaleTransition.play();
        }

        @Override
        public void reversePlay() {
            throw new UnsupportedOperationException();
        }
    }
}
