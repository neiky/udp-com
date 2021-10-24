package de.neiky.udp;

import java.io.IOException;

import org.junit.Test;

public class SendMsgToVirtuos {

    @Test
    public void test() throws IOException {
        String msg = "{\"L\":\"104\",\"C\":\"234\",\"VIRTUOS_EVT\":[{\"VAR_WRITE\":[{\"SENDER\":\"AM\"},{\"NAME\":\"MOTOR_1\"},{\"VALUE\":\"true\"}]}]}";

        try (UdpSender sender = new UdpSender("192.168.0.2", 11003)) {
            sender.setRemoteAddress("192.168.0.2", 11002).send(msg);
        }
    }

}
