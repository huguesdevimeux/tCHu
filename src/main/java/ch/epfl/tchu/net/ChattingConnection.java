package ch.epfl.tchu.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

import static ch.epfl.tchu.net.Serdes.stringSerde;

public abstract class ChattingConnection {
    private Consumer<String> onReceiveCallBack;
    private ChattingThread chatThread = new ChattingThread();

    public ChattingConnection(Consumer<String> onReceiveCallBack) {
        this.onReceiveCallBack = onReceiveCallBack;
    }

    public void startConnection() {
        chatThread.start();
    }

    public void send(String data) throws IOException {
        chatThread.out.writeObject(stringSerde.serialize(data));
    }

    protected abstract boolean isServer();

    protected abstract String getIP();

    protected abstract int getPort();

    private class ChattingThread extends Thread {
        private Socket socket;
        private ObjectOutputStream out;
        /** */
        @Override
        public void run() {
            try {
                ServerSocket server =
                        isServer()
                                ? new ServerSocket(NetConstants.Network.CHAT_DEFAULT_PORT)
                                : null;
                Socket socket = isServer() ? server.accept() : new Socket(getIP(), getPort());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                this.socket = socket;
                this.out = out;
                while (true) {
                    String data = stringSerde.deserialize((String) in.readObject());
                    onReceiveCallBack.accept(data);
                }
            } catch (Exception e) {
                onReceiveCallBack.accept("Player has left the game");
            }
        }
    }
}
