package ch.epfl.tchu.gui.animation;

import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;

/**
 * Implements a translation animation on hover. Plays back when the mouse exit the node.
 *
 * @author ${
 */
public class TranslationOnHoverAnimation  {

	private final float offsetX;
	private final float offsetY;
	private final float cycleTime;
	private TranslateTransition innerTransition;

	private TranslationOnHoverAnimation(float offsetX, float offsetY, float cycleTime) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.cycleTime = cycleTime;

	}

	public static void attachTo(Node animated) {
		TranslateTransition translateTransition = new TranslateTransition();
		animated.setOnMouseEntered(event -> {

		});
	}

	private static double easeOutBounce(double x) {
		float n1 = 7.5625f;
		float d1 = 2.75f;

		if (x < 1 / d1) {
			return n1 * x * x;
		} else if (x < 2 / d1) {
			return n1 * (x -= 1.5 / d1) * x + 0.75;
		} else if (x < 2.5 / d1) {
			return n1 * (x -= 2.25 / d1) * x + 0.9375;
		} else {
			return n1 * (x -= 2.625 / d1) * x + 0.984375;
		}
	}

}
