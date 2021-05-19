package ch.epfl.tchu.gui.animation;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Implements a translation animation on hover. Plays back when the mouse exit the node.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class TranslationOnHoverAnimation implements AnimationAttacher{

    private final float offsetX;
    private final float offsetY;
    private final Duration cycleTime;
    private final Interpolator interpolator1;
    private final Interpolator interpolator2;

	/**
	 * Construct a TranslationOnHover animation, given the animated node and the necessary parameters.
	 *
	 * @param offsetX The offset in the x axis the node will be translated of.
	 * @param offsetY The offset in the y axis the node will be translated of.
	 * @param cycleTime The time of the animation.
	 * @param interpolator1 The interpolator for the first way animation.
	 * @param interpolator2 the interpolator for the second way animation (the return).
	 */
    public TranslationOnHoverAnimation(
            float offsetX,
            float offsetY,
            Duration cycleTime,
            Interpolator interpolator1,
            Interpolator interpolator2) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.cycleTime = cycleTime;
        this.interpolator1 = interpolator1;
        this.interpolator2 = interpolator2;
    }

	/**
	 * Given a target, applies an animation translation to the target while taking care of not going further the target.
	 * Stop translateTransition if playing.
	 *
	 * @param relativeXTarget The target in the x axis.
	 * @param relativeYTarget The target in the y axis.
	 * @param translateTransition The translation to apply.
	 * @param animated The animated node.
	 */
    private static void translateAnimatedTo(
            float relativeXTarget,
            float relativeYTarget,
            TranslateTransition translateTransition,
            Node animated) {
        translateTransition.stop();
        translateTransition.setByX(relativeXTarget - animated.getTranslateX());
        translateTransition.setByY(relativeYTarget - animated.getTranslateY());
        translateTransition.play();
    }

	/**
	 * Attach a {@link TranslationOnHoverAnimation} to animated
	 *
	 * @param animated The node to animate.
	 */
	public TranslationOnHover attachTo(Node animated) {
		return new TranslationOnHover(animated);
    }

    private class TranslationOnHover extends AbstractAnimation {

		private final TranslateTransition translateTransition;

		public TranslationOnHover(Node animated) {
			super(animated);
			this.translateTransition = new TranslateTransition(cycleTime, animated);
		}

		@Override
		public void play() {
			translateTransition.setInterpolator(interpolator1);
			translateAnimatedTo(offsetX, offsetY, translateTransition, animated);
		}

		@Override
		public void reversePlay() {
			translateTransition.setInterpolator(interpolator2);
			translateAnimatedTo(0, 0, translateTransition, animated);
		}
	}
}
