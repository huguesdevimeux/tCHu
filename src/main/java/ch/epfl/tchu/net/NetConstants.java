package ch.epfl.tchu.net;

import java.nio.charset.Charset;
import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * GuiConstants ans settings commonly used in the package through tCHu.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class NetConstants {

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
        public static final int NUMBER_PARAMETERS_REQUIRED = 3;
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

    public static class Image {
        public static final String EXTENSION_IMAGE = "png";
        public static final String FILENAME_PROFILE_IMAGE =
                "/home/hugues/OneDrive/Desktop/Programmation/JAVA/tCHu/%s.png";
        public static final String FILENAME_DEFAULT_PROFILE_IMAGE =
			"file:///home/hugues/OneDrive/Desktop/Programmation/JAVA/tCHu/src/main/resources/%s.png";

    }
}
