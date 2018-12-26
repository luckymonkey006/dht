package com.github.fengleicn.dht.subsystem;

import com.github.fengleicn.dht.bencode.BencodeObject;
import com.github.fengleicn.dht.bencode.BencodeUtil;
import com.github.fengleicn.dht.packet.UdpPacket;

import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;

import static com.github.fengleicn.dht.utils.Utils.*;

public
/**
 * author Lei Feng
 */


class UdpNetworkContoller {
    public static FileWriter sendWriter;
    public static FileWriter recvWriter;

    static {
        try {
            recvWriter = new FileWriter("recv.txt", true);
            sendWriter = new FileWriter("send.txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    DatagramSocket datagramSocket;

    public UdpNetworkContoller(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public void send(UdpPacket udpPacket) throws IOException {
        BencodeObject bencodeObject = udpPacket.bencodeObject;
        byte[] sendBytes = BencodeUtil.toBencodeString(bencodeObject);
        printBytes(sendBytes);
        datagramSocket.send(new DatagramPacket(sendBytes, sendBytes.length, udpPacket.address));
    }

    public UdpPacket recv() throws Exception {
        byte[] buf = new byte[65536];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        datagramSocket.receive(packet);
        byte[] recvBytes = Arrays.copyOf(packet.getData(), packet.getLength());
        printBytes(recvBytes);
        BencodeObject bencodeObject;
        try {
            bencodeObject = BencodeUtil.parse(recvBytes);
        }catch (Exception e){
            printBytes(recvBytes);
            throw e;
        }
        return new UdpPacket((InetSocketAddress) packet.getSocketAddress(), bencodeObject);
    }

    public void printBytes(byte[] recvBytes) {
        System.err.println("Original: " + getOrignalBytesString(recvBytes));
        System.err.println("Masked:   " + getMaskedBytesString(recvBytes));
    }
}
