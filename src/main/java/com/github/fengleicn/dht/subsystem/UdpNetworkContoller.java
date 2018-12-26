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
import java.util.Date;

import static com.github.fengleicn.dht.utils.Utils.*;

public
/**
 * author Lei Feng
 */


class UdpNetworkContoller {
    public static FileWriter log;

    static {
        try {
            log = new FileWriter("log.txt", true);
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
        datagramSocket.send(new DatagramPacket(sendBytes, sendBytes.length, udpPacket.address));
        addLog("SEND", sendBytes, udpPacket.address.getHostName(), udpPacket.address.getPort() + "");
    }

    public UdpPacket recv() throws Exception {
        byte[] buf = new byte[65536];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        datagramSocket.receive(packet);
        byte[] recvBytes = Arrays.copyOf(packet.getData(), packet.getLength());
        addLog("RECV", recvBytes, packet.getAddress().getHostAddress(), packet.getPort() + "");
        BencodeObject bencodeObject;
        try {
            bencodeObject = BencodeUtil.parse(recvBytes);
        } catch (Exception e) {
            printBytes(recvBytes);
            throw e;
        }
        return new UdpPacket((InetSocketAddress) packet.getSocketAddress(), bencodeObject);
    }

    public void printBytes(byte[] bytes) {
        System.err.println("Original: " + getOrignalBytesString(bytes));
        System.err.println("Masked:   " + getMaskedBytesString(bytes));
    }

    public void addLog(String title, byte[] bytes, String ip, String port) throws IOException {
        log.append("[").append(title).append("]  ").append(new Date().toString()).append("  ");
        log.append(ip);
        log.append(":").append(port);
        log.append("\n");
        log.append("        ").append(getOrignalBytesString(bytes));
        log.append("\n");
        log.flush();
    }
}
