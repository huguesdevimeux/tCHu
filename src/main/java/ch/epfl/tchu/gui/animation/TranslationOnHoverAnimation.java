package ch.epfl.tchu.gui.animation;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Implements a translation animation on hover. Plays back when the mouse exit the node.
 *
 * @author ${
 */
public class TranslationOnHoverAnimation {

    private final float offsetX;
    private final float offsetY;
    private final Duration time1;
    private final Interpolator interpolator1;
    private final Interpolator interpolator2;

    public TranslationOnHoverAnimation(
            float offsetX,
            float offsetY,
            Duration time1,
            Interpolator interpolator1,
            Interpolator interpolator2) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.time1 = time1;
        this.interpolator1 = interpolator1;
        this.interpolator2 = interpolator2;
    }

    private static void moveAnimatedTo(
            float relativeXTarget,
            float relativeYTarget,
            TranslateTransition translateTransition,
            Node animated) {
        translateTransition.stop();
        translateTransition.setByX(relativeXTarget - animated.getTranslateX());
        translateTransition.setByY(relativeYTarget - animated.getTranslateY());
        translateTransition.play();
    }

    public void attachTo(Node animated) {
        TranslateTransition translateTransition = new TranslateTransition(this.time1, animated);
        translateTransition.setDuration(time1);
        animated.setOnMouseEntered(
                mouseEvent -> {
                    translateTransition.setInterpolator(interpolator1);
                    moveAnimatedTo(offsetX, offsetY, translateTransition, animated);
                });
        animated.setOnMouseExited(
                mouseEvent -> {
                    translateTransition.setInterpolator(interpolator2);
                    moveAnimatedTo(0, 0, translateTransition, animated);
                });
    }
}
