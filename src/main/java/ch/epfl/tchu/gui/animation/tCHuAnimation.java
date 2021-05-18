package ch.epfl.tchu.gui.animation;

// TODO change this name
public interface tCHuAnimation {
	/**
	 * Plays in normal order the animation, until it reaches its stop.
	 */
	void play();

	/**
	 * Plays in reverse the animation, until it reaches its starting state.
	 */
	void reversePlay();
}
