package com.github.fengleicn.dht.utils;

import com.github.fengleicn.dht.bencode.BencodeObject;
import com.github.fengleicn.dht.bencode.BencodeHashMap;
import com.github.fengleicn.dht.node.KBucketNode;
import com.github.fengleicn.dht.packet.UdpPacket;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

import static com.github.fengleicn.dht.bencode.BencodeObject.UNICODE_UTF8;

public class Utils {

    public static final Random random = new Random();

    public static byte[] getBytesFromHost(String host) throws UnknownHostException {
        return InetAddress.getByName(host).getAddress();
    }

    public static String getHostIpFromBytes(byte[] bytes) {
        return (bytes[0] & 0xFF) + "." + (bytes[1] & 0xFF) + "."
                + (bytes[2] & 0xFF) + "." + (bytes[3] & 0xFF);
    }

    public static byte[] getBytesFromInt(int port) {
        return new byte[]{(byte) (port / 256), (byte) (port % 256)};
    }

    public static String getIntFromBytes(byte[] bytes) {
        if (bytes.length == 2)
            return ((bytes[0] & 0xFF) << 8) + (bytes[1] & 0xFF) + "";
        else
            return ((bytes[4] & 0xFF) << 8) + (bytes[5] & 0xFF) + "";
    }

    public static byte[] getBytesFromHex(String hex) {
        return DatatypeConverter.parseHexBinary(hex);
    }

    public static List<KBucketNode> getNodesFromBytes(byte[] buf) throws Exception {
        int len = buf.length;
        int ptr = 0, nodeLen = 26;
        List<KBucketNode> list = new ArrayList<>();
        if (len % nodeLen != 0) {
            throw new Exception("[ERROR] nodes-id's length isn't 26*N");
        }
        while (ptr < buf.length) {
            list.add(new KBucketNode(Arrays.copyOfRange(buf, ptr, ptr + nodeLen)));
            ptr += nodeLen;
        }
        return list;
    }

    public static byte[] getBytesFromNodes(List<KBucketNode> KBucketNodes) {
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

    public static byte[] getBytesFromChar(char character) {
        return Character.toString(character).getBytes(UNICODE_UTF8);
    }

    public static String getOrignalBytesString(byte[] bytes){
        byte[] copy = bytes.clone();
        for (int i = 0; i < copy.length; i++) {
            byte b = copy[i];
            b = b >= ' ' && b <= '~' ? b : (byte) '.';
            if(b == '\n' || b == '\t' || b == '\r' || b == '\f'){
                b = '.';
            }
            copy[i] = b;
        }
        return new String(copy);
    }

    public static String getMaskedBytesString(byte[] bytes){
        byte[] copy = bytes.clone();
        for (int i = 0; i < copy.length; i++) {
            byte b = copy[i];
            b = b >= ' ' && b <= '~' ? (byte) ' ' : (byte) '*' ;
            if(b == '\n' || b == '\t' || b == '\r' || b == '\f'){
                b = '*';
            }
            copy[i] = b;
        }
        return new String(copy);
    }

    public static byte[] bytesConcat(byte[]... bytesArg) {
        byte[] ret = {};
        for (byte[] i : bytesArg) {
            ret = bytesConcat0(ret, i);
        }
        return ret;
    }

    public static byte[] bytesConcat0(byte[] head, byte[] tail) {
        byte[] result = new byte[head.length + tail.length];
        System.arraycopy(head, 0, result, 0, head.length);
        System.arraycopy(tail, 0, result, head.length, tail.length);
        return result;
    }

    public static boolean isBytesEqual(byte[] bytes1, byte[] bytes2) {
        if (bytes1 == null || bytes2 == null || bytes1.length != bytes2.length)
            return false;
        for (int i = 0; i < bytes1.length; i++) {
            if (bytes1[i] != bytes2[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] randomBytes(int len) {
        byte[] b = new byte[len];
        random.nextBytes(b);
        return b;
    }

    // Request: ping, find_node, get_peer, announce_peer
    public static UdpPacket ping(byte[] transId, byte[] nodeId, InetSocketAddress socketAddress) throws UnsupportedEncodingException {
        BencodeObject bencodeObject = new BencodeObject(new BencodeHashMap() {{
            put("t", new BencodeObject(transId));
            put("y", new BencodeObject("q"));
            put("q", new BencodeObject("ping"));
            put("a", new BencodeObject(new BencodeHashMap() {{
                put("id", new BencodeObject(nodeId));
            }}));
        }});
        return new UdpPacket(socketAddress, bencodeObject);
    }

    public static UdpPacket findNode(byte[] transId, byte[] nodeId, byte[] target, InetSocketAddress socketAddress) throws UnsupportedEncodingException {
        BencodeObject bencodeObject = new BencodeObject(new BencodeHashMap() {{
            put("t", new BencodeObject(transId));
            put("y", new BencodeObject("q"));
            put("q", new BencodeObject("find_node"));
            put("a", new BencodeObject(new BencodeHashMap() {{
                put("id", new BencodeObject(nodeId));
                put("target", new BencodeObject(target));
            }}));
        }});
        return new UdpPacket(socketAddress, bencodeObject);
    }

    public static UdpPacket getPeer(byte[] transId, byte[] nodeId, byte[] infoHash, InetSocketAddress socketAddress) throws UnsupportedEncodingException {
        BencodeObject bencodeObject = new BencodeObject(new BencodeHashMap() {{
            put("t", new BencodeObject(transId));
            put("y", new BencodeObject("q"));
            put("q", new BencodeObject("get_peers"));
            put("a", new BencodeObject(new BencodeHashMap() {{
                put("id", new BencodeObject(nodeId));
                put("info_hash", new BencodeObject(infoHash));
            }}));
        }});
        return new UdpPacket(socketAddress, bencodeObject);
    }

    public static UdpPacket announcePeer(byte[] transId, byte[] nodeId, byte[] infoHash, int port, byte[] token, InetSocketAddress socketAddress) throws UnsupportedEncodingException {
        BencodeObject bencodeObject = new BencodeObject(new BencodeHashMap() {{
            put("t", new BencodeObject(transId));
            put("y", new BencodeObject("q"));
            put("q", new BencodeObject("announce_peer"));
            put("a", new BencodeObject(new BencodeHashMap() {{
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
        BencodeObject bencodeObject = new BencodeObject(new BencodeHashMap() {{
            put("t", new BencodeObject(transId));
            put("y", new BencodeObject("r".getBytes()));
            put("r", new BencodeObject(new BencodeHashMap() {{
                put("id", new BencodeObject(nodeId));
            }}));
        }});
        return new UdpPacket(socketAddress, bencodeObject);
    }

    public static UdpPacket rspFindNode(byte[] transId, byte[] nodeId, List<KBucketNode> KBucketNodes, InetSocketAddress socketAddress) throws UnsupportedEncodingException {
        BencodeObject bencodeObject = new BencodeObject(new BencodeHashMap() {{
            put("t", new BencodeObject(transId));
            put("y", new BencodeObject("r"));
            put("r", new BencodeObject(new BencodeHashMap() {{
                put("id", new BencodeObject(nodeId));
                put("nodes", new BencodeObject(getBytesFromNodes(KBucketNodes)));
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
