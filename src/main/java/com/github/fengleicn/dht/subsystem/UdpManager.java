package com.github.fengleicn.dht.subsystem;

import com.github.fengleicn.dht.bencode.BencodeObject;
import com.github.fengleicn.dht.node.KBucketNode;
import com.github.fengleicn.dht.packet.UdpPacket;
import com.github.fengleicn.dht.utils.KBucket;
import com.github.fengleicn.dht.utils.DhtUtil;

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
            if (b.get("y").castB()[0] == 'q') {
                String q = new String(b.get("q").castB(), BencodeObject.DEFAULT_CHARSET);
                switch (q) {
                    case "ping":
                        byte[] transId = b.get("t").cast();
                        return DhtUtil.rspPing(transId, localNodeId, remoteSocketAddress);
                    case "find_node":
                        transId = b.get("t").cast();
                        byte[] target = b.get("a").get("target").cast();
                        List<KBucketNode> KBucketNodes = kBucket.get(new KBucketNode(target, null, null));
                        return DhtUtil.rspFindNode(transId, localNodeId, KBucketNodes, remoteSocketAddress);
                    case "get_peers":
                        byte[] bytes = b.get("a").get("info_hash").cast();
                        StringBuilder sb = new StringBuilder();
                        for (byte a : bytes) {
                            sb.append(String.format("%02X", a));
                        }
                        save(b, BtLibrary.GET);
                        break;
                    case "announce_peer":
                        transId = b.get("t").castB();
                        save(b, BtLibrary.ANNOUNCE);
                        return DhtUtil.rspAnnouncePeer(transId, localNodeId, remoteSocketAddress); //TODO check token
                }
            } else {
                if (DhtUtil.byteArraysEqual(b.get("t").castB(), new byte[]{'p', 'g'})) {
                    //ping
                } else if (DhtUtil.byteArraysEqual(b.get("t").castB(), new byte[]{'f', 'n'})) {
                    //find node
                    List<KBucketNode> KBucketNodes = DhtUtil.decodeNodes(b.get("r").get("KBucketNodes").castB());
                    for (KBucketNode KBucketNode : KBucketNodes)
                        kBucket.add(KBucketNode);
                } else if (DhtUtil.byteArraysEqual(b.get("t").castB(), new byte[]{'g', 'p'})) {
                    //get peer
                } else if (DhtUtil.byteArraysEqual(b.get("t").castB(), new byte[]{'a', 'p'})) {
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
        byte[] bytes = recv.get("a").get("info_hash").castB();
        for (byte a : bytes) {
            sb.append(String.format("%02X", a));
        }
        BtLibrary.getInstence().addInfoHash(sb.toString(), type);
    }
}