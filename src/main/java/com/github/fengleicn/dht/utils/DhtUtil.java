package com.github.fengleicn.dht.utils;

import com.github.fengleicn.dht.bencode.BencodeObject;
import com.github.fengleicn.dht.bencode.BcdHashMap;
import com.github.fengleicn.dht.node.KBucketNode;
import com.github.fengleicn.dht.packet.UdpPacket;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

public class DhtUtil {

    public static final Random random = new Random();

    public static byte[] hostToByte(String host) throws UnknownHostException {
        return InetAddress.getByName(host).getAddress();
    }

    public static byte[] portToByte(int port){
        return new byte[]{(byte) (port / 256), (byte) (port % 256)};
    }

    public static String byteToIp(byte[] buf) {
        return (buf[0] & 0xFF) + "." + (buf[1] & 0xFF) + "."
                + (buf[2] & 0xFF) + "." + (buf[3] & 0xFF);
    }

    public static String byteToPort(byte[] buf) {
        if (buf.length == 2)
            return ((buf[0] & 0xFF) << 8) + (buf[1] & 0xFF) + "";
        else
            return ((buf[4] & 0xFF) << 8) + (buf[5] & 0xFF) + "";
    }

    public static String ByteToHexStr(byte[] Hex) {
        return DatatypeConverter.printHexBinary(Hex);
    }

    public static byte[] hexToByteArray(String hex) {
        return DatatypeConverter.parseHexBinary(hex);
    }

    public static boolean byteArraysEqual(byte[] a, byte[] b){
        if(a == null || b == null || a.length != b.length)
            return false;
        for(int i = 0; i < a.length; i++){
            if(a[i] != b[i])
                return false;
        }
        return true;
    }

    public static byte[] randomByteArray(int len) {
        byte[] b = new byte[len];
        random.nextBytes(b);
        return b;
    }

    public static List<KBucketNode> decodeNodes(byte[] buf) throws Exception {
        int len = buf.length;
        int ptr = 0, nodeLen = 26;
        List<KBucketNode> list = new ArrayList<>();
        if (len % nodeLen != 0) {
            throw new Exception("node length is wrong");
        }
        while (ptr < buf.length) {
            list.add(new KBucketNode(Arrays.copyOfRange(buf, ptr, ptr + nodeLen)));
            ptr += nodeLen;
        }
        return list;
    }

    public static byte[] encodeNodes(List<KBucketNode> KBucketNodes) {
        if (KBucketNodes == null || KBucketNodes.size() == 0)
            return null;
        int ptr = 0, nodeLen = 26;
        byte[] ret = new byte[KBucketNodes.size() * nodeLen];
        for (KBucketNode KBucketNode : KBucketNodes) {
            byte[] buf = new byte[nodeLen];
            System.arraycopy(KBucketNode.nodeId, 0, buf, 0, 20);
            System.arraycopy(KBucketNode.ip, 0, buf, 20, 4);
            System.arraycopy(KBucketNode.port, 0, buf, 24, 2);
            System.arraycopy(buf, 0, ret, ptr, nodeLen);
            ptr += 26;
        }
        return ret;
    }

    // Request: ping, find_node, get_peer, announce_peer
    public static UdpPacket ping(byte[] transId, byte[] nodeId, InetSocketAddress socketAddress) throws UnsupportedEncodingException {
        BencodeObject bencodeObject = new BencodeObject(new BcdHashMap() {{
            put("t", new BencodeObject(transId));
            put("y", new BencodeObject("q"));
            put("q", new BencodeObject("ping"));
            put("a", new BencodeObject(new BcdHashMap() {{
                put("id", new BencodeObject(nodeId));
            }}));
        }});
        return new UdpPacket(socketAddress, bencodeObject);
    }

    public static UdpPacket findNode(byte[] transId, byte[] nodeId, byte[] target, InetSocketAddress socketAddress) throws UnsupportedEncodingException {
        BencodeObject bencodeObject = new BencodeObject(new BcdHashMap() {{
            put("t", new BencodeObject(transId));
            put("y", new BencodeObject("q"));
            put("q", new BencodeObject("find_node"));
            put("a", new BencodeObject(new BcdHashMap() {{
                put("id", new BencodeObject(nodeId));
                put("target", new BencodeObject(target));
            }}));
        }});
        return new UdpPacket(socketAddress, bencodeObject);
    }

    public static UdpPacket getPeer(byte[] transId, byte[] nodeId, byte[] infoHash, InetSocketAddress socketAddress) throws UnsupportedEncodingException {
        BencodeObject bencodeObject = new BencodeObject(new BcdHashMap() {{
            put("t", new BencodeObject(transId));
            put("y", new BencodeObject("q"));
            put("q", new BencodeObject("get_peers"));
            put("a", new BencodeObject(new BcdHashMap() {{
                put("id", new BencodeObject(nodeId));
                put("info_hash", new BencodeObject(infoHash));
            }}));
        }});
        return new UdpPacket(socketAddress, bencodeObject);
    }

    public static UdpPacket announcePeer(byte[] transId, byte[] nodeId, byte[] infoHash, int port, byte[] token, InetSocketAddress socketAddress) throws UnsupportedEncodingException {
        BencodeObject bencodeObject = new BencodeObject(new BcdHashMap() {{
            put("t", new BencodeObject(transId));
            put("y", new BencodeObject("q"));
            put("q", new BencodeObject("announce_peer"));
            put("a", new BencodeObject(new BcdHashMap() {{
                put("id", new BencodeObject(nodeId));
                put("implied_port", new BencodeObject(0));
                put("info_hash", new BencodeObject(infoHash));
                put("port", new BencodeObject(port));
                put("token", new BencodeObject(token));
            }}));
        }});
        return new UdpPacket(socketAddress, bencodeObject);

    }

    // Response: ping, find_node ...
    public static UdpPacket rspPing(byte[] transId, byte[] nodeId, InetSocketAddress socketAddress) {
        BencodeObject bencodeObject = new BencodeObject(new BcdHashMap() {{
            put("t", new BencodeObject(transId));
            put("y", new BencodeObject("r".getBytes()));
            put("r", new BencodeObject(new BcdHashMap() {{
                put("id", new BencodeObject(nodeId));
            }}));
        }});
        return new UdpPacket(socketAddress, bencodeObject);
    }

    public static UdpPacket rspFindNode(byte[] transId, byte[] nodeId, List<KBucketNode> KBucketNodes, InetSocketAddress socketAddress) throws UnsupportedEncodingException {
        BencodeObject bencodeObject = new BencodeObject(new BcdHashMap() {{
            put("t", new BencodeObject(transId));
            put("y", new BencodeObject("r"));
            put("r", new BencodeObject(new BcdHashMap() {{
                put("id", new BencodeObject(nodeId));
                put("KBucketNodes", new BencodeObject(encodeNodes(KBucketNodes)));
            }}));
        }});
        return new UdpPacket(socketAddress, bencodeObject);
    }

    public static UdpPacket rspGetPeerNotFound() throws UnsupportedEncodingException {
        return null;
    }

    public static UdpPacket rspAnnouncePeer(byte[] transId, byte[] nodeId, InetSocketAddress socketAddress) {
        return rspPing(transId, nodeId, socketAddress);
    }
}
