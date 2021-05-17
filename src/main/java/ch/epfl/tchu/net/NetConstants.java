package ch.epfl.tchu.net;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.nio.charset.Charset;

/**
 * Constants ans settings commonly used in network operations through tCHu.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class NetConstants {
    /* Encoding used in CharSet message */
    public static final Charset ENCODING = US_ASCII;
    /* End line char used in networkd messages.*/
    public static final String END_LINE = "\n";
    /* Space used in network messages.*/
    public static final String SPACE = " ";
    /* Comma used in network to separate elements. */
    public static final String COMMA_SEPARATOR = ",";
    /* Semi-colon used in network to separate elements. */
    public static final String SEMI_COLON_SEPARATOR = ";";
    /* Colon used in network to separate elements. */
    public static final String COLON_SEPARATOR = ":";
    /* Host used in the game. */
    public static final String SERVER_HOST = "localhost";
    /* Port number on which the server listens. */
    public static final int PORT_NUMBER = 5108;
}
