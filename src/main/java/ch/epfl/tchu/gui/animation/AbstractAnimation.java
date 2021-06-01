package ch.epfl.tchu.gui.animation;

import javafx.scene.Node;

public abstract class AbstractAnimation {

	protected final Node animated;

	public AbstractAnimation(Node animated) {
		this.animated = animated;
	}

	/**
	 * Plays in normal order the animation, until it reaches its stop.
	 */
	public abstract void play();

	/**
	 * Plays in reverse the animation, until it reaches its starting state.
	 */
	public abstract void  reversePlay();
}
