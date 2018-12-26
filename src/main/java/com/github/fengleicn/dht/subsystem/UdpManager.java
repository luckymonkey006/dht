package com.github.fengleicn.dht.subsystem;

import com.github.fengleicn.dht.bencode.BencodeObject;
import com.github.fengleicn.dht.node.KBucketNode;
import com.github.fengleicn.dht.packet.UdpPacket;
import com.github.fengleicn.dht.utils.KBucket;
import com.github.fengleicn.dht.utils.Utils;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UdpManager {
    public static Map<String, Integer> get = new ConcurrentHashMap<>();
    public static Map<String, Integer> announce = new ConcurrentHashMap<>();

    public KBucket kBucket;

    public UdpManager(KBucket kBucket) {
        this.kBucket = kBucket;
    }

    public UdpPacket manage(UdpPacket p, byte[] localNodeId) {
        try {
            BencodeObject b = p.bencodeObject;
            InetSocketAddress remoteSocketAddress = p.address;
            if (b.get("y").castToBytes()[0] == 'q') {
                String q = new String(b.get("q").castToBytes(), BencodeObject.UNICODE_UTF8);
                switch (q) {
                    case "ping":
                        byte[] transId = b.get("t").fetch();
                        return Utils.rspPing(transId, localNodeId, remoteSocketAddress);
                    case "find_node":
                        transId = b.get("t").fetch();
                        byte[] target = b.get("a").get("target").fetch();
                        List<KBucketNode> KBucketNodes = kBucket.get(new KBucketNode(target, null, null));
                        return Utils.rspFindNode(transId, localNodeId, KBucketNodes, remoteSocketAddress);
                    case "get_peers":
                        byte[] bytes = b.get("a").get("info_hash").fetch();
                        StringBuilder sb = new StringBuilder();
                        for (byte a : bytes) {
                            sb.append(String.format("%02X", a));
                        }
                        save(b, BtLibrary.GET);
                        break;
                    case "announce_peer":
                        transId = b.get("t").castToBytes();
                        save(b, BtLibrary.ANNOUNCE);
                        return Utils.rspAnnouncePeer(transId, localNodeId, remoteSocketAddress); //TODO check token
                }
            } else {
                if (Utils.isBytesEqual(b.get("t").castToBytes(), new byte[]{'p', 'g'})) {
                    //ping
                } else if (Utils.isBytesEqual(b.get("t").castToBytes(), new byte[]{'f', 'n'})) {
                    //find node
                    List<KBucketNode> KBucketNodes = Utils.getNodesFromBytes(b.get("r").get("nodes").castToBytes());
                    for (KBucketNode KBucketNode : KBucketNodes)
                        kBucket.add(KBucketNode);
                } else if (Utils.isBytesEqual(b.get("t").castToBytes(), new byte[]{'g', 'p'})) {
                    //get peer
                } else if (Utils.isBytesEqual(b.get("t").castToBytes(), new byte[]{'a', 'p'})) {
                    //announce peer
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void save(BencodeObject recv, int type) {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = recv.get("a").get("info_hash").castToBytes();
        for (byte a : bytes) {
            sb.append(String.format("%02X", a));
        }
        BtLibrary.getInstence().addInfoHash(sb.toString(), type);
    }
}