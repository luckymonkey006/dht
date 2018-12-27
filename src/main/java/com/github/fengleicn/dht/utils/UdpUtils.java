package com.github.fengleicn.dht.utils;

import com.github.fengleicn.dht.utils.structs.bencode.BencodeObject;
import com.github.fengleicn.dht.modules.InfoHashStorage;
import com.github.fengleicn.dht.utils.structs.KBucket;
import com.github.fengleicn.dht.utils.structs.KBucketNode;
import com.github.fengleicn.dht.utils.structs.UdpPacket;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UdpUtils {
    public static Map<String, Integer> get = new ConcurrentHashMap<>();
    public static Map<String, Integer> announce = new ConcurrentHashMap<>();

    public static KBucket kBucket;

    public static void set(KBucket kBucket) {
        UdpUtils.kBucket = kBucket;
    }

    public static UdpPacket createResp(UdpPacket p, byte[] localNodeId) {
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
                        save(b, InfoHashStorage.WEIGHT_GET);
                        break;
                    case "announce_peer":
                        transId = b.get("t").castToBytes();
                        save(b, InfoHashStorage.WEIGHT_ANNOUNCE);
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


    public static void save(BencodeObject recv, int weight) {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = recv.get("a").get("info_hash").castToBytes();
        for (byte a : bytes) {
            sb.append(String.format("%02X", a));
        }
        InfoHashStorage.getInstance().addInfoHash(sb.toString(), weight);
    }
}