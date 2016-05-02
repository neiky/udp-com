package de.neiky.udp;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class UdpSenderBuilder {
	private InetAddress address;
	private int port;
	private InetAddress remoteAddress;
	private int remotePort;
	private boolean broadcast = false;

	public UdpSenderBuilder() {

	}

	public UdpSenderBuilder setLocalAddress(InetAddress localAddress) {
		this.address = localAddress;

		return this;
	}

	public UdpSenderBuilder setLocalAddress(String localAddress) throws UnknownHostException {
		this.address = InetAddress.getByName(localAddress);

		return this;
	}

	public UdpSenderBuilder setLocalPort(int port) {
		this.port = port;

		return this;
	}

	public UdpSenderBuilder setRemoteAddress(InetAddress remoteAddress) {
		this.remoteAddress = remoteAddress;

		return this;
	}

	public UdpSenderBuilder setRemoteAddress(String remoteAddress) throws UnknownHostException {
		this.remoteAddress = InetAddress.getByName(remoteAddress);

		return this;
	}

	public UdpSenderBuilder setRemotePort(int remotePort) {
		this.remotePort = remotePort;

		return this;
	}

	public UdpSenderBuilder setBroadcast(boolean on) {
		this.broadcast = on;

		return this;
	}

	public UdpSender build() {
		UdpSender udpSender = new UdpSender(address, port);
		udpSender.setRemoteAddress(remoteAddress);
		udpSender.setRemotePort(remotePort);
		udpSender.setBroadcast(broadcast);

		return udpSender;
	}
}
