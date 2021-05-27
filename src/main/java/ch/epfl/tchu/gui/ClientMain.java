package ch.epfl.tchu.gui;

import ch.epfl.tchu.net.RemotePlayerClient;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class ClientMain  {
    private static String[] args;
    public static void main(String[] args) {
        ClientMain.args = args;
        new Thread(
                () ->
                        new RemotePlayerClient(
                                new GraphicalPlayerAdapter(),
                                args[0],
                                Integer.parseInt(args[1]))
                                .run())
                .start();
        //launch(args);
    }

//    @Override
//    public void start(Stage stage) throws Exception {
//      //  List<String> params = getParameters().getRaw();
//        // Default values.
//        String ipTarget = GuiConstants.DEFAULT_IP;
//        int port = GuiConstants.DEFAULT_PORT;
////        if (params.size() == 2) {
////            ipTarget = params.get(0);
////            port = Integer.parseInt(params.get(1));
////        } else if (params.size() != 0)
////            throw new Exception("Wrong number of parameters given to the programme. Exiting.");
//
////        MainMenuServer mainMenu = new MainMenuServer();
////        mainMenu.mainMenuView().show();
//
//        String finalIpTarget = ipTarget;
//        int finalPort = port;
//        new Thread(
//                        () ->
//                                new RemotePlayerClient(
//                                                new GraphicalPlayerAdapter(),
//                                                args[0],
//                                                Integer.parseInt(args[1]))
//                                        .run())
//                .start();

}
