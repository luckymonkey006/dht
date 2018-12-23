package com.github.fengleicn.dht.subsystem;

import com.github.fengleicn.dht.node.KBucketNode;
import com.github.fengleicn.dht.packet.UdpPacket;
import com.github.fengleicn.dht.utils.KBucket;
import com.github.fengleicn.dht.utils.DhtUtil;

import java.net.InetSocketAddress;


public class NodeInfo {

    static KBucket kBucket;

    public NodeInfo(KBucket kBucket) {
        this.kBucket = kBucket;
    }

    public static KBucketNode getRandomNode() throws Exception {
        return kBucket.getRandomNode();
    }

    public UdpPacket getRandomFindNodePacket(byte[] transId, KBucketNode localKBucketNode, byte[] targetId) throws Exception {
        KBucketNode KBucketNode = kBucket.getRandomNode();
        return DhtUtil.findNode(transId, localKBucketNode.nodeId, targetId, new InetSocketAddress(KBucketNode.getIp(), KBucketNode.getPort()));
    }
}
