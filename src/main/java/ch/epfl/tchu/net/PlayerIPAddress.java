package ch.epfl.tchu.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Simple class that allows a player to obtain its ip address.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public final class PlayerIPAddress {
    public static String getPublicIPAddress() throws UnknownHostException {
        String publicIP = "";
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
            publicIP = sc.readLine().trim();
        } catch (Exception e) {
            publicIP = "Cannot Execute Properly";
        }
        return publicIP;
    }
}
