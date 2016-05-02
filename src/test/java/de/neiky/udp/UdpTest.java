package de.neiky.udp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.FormattedMessage;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UdpTest {
	private static final String SENDER_HOST = "localhost";
	private static final int SENDER_PORT = 11002;
	private static final String RECEIVER_HOST = "127.0.0.1";
	private static final int RECEIVER_PORT = 11001;
	private static final String TESTMESSAGE = "Testmessage";

	private final Logger logger = LogManager.getLogger(UdpTest.class.getName());

	private UdpReceiver receiver;
	private String receivedMessage;

	@Before
	public void setup() throws SocketException, UnknownHostException {
		receivedMessage = null;
	}

	@After
	public void teardown() {
		if (receiver != null) {
			receiver.stop();
		}
	}

	@Test
	public void useStringAddress() throws UnknownHostException, IOException, InterruptedException {
		assertNull(receivedMessage);
		receiver = getUdpReceiver().start();

		UdpSenderBuilder senderBuilder = new UdpSenderBuilder();
		senderBuilder.setLocalAddress(SENDER_HOST).setLocalPort(SENDER_PORT);
		try (UdpSender sender = senderBuilder.build()) {
			sender.send(TESTMESSAGE, RECEIVER_HOST, RECEIVER_PORT);

			ThreadContext.put("name", "sender");
			logger.info("Message sent: {}", TESTMESSAGE);
		}

		// give the receiver some time to receive the message
		Thread.sleep(100);

		assertEquals(TESTMESSAGE, receivedMessage);
	}

	@Test
	public void useMessageHandler() throws UnknownHostException, IOException, InterruptedException {
		assertNull(receivedMessage);
		receiver = new UdpReceiver(RECEIVER_HOST, RECEIVER_PORT).setMessageHandler((address, port, message) -> {
			ThreadContext.put("name", "receiver");
			logger.info(new StringFormattedMessage("Message received from %s:%d:", address, port));
			logger.info(new StringFormattedMessage("> [%s]", message));

			setReceivedMessage(message);
		}).start();

		try (UdpSender sender = new UdpSender(SENDER_HOST, SENDER_PORT)) {
			sender.setRemoteAddress(RECEIVER_HOST).setRemotePort(RECEIVER_PORT).send(TESTMESSAGE, RECEIVER_HOST, RECEIVER_PORT);

			ThreadContext.put("name", "sender");
			logger.info("Message sent: {}", TESTMESSAGE);
		}

		// give the receiver some time to receive the message
		Thread.sleep(100);

		assertEquals(TESTMESSAGE, receivedMessage);
	}

	@Test
	public void useINetAddress() throws UnknownHostException, IOException, InterruptedException {
		assertNull(receivedMessage);
		receiver = getUdpReceiver().start();

		try (UdpSender sender = new UdpSender(InetAddress.getByName(SENDER_HOST), SENDER_PORT)) {
			sender.setRemoteAddress(InetAddress.getByName(RECEIVER_HOST)).setRemotePort(RECEIVER_PORT)
					.send(TESTMESSAGE, InetAddress.getByName(RECEIVER_HOST), RECEIVER_PORT);

			ThreadContext.put("name", "sender");
			logger.info("Message sent: {}", TESTMESSAGE);
		}

		// give the receiver some time to receive the message
		Thread.sleep(100);

		assertEquals(TESTMESSAGE, receivedMessage);
	}

	@Test
	public void shouldHandleEmptyMessage() throws UnknownHostException, IOException, InterruptedException {
		assertNull(receivedMessage);
		receiver = getUdpReceiver().start();

		try (UdpSender sender = new UdpSender(InetAddress.getByName(SENDER_HOST), SENDER_PORT)) {
			sender.send("", InetAddress.getByName(RECEIVER_HOST), RECEIVER_PORT);

			ThreadContext.put("name", "sender");
			logger.info("Empty message sent.");
		}

		// give the receiver some time to receive the message
		Thread.sleep(100);

		assertEquals("", receivedMessage);
	}

	@Test(expected = UnknownHostException.class)
	public void wrongSenderAddressThrowsException() throws UnknownHostException, IOException, InterruptedException {
		assertNull(receivedMessage);
		receiver = getUdpReceiver().start();

		UdpSenderBuilder senderBuilder = new UdpSenderBuilder().setLocalAddress(InetAddress.getByName("abc"))
				.setLocalPort(SENDER_PORT).setRemoteAddress(InetAddress.getByName(RECEIVER_HOST)).setRemotePort(RECEIVER_PORT);
		try (UdpSender sender = senderBuilder.build()) {
			sender.send(TESTMESSAGE);

			ThreadContext.put("name", "sender");
			logger.info("Message sent: {}", TESTMESSAGE);
		}

		// give the receiver some time to receive the message
		Thread.sleep(100);

		fail("Exception should have been thrown!");
	}

	@Test(expected = SocketException.class)
	public void receiverAddressMustBeGiven() throws UnknownHostException, IOException, InterruptedException {
		assertNull(receivedMessage);
		receiver = getUdpReceiver().start();

		try (UdpSender sender = new UdpSender(InetAddress.getByName(SENDER_HOST), SENDER_PORT)) {
			sender.setRemotePort(RECEIVER_PORT).send(TESTMESSAGE);

			ThreadContext.put("name", "sender");
			logger.info("Message sent: {}", TESTMESSAGE);
		}

		// give the receiver some time to receive the message
		Thread.sleep(100);

		fail("Exception should have been thrown!");
	}

	@Test(expected = IllegalArgumentException.class)
	public void messageMustNotBeNull() throws UnknownHostException, IOException, InterruptedException {
		assertNull(receivedMessage);
		receiver = getUdpReceiver().start();

		try (UdpSender sender = new UdpSender(InetAddress.getByName(SENDER_HOST), SENDER_PORT)) {
			sender.setRemoteAddress(RECEIVER_HOST).setRemotePort(RECEIVER_PORT).send((String) null);

			ThreadContext.put("name", "sender");
			logger.info("Message sent: {}", TESTMESSAGE);
		}

		// give the receiver some time to receive the message
		Thread.sleep(100);

		fail("Exception should have been thrown!");
	}

	@Test(expected = UnknownHostException.class)
	public void wrongReciverAddressThrowsException() throws UnknownHostException, IOException, InterruptedException {
		assertNull(receivedMessage);
		receiver = new UdpReceiver("abc", RECEIVER_PORT);

		// give the receiver some time to receive the message
		Thread.sleep(100);

		fail("Exception should have been thrown!");
	}

	private UdpReceiver getUdpReceiver() throws UnknownHostException {
		return new UdpReceiver(RECEIVER_HOST, RECEIVER_PORT).setPacketHandler((packet) -> {
			InetAddress address = packet.getAddress();
			int port = packet.getPort();
			int len = packet.getLength();
			byte[] data = packet.getData();

			String receivedMessage = new String(data, 0, len);

			ThreadContext.put("name", "receiver");
			logger.info(new StringFormattedMessage("Message received from %s:%d, length %d:", address, port, len));
			logger.info(new StringFormattedMessage("> [%s]", receivedMessage));

			setReceivedMessage(receivedMessage);
		});
	}

	private void setReceivedMessage(String message) {
		this.receivedMessage = message;
	}

}
