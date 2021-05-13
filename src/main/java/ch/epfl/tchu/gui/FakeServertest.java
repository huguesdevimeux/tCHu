package ch.epfl.tchu.gui;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class FakeServertest {
    // Messages d'exemple de la §2.1 de l'étape 7
    private static final List<String> MESSAGES =
            List.of(
                    "INIT_PLAYERS 0 QWRh,Q2hhcmxlcw==",
                    "RECEIVE_INFO QWRhIGpvdWVyYSBlbiBwcmVtaWVyLgoK",
                    "SET_INITIAL_TICKETS 6,1,44,42,42",
                    "UPDATE_STATE 36:6,7,4,7,1;97;0:0:0;4;:0;4;: ;0,1,5,5;",
                    "CHOOSE_INITIAL_TICKETS");

    public static void main(String[] args) throws IOException {
        try (ServerSocket s0 = new ServerSocket(5108);
                Socket s = s0.accept();
                BufferedReader r =
                        new BufferedReader(new InputStreamReader(s.getInputStream(), US_ASCII));
                BufferedWriter w =
                        new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), US_ASCII))) {
            System.out.println("Connected");
            // Envoi des messages
            for (String m : MESSAGES) {
                System.out.println("Sending " + m);
                w.write(m + '\n');
                w.flush();
            }
            // Attente et impression de la réponse
            System.out.println(r.readLine());
        }
    }
}
