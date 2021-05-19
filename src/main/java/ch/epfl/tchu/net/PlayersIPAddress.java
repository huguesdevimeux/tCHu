package ch.epfl.tchu.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Simple class that allows a player to obtain its ip address.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public final class PlayersIPAddress {
  public static String getIPAddress() throws UnknownHostException {
    InetAddress IP = InetAddress.getLocalHost();
    return IP.getHostAddress();
  }
}
