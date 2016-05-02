package de.neiky.udp.clisender;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Scanner;

import org.apache.commons.lang3.ArrayUtils;

import de.neiky.udp.UdpSender;
import de.neiky.udp.UdpSenderBuilder;

public class CliUdpSender {
	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);

		System.out.print("Local ip [localhost]:\t");
		String localIp = scanner.next();

		System.out.print("Local port [random]:\t");
		int localPort = scanner.nextInt();

		System.out.print("Remote ip:\t");
		String remoteIp = scanner.next();
		System.out.print("Remote port:\t");
		int remotePort = scanner.nextInt();

		// String ip = "192.168.0.2";
		// int port = 11003;
		UdpSenderBuilder builder = new UdpSenderBuilder().setLocalAddress(InetAddress.getByName(localIp))
				.setLocalPort(localPort).setRemoteAddress(InetAddress.getByName(remoteIp)).setRemotePort(remotePort);
		try (UdpSender sender = builder.build()) {
			// ip = "192.168.0.2";
			// port = 11002;

			System.out.println("Message (type 'exit' to quit)");
			System.out.print("> ");

			String msg = scanner.next();
			while (!msg.equals("exit")) {
				// calculate message length
				int l = msg.length();
				byte b1 = (byte) (l >> 24 & 0xFF);
				byte b2 = (byte) (l >> 16 & 0xFF);
				byte b3 = (byte) (l >> 8 & 0xFF);
				byte b4 = (byte) (l & 0xFF);

				byte[] buff = null;
				try {
					buff = msg.getBytes("UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				byte[] res = ArrayUtils.addAll(new byte[] { b1, b2, b3, b4 }, buff);

				System.out.println("Sending: " + ArrayUtils.toString(res));
				sender.send(res);
				System.out.print("> ");
				msg = scanner.next();
			}
			System.out.println("Bye bye!");
		}

		scanner.close();
	}
}
