package ch.epfl.tchu.gui;

import ch.epfl.tchu.net.Constants;
import ch.epfl.tchu.net.RemotePlayerClient;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

/**
 * Implements a client for tCHu, to play a game as a Client.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class ClientMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        List<String> params = getParameters().getRaw();
        // Default values.
        String ipTarget = Constants.Network.DEFAULT_IP;
        int port = Constants.Network.DEFAULT_PORT;
        if (params.size() == Constants.Network.NUMBER_PARAMETERS_REQUIRED) {
            ipTarget = params.get(0);
            port = Integer.parseInt(params.get(1));
        } else if (params.size() != 0)
            throw new Exception("Wrong number of parameters given to the programme. Exiting.");

        String finalIpTarget = ipTarget;
        int finalPort = port;
        new Thread(
                        () ->
                                new RemotePlayerClient(
                                                new GraphicalPlayerAdapter(),
                                                finalIpTarget,
                                                finalPort)
                                        .run())
                .start();
    }
}
