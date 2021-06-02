package ch.epfl.tchu.net;

import java.util.function.Consumer;

public class ChattingClient extends ChattingConnection {

    private String ip;
    private int port;

    public ChattingClient(String ip, int port, Consumer<String> onReceiveCallBack) {
        super(onReceiveCallBack);
        this.ip = ip;
        this.port = port;
    }

    @Override
    protected boolean isServer() {
        return false;
    }

    @Override
    protected String getIP() {
        return ip;
    }

    @Override
    protected int getPort() {
        return port;
    }
}