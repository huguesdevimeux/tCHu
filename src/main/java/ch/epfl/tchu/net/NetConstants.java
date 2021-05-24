package ch.epfl.tchu.net;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.nio.charset.Charset;
import java.util.List;

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
    /* The default port used for conne	ction */
    public static final int DEFAULT_PORT = 5108;
    /* The default IP used for connection */
    public static final String DEFAULT_IP = "localhost";
    /* Default names for the players.*/
    public static final List<String> DEFAULT_NAMES = List.of("Ada", "Charles");
    /* Required number of paramaters */
    public static final int PARAMETERS_REQUIRED = 2;
}
