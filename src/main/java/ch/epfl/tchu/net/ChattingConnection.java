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
        chatThread.setDaemon(true);
    }

    public void startConnection() {
        chatThread.start();
    }

    public void send(String data) throws IOException {
        chatThread.out.writeObject(stringSerde.serialize(data));
    }

    public void closeConnection() throws IOException {
        chatThread.socket.close();
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
            try (ServerSocket server = isServer() ? new ServerSocket(5108) : null;
                    Socket socket = isServer() ? server.accept() : new Socket(getIP(), getPort());
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                this.socket = socket;
                this.out = out;
                socket.setTcpNoDelay(true);
                while (true) {
                    String data = stringSerde.deserialize((String) in.readObject());
                    onReceiveCallBack.accept(data);
                }
            } catch (Exception e) {
                onReceiveCallBack.accept("connection closed");
            }
        }
    }
}
