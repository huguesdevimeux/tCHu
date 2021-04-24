package ch.epfl.tchu.net;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.nio.charset.Charset;

/**
 * Constants ans settings commonly used in network operations through tCHu.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class NetConst {
    /* Encoding used in CharSet message */
    public static final Charset ENCODING = US_ASCII;
    /* Endile char used in networkd messages.*/
    public static final String ENDLINE = "\n";
    /* Space used in network messages.*/
    public static final String SPACE = " ";
}
