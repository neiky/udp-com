package de.neiky.udp;

import java.io.IOException;
import java.net.InetAddress;

import org.junit.Test;

public class SendAndReceiveMsgFromVirtuos
{
  
  @Test
  public void test () throws InterruptedException, IOException
  {
    UdpReceiver receiver = new UdpReceiver ("192.168.0.2", 11001)
        .setPacketHandler ( (packet) -> {
          // Empfänger auslesen
          
          InetAddress address = packet.getAddress ();
          int port = packet.getPort ();
          int len = packet.getLength ();
          byte [] data = packet.getData ();
          
          System.out.printf ("Message from %s:%d - length %d:%n%s%n", address,
              port, len, new String (data, 0, len));
        }).start ();
        
    String msg = "{\"L\":\"104\",\"C\":\"234\",\"VIRTUOS_EVT\":[{\"VAR_WRITE\":[{\"SENDER\":\"AM\"},{\"NAME\":\"MOTOR_1\"},{\"VALUE\":\"true\"}]}]}";
    
    try (
        UdpSender sender = new UdpSender ("192.168.0.2", 11003))
    {
      sender.setRemoteAddress ("192.168.0.2", 11002);
      int i = 0;
      while (i < 10)
      {
        Thread.sleep (5000);
        sender.send (msg);
        i++;
      }
    }
    
    Thread.sleep (120000);
    
    receiver.stop ();
  }
  
}
