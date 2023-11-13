import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Scanner;

import static java.net.InetAddress.getLocalHost;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 11122;
        int sPort =0;
        InetAddress sAddr = null;
        byte[] receiveBuffer = new byte[1029];
        byte[] resTmp;

        Scanner cin = new Scanner(System.in);
        System.out.print("Enter a url: ");
        String url = cin.nextLine();

        int packetLength = url.length() + 5;
        byte[] tmp = new byte[packetLength];
        byte[] length = ByteBuffer.allocate(4).putInt(packetLength).array();
        byte seq = 0;
        byte[] msg = url.getBytes();
        tmp[0] = seq;
        tmp[1] = length[0];
        tmp[2] = length[1];
        tmp[3] = length[2];
        tmp[4] = length[3];
        System.arraycopy(msg, 0, tmp, 5, url.length());

        DatagramPacket request = new DatagramPacket(tmp, packetLength, getLocalHost(), port);
        DatagramPacket response = new DatagramPacket(receiveBuffer, 1029);
        DatagramSocket datagramSocket = new DatagramSocket();

        datagramSocket.send(request);
        System.out.println("Sent message: " + url);
        boolean initial = true;
        int tmp1 = 0;
        do {
            System.out.println("Waiting for response");
            datagramSocket.receive(response);
            if (initial) {
                initial = false;
                sPort = response.getPort();
                sAddr = response.getAddress();
            }

            resTmp = response.getData();
            System.out.println("\tReceived packet. Seq#" + resTmp[0]);
            System.out.println("\tExpected seq#" + tmp1);
            if (tmp1 == resTmp[0]) {
                length[0] = resTmp[1];
                length[1] = resTmp[2];
                length[2] = resTmp[3];
                length[3] = resTmp[4];
                packetLength = new BigInteger(length).intValue();
                System.out.println("\tPacket length: " + packetLength);
                byte[] packet = new byte[1024];
                if (packetLength - 5 >= 0) System.arraycopy(resTmp, 5, packet, 0, packetLength - 5);
                request = new DatagramPacket(getAck((byte) tmp1), 5, sAddr, sPort);
                System.out.println("\tSending ack" + tmp1);
                datagramSocket.send(request);
                tmp1 = tmp1 ^ 1;
            } else {
                System.out.println("\t***** wrong seq#");
                int tmp2 = tmp1 ^ 1;
                System.out.println("\tSending ack" + tmp2);
                datagramSocket.send(new DatagramPacket(getAck((byte) tmp2), 5, sAddr, sPort));
            }
        } while (packetLength > 1028);
    }

    public static byte[] getAck(byte ack) {
        byte[] retValue = new byte[5];
        retValue[0] = ack;
        retValue[1] = 0;
        retValue[2] = 0;
        retValue[3] = 0;
        retValue[4] = 0;
        return retValue;

    }
}
