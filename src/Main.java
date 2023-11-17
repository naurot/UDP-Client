import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
        int sPort = 0;
        InetAddress sAddr = null;
        byte[] receiveBuffer = new byte[1029];
        byte[] resTmp;

        Scanner cin = new Scanner(System.in);
        System.out.print("Enter a url: ");
        String url = cin.nextLine();
        StringBuilder webPage = new StringBuilder();

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
        String tab = "";
        do {
            System.out.println(tab + "Waiting for response");
            datagramSocket.receive(response);
            if (initial) {
                initial = false;
                sPort = response.getPort();
                sAddr = response.getAddress();
            }

            resTmp = response.getData();
            System.out.println(tab + "\tReceived packet. Seq#" + resTmp[0]);
            System.out.println(tab + "\tExpected seq#" + tmp1);
            if (tmp1 == resTmp[0]) {
                length[0] = resTmp[1];
                length[1] = resTmp[2];
                length[2] = resTmp[3];
                length[3] = resTmp[4];
                packetLength = new BigInteger(length).intValue();
//                System.out.println("\tPacket length: " + packetLength);
                byte[] packet = new byte[1024];
                if (packetLength - 5 >= 0) System.arraycopy(resTmp, 5, packet, 0, packetLength - 5);
                webPage.append(new String(packet).substring(0,packetLength - 5));
                request = new DatagramPacket(getAck((byte) tmp1), 5, sAddr, sPort);
                System.out.println(tab + "\tSending ack" + tmp1);
                datagramSocket.send(request);
                tmp1 = tmp1 == 0 ? 1 : 0;
                tab = "\t";
            } else {
                System.out.println(tab + "\t***** wrong seq#");
                int tmp2 = tmp1 == 0 ? 1 : 0;
                System.out.println(tab + "\tSending ack" + tmp2);
                datagramSocket.send(new DatagramPacket(getAck((byte) tmp2), 5, sAddr, sPort));
                tab = tab + "\t";
            }
        } while (packetLength > 1028);
        FileWriter fout = new FileWriter(("something.html"));
        fout.write(webPage.toString());
        fout.close();

        fComp("./something.html", "../Server/something1.html");
    }

    private static void fComp(String s, String s1) {
        System.out.println("in File Compare");
        int index = 0;
        Scanner fin1 = null, fin2 = null;
        try {
            fin1 = new Scanner(new File(s));
            fin2 = new Scanner(new File(s1));
            String b1, b2;
            while (fin1.hasNext() && fin2.hasNext()) {
                b1 = fin1.next();
                b2 = fin2.next();
                if (!b1.equals(b2)) {
                    System.out.println("diff: index= " + index + ", b1= " + b1 + ", b2= " + b2);
                }
                index++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("error: " + e);
        } finally {
            if (fin1.hasNext() ^ fin2.hasNext())
                System.out.println("Error: files have different sizes");
            System.out.println("Leaving File Compare: index= " + index); //making certain something was compared
        }
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
