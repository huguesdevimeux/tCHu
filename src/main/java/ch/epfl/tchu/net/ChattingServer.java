package ch.epfl.tchu.net;

import java.util.function.Consumer;

public class ChattingServer extends ChattingConnection {
    private int port;

    public ChattingServer(int port, Consumer<String> onReceiveCallBack) {
        super(onReceiveCallBack);
        this.port = port;
    }

    @Override
    protected boolean isServer() {
        return true;
    }

    @Override
    protected String getIP() {
        return null;
    }

    @Override
    protected int getPort() {
        return port;
    }
}
