package ch.epfl.tchu.game;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class Client {
DatagramPacket receivePacket;
    public static void main(String[] args) {

        Client client = new Client();
        client.connect();

    }

    public void connect() {

        try {
            DatagramSocket c;
            c = new DatagramSocket();
            c.setBroadcast(true);
            byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();

            try {

                InetAddress ip = InetAddress.getByName("255.255.255.255");
                DatagramPacket packet = new DatagramPacket(sendData, sendData.length, ip, 3000);
                c.send(packet);
                System.out.println(getClass().getName() + ": Request packet sent to: 255.255.255.255 (DEFAULT)");

            } catch (IOException e) {
                e.printStackTrace();

            }

            // Broadcast the message over all the network interfaces
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

//                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
//                    continue; // Don't want to broadcast to the loopback interface
//                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 3000);
                        c.send(sendPacket);

                    } catch (Exception e) {

                    }

                    System.out.println(getClass().getName() + ": Request packet sent to: " + broadcast.getHostAddress());
                }
            }

            System.out.println(getClass().getName() + ": Done looping over all network interfaces. Now waiting for a reply!");

            //Wait for a response
            byte[] recvBuf = new byte[5];
            receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            c.receive(receivePacket);

            //We have a response
            System.out.println(getClass().getName() + ": Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

                //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
                System.out.println("Server's IP: " + receivePacket.getAddress());


            //Close the port!
            c.close();
        } catch (IOException ex) {

        }
    }

    public DatagramPacket getReceivePacket() {
        return receivePacket;
    }
}
