package ch.epfl.tchu.gui.animation;

import javafx.animation.Interpolator;

import static java.lang.Math.PI;

/**
 * Standards easing functions used as interpolator for animations.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class CustomInterpolators {

    public static final Interpolator EASE_OUT_SINE =
            new Interpolator() {
                public double curve(double x) {
                    return Math.sin((x * PI) / 2);
                }
            };
    public static Interpolator EASE_OUT_BOUNCE =
            new Interpolator() {
                private static final float n1 = 7.5625f;
                private static final float d1 = 2.75f;

                @Override
                protected double curve(double x) {
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
            };
}
