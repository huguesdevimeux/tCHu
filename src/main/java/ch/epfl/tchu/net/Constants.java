package ch.epfl.tchu.net;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Constants ans settings commonly used in the package through tCHu.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class Constants {

    public static class Network {
        /* Encoding used in CharSet message */
        public static final Charset ENCODING = US_ASCII;
        /* The default port used for connection */
        public static final int DEFAULT_PORT = 5108;
        /* The default IP used for connection */
        public static final String DEFAULT_IP = "localhost";
        /* Default names for the players.*/
        public static final List<String> DEFAULT_NAMES = List.of("Ada", "Charles");
        /* Required number of paramaters */
        public static final int NUMBER_PARAMETERS_REQUIRED = 2;
		/* End line char used in network messages.*/
		public static final String CHAR_END_MESSAGE = "\n";
		/* Character used to separate the components in the network messages.*/
		public static final String SEPARATOR_COMPONENT_MESSAGE = " ";

		private Network() {}
    }

    public static class Serdes {
        /* UTF-8 encoding for string serde */
        public static final Charset ENCODING = UTF_8;
		/* Comma used in network to separate elements. */
        public static final String SEPARATOR_1 = ",";
        /* Semi-colon used in network to separate elements. */
        public static final String SEPARATOR_2 = ";";
        /* Colon used in network to separate elements. */
        public static final String SEPARATOR_3 = ":";
        /* Default value when the element is either null or empty. */
        public static final String DEFAULT_VALUE_EMPTINESS = "";

        private Serdes() {}
    }
}
