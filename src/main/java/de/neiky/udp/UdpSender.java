package de.neiky.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * A UdpSender class built to send UDP messages.
 * 
 * @author Michael Neike
 */
public class UdpSender implements AutoCloseable {
	private InetAddress address;
	private int port;
	private InetAddress remoteAddress;
	private int remotePort;
	private boolean broadcast = false;
	private DatagramSocket socket;

	/**
	 * Default constructor for UdpSender.
	 */
	public UdpSender() {

	}

	/**
	 * Parameterized contructor for UdpSender.
	 * 
	 * @param port
	 *            The local port of the socket.
	 */
	public UdpSender(int port) {
		this.port = port;
	}

	/**
	 * Parameterized contructor for UdpSender.
	 * 
	 * @param address
	 *            The local address of the socket (ip address, host name).
	 * @param port
	 *            The local port of the socket.
	 * @throws UnknownHostException
	 *             if no IP address for the host could be found, or if a
	 *             scope_id was specified for a global IPv6 address.
	 */
	public UdpSender(String address, int port) throws UnknownHostException {
		if (address != null && !address.isEmpty()) {
			this.address = InetAddress.getByName(address);
		}
		if (port > 0) {
			this.port = port;
		}
	}

	/**
	 * Parameterized contructor for UdpSender.
	 * 
	 * @param address
	 *            The local address of the socket.
	 * @param port
	 *            The local port of the socket.
	 */
	public UdpSender(InetAddress address, int port) {
		if (address != null) {
			this.address = address;
		}
		if (port > 0) {
			this.port = port;
		}
	}

	/**
	 * Sets the address and port of the recipient.
	 * 
	 * @param address
	 *            The address of the recipient (ip address, host name).
	 * @param port
	 *            The port of the recipient.
	 * @return this UdpSender.
	 * @throws UnknownHostException
	 */
	protected UdpSender setRemoteAddress(String address, int port) throws UnknownHostException {
		return this.setRemoteAddress(address).setRemotePort(port);
	}

	/**
	 * Sets the address of the recipient.
	 * 
	 * @param remoteAddress
	 *            The address of the recipient (ip address, host name).
	 * @return this UdpSender.
	 * @throws UnknownHostException
	 *             if no IP address for the host could be found, or if a
	 *             scope_id was specified for a global IPv6 address.
	 */
	protected UdpSender setRemoteAddress(String remoteAddress) throws UnknownHostException {
		this.remoteAddress = InetAddress.getByName(remoteAddress);

		return this;
	}

	/**
	 * Sets the address of the recipient.
	 * 
	 * @param remoteAddress
	 *            The address of the recipient.
	 * @return this UdpSender.
	 */
	protected UdpSender setRemoteAddress(InetAddress remoteAddress) {
		this.remoteAddress = remoteAddress;

		return this;
	}

	/**
	 * Sets the port of the recipient.
	 * 
	 * @param remotePort
	 *            The port of the recipient.
	 * @return this UdpSender.
	 */
	protected UdpSender setRemotePort(int remotePort) {
		this.remotePort = remotePort;

		return this;
	}

	/**
	 * Opens the socket on the local address and port, if given.<br />
	 * If no local address and/or port is given, the socket will be bound to the
	 * wildcard address, an IP address chosen by the kernel. <br />
	 * You may ignore this method as the socket will be opened implicitly when
	 * one of the send methods is called the first time. You can call this
	 * method explicitly to check if the socket can be bound.
	 * 
	 * @return this UdpSender.
	 * @throws SocketException
	 *             if the socket could not be opened, or the socket could not
	 *             bind to the specified local port.
	 */
	public synchronized UdpSender openSocket() throws SocketException {
		if (port > 0) {
			if (address != null) {
				this.socket = new DatagramSocket(port, address);
			} else {
				this.socket = new DatagramSocket(port);
			}
		} else {
			this.socket = new DatagramSocket();
		}

		return this;
	}

	/**
	 * Sends a UDP message.
	 * 
	 * @param message
	 *            The message to be sent.
	 * @return this UdpSender.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public UdpSender send(String message) throws IOException {
		return send(message, this.remoteAddress, this.port);
	}

	/**
	 * 
	 * @param message
	 *            The message to be sent.
	 * @param remoteAddress
	 *            The address of the destination.
	 * @param remotePort
	 *            The port of the destination.
	 * @return this UdpSender
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public UdpSender send(String message, InetAddress remoteAddress, int remotePort) throws IOException {
		if (message == null) {
			throw new IllegalArgumentException("Given message must not be NULL.");
		}
		byte[] messageBytes = message.getBytes("UTF-8");
		return send(messageBytes, remoteAddress, remotePort);
	}

	public UdpSender send(String message, String remoteAddress, int remotePort)
			throws UnknownHostException, IOException {
		return send(message, InetAddress.getByName(remoteAddress), remotePort);
	}

	/**
	 * Sends a UDP message.
	 * 
	 * @param bytes
	 *            The message to be sent as byte array.
	 * @return this UdpSender.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public UdpSender send(byte[] bytes) throws IOException {
		return send(bytes, this.remoteAddress, this.remotePort);
	}

	/**
	 * Sends a UDP message.
	 * 
	 * @param bytes
	 *            The byte array to be sent.
	 * @param remoteAddress
	 *            The address of the destination.
	 * @param port
	 *            The port of the destination.
	 * @return This UdpSender.
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public synchronized UdpSender send(byte[] bytes, InetAddress remoteAddress, int remotePort) throws IOException {
		if (remoteAddress == null) {
			throw new SocketException("No receiver address given.");
		}

		if (remotePort <= 0) {
			throw new SocketException("No receiver port given.");
		}

		if (bytes == null) {
			throw new IllegalArgumentException("Given message must not be NULL.");
		}

		if (socket == null) {
			openSocket();
		}

		DatagramPacket packet = new DatagramPacket(bytes, bytes.length, remoteAddress, remotePort);

		this.socket.setBroadcast(broadcast);
		this.socket.send(packet);

		return this;
	}

	public UdpSender send(byte[] bytes, String remoteAddress, int remotePort) throws UnknownHostException, IOException {
		return send(bytes, InetAddress.getByName(remoteAddress), remotePort);
	}

	/**
	 * Enable/disable SO_BROADCAST.
	 * 
	 * @param on
	 *            whether or not to have broadcast turned on.
	 * @return this UdpSender.
	 */
	protected UdpSender setBroadcast(boolean on) {
		this.broadcast = on;

		return this;
	}

	/**
	 * Gets the local address to which the socket is bound. If there is a
	 * security manager, its checkConnect method is first called with the host
	 * address and -1 as its arguments to see if the operation is allowed.
	 * 
	 * @return the local address to which the socket is bound.
	 */
	public InetAddress getLocalAddress() {
		return socket.getLocalAddress();
	}

	/**
	 * Returns the port number on the local host to which this socket is bound.
	 * 
	 * @return the port number on the local host to which this socket is bound,
	 *         -1 if the socket is closed, or 0 if it is not bound yet.
	 */
	public int getLocalPort() {
		return socket.getLocalPort();
	}

	/**
	 * Closes the socket of the UdpSender.
	 */
	@Override
	public void close() {
		if (socket != null) {
			socket.close();
		}
	}
}
