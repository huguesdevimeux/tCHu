package ch.epfl.tchu.gui.animation;

import javafx.scene.Node;

/**
 * Can attach to a {@link Node} a AbstractAnimation.
 */
public interface AnimationAttacher {
	/**
	 * Returns an animation parametrized with the given animated.
	 *
	 * @param animated The node to animate.
	 * @return The new animation.
	 */
	AbstractAnimation attachTo(Node animated);
}
