package de.neiky.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * A UdpReceiver class built to receive UDP messages.
 *
 * @author Michael Neike
 */
public class UdpReceiver implements Runnable {
    private final int port;
    private InetAddress address;
    private Thread receiverThread;
    private DatagramSocket socket;
    private PacketHandler packetHandler;
    private MessageHandler messageHandler;

    /**
     * Constructor for UdpReceiver. After constructing use
     * {@link #setPacketHandler(PacketHandler)} to assign a packet handler to
     * the receiver. Then start listening using {@link #start()}.
     *
     * @param port The port on which the receiver will listen.
     */
    public UdpReceiver(int port) {
        this.port = port;
    }

    public UdpReceiver(String address, int port) throws UnknownHostException {
        this.port = port;
        this.address = InetAddress.getByName(address);
    }

    /**
     * Starts listening on the port given in the constructor.
     *
     * @return this UdpReceiver
     * @throws SocketException if the socket could not be opened, or the socket could not
     *                         bind to the specified local port.
     */
    public UdpReceiver start() throws SocketException {
        socket = new DatagramSocket(this.port, this.address);
        receiverThread = new Thread(this);
        receiverThread.start();

        return this;
    }

    /**
     * Sets a packet handler. {@link PacketHandler} is a functional interface
     * providing the function {@link PacketHandler#handlePacket(DatagramPacket)}
     * .
     *
     * @param packetHandler
     * @return this UdpReceiver
     */
    public UdpReceiver setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;

        return this;
    }

    /**
     * Sets a message handler. {@link MessageHandler} is a functional interface
     * providing the function {@link MessageHandler#handleMessage(InetAddress, int, String)}
     * .
     *
     * @param messageHandler
     * @return this UdpReceiver
     */
    public UdpReceiver setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;

        return this;
    }

    /**
     * Stop listening for messages.
     *
     */
    public void stop() {
        receiverThread.interrupt();
        socket.close();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

            try {
                // wait for next packet
                // the thread is blocking while waiting...
                socket.receive(packet);

                if (this.packetHandler != null) {
                    this.packetHandler.handlePacket(packet);
                }
                if (this.messageHandler != null) {
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    int len = packet.getLength();
                    byte[] data = packet.getData();

                    String receivedMessage = new String(data, 0, len);

                    this.messageHandler.handleMessage(address, port, receivedMessage);
                }
                if (this.packetHandler == null && this.messageHandler == null) {
                    System.err.println("Neither OnPacketReceive nor OnMessageReceive have been set!");
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Functional interface to handle a {@link DatagramPacket}, that are
     * received by the {@link UdpReceiver}.
     *
     * @author Michael Neike
     */
    @FunctionalInterface
    interface PacketHandler {
        /**
         * Handle the received {@link DatagramPacket}.
         *
         * @param packet
         */
        void handlePacket(DatagramPacket packet);
    }

    /**
     * Functional interface to handle a {@link DatagramPacket}, that are
     * received by the {@link UdpReceiver}.
     *
     * @author Michael Neike
     */
    @FunctionalInterface
    interface MessageHandler {
        /**
         * Handle the received {@link DatagramPacket}.
         *
         * @param senderAddress The remote address of the sender.
         * @param senderPort    The remote port of the sender.
         * @param message       The message.
         */
        void handleMessage(InetAddress senderAddress, int senderPort, String message);
    }
}
