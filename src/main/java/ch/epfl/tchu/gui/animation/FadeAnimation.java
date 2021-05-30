package ch.epfl.tchu.gui.animation;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Animation that fades out and in the animated.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class FadeAnimation implements AnimationAttacher {
    private final Duration cycleTime;
    private final double from;
    private final double to;

    /**
     * Constructor for {@link FadeAnimation}.
     *
     * @param cycleTime Time of 1 animation cycle.
     * @param from Fading value the animation will start from. (it will end at this value in reverse
     *     order).
     * @param to Fading value the animation will start from. (it will start at this value in reverse
     *     order).
     */
    public FadeAnimation(Duration cycleTime, double from, double to) {
        this.cycleTime = cycleTime;
        this.from = from;
        this.to = to;
    }

    @Override
    public AbstractAnimation attachTo(Node animated) {
        return new Fade(animated);
    }

    private class Fade extends AbstractAnimation {

        private final FadeTransition fadeTransition;

        public Fade(Node animated) {
            super(animated);
            this.fadeTransition = new FadeTransition(cycleTime, animated);
        }

        @Override
        public void play() {
            playFade(from, to);
        }

        @Override
        public void reversePlay() {
            playFade(to, from);
        }

        private void playFade(double from, double to) {
            fadeTransition.stop();
            fadeTransition.setFromValue(from);
            fadeTransition.setToValue(to);
            fadeTransition.playFromStart();
        }
    }
}
