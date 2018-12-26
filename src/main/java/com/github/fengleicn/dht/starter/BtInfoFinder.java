package com.github.fengleicn.dht.starter;

import com.github.fengleicn.dht.node.KBucketNode;
import com.github.fengleicn.dht.packet.UdpPacket;
import com.github.fengleicn.dht.subsystem.NodeInfo;
import com.github.fengleicn.dht.subsystem.UdpManager;
import com.github.fengleicn.dht.subsystem.UdpNetworkContoller;
import com.github.fengleicn.dht.utils.KBucket;
import com.github.fengleicn.dht.utils.Utils;

import java.net.*;
import java.util.Arrays;

public class BtInfoFinder {
    public static KBucketNode localKBucketNode;
    UdpNetworkContoller networkController;
    UdpManager udpManager;
    NodeInfo nodeInfo;
    int localPort;

    private static final int SEC = 1000;
    private static final int GET_PEER_MS = 5;
    private static final int FIND_NODE_MS = 5;
    private static final int CHANGE_NODE_ID_MS = 2 * SEC;
    public static final String GET_PEER_INFO_HASH = "EEB7C79987A49F3CA816A951C404350A83C23C3C";

    public BtInfoFinder(String ip, int port) throws SocketException, UnknownHostException {
        this.localPort = port;
        localKBucketNode = new KBucketNode(Utils.randomBytes(20), Utils.getBytesFromHost(ip), Utils.getBytesFromInt(port));
        System.out.println("DEBUG ip: " + Arrays.toString(localKBucketNode.ip));
        networkController = new UdpNetworkContoller(new DatagramSocket(port));
        KBucket kBucket = new KBucket(localKBucketNode);
        udpManager = new UdpManager(kBucket);
        nodeInfo = new NodeInfo(kBucket);
    }

    private void init() throws UnknownHostException {
        String urlA = "router.utorrent.com";
        String urlB = "router.bittorrent.com";
        String urlC = "dht.transmissionbt.com";
        String urlD = "112.10.240.235";
        String urlE = "72.178.134.65";
        String urlF = "121.239.242.138";
        udpManager.kBucket.add(new KBucketNode(Utils.randomBytes(20), Utils.getBytesFromHost(urlA), Utils.getBytesFromInt(6881)));
        udpManager.kBucket.add(new KBucketNode(Utils.randomBytes(20), Utils.getBytesFromHost(urlB), Utils.getBytesFromInt(6881)));
        udpManager.kBucket.add(new KBucketNode(Utils.randomBytes(20), Utils.getBytesFromHost(urlC), Utils.getBytesFromInt(6881)));
        udpManager.kBucket.add(new KBucketNode(Utils.randomBytes(20), Utils.getBytesFromHost(urlD), Utils.getBytesFromInt(26529)));
        udpManager.kBucket.add(new KBucketNode(Utils.randomBytes(20), Utils.getBytesFromHost(urlE), Utils.getBytesFromInt(51978)));
        udpManager.kBucket.add(new KBucketNode(Utils.randomBytes(20), Utils.getBytesFromHost(urlF), Utils.getBytesFromInt(6339)));
    }

    private void findNodeTask() {
        while (true) {
            byte[] transId = new byte[]{'f', 'n'};
            byte[] targetId = Utils.randomBytes(20);
            try {
                Thread.sleep(FIND_NODE_MS);
                networkController.send(nodeInfo.getRandomFindNodePacket(transId, localKBucketNode, targetId)); //send
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void gerPeerTask() {
        byte[] transId = new byte[]{'g', 'p'};
        while (true) {
            try {
                Thread.sleep(GET_PEER_MS);
                KBucketNode KBucketNode = NodeInfo.getRandomNode();
                UdpPacket udpPacket = Utils.getPeer(transId, localKBucketNode.nodeId,
                        Utils.getBytesFromHex(GET_PEER_INFO_HASH), new InetSocketAddress(KBucketNode.getIp(), KBucketNode.getPort()));
                networkController.send(udpPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void manageUdpNetworkTask() {
        while (true) {
            try {
                UdpPacket udpPacket = networkController.recv();
                if (udpPacket != null) {
                    UdpPacket resp;
                    resp = udpManager.manage(udpPacket, localKBucketNode.nodeId);
                    if (resp != null)
                        networkController.send(resp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void changeNodeIdTask() {
        while (true) {
            try {
                Thread.sleep(CHANGE_NODE_ID_MS);
                localKBucketNode.nodeId = Utils.randomBytes(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void start() {
        new Thread(this::findNodeTask, "FIND_NODE_THREAD").start();
        new Thread(this::gerPeerTask, "GET_PEER_THREAD").start();
        new Thread(this::manageUdpNetworkTask, "HANDLER_THREAD").start();
        new Thread(this::changeNodeIdTask, "CHG_NODE_ID_THREAD").start();
    }

    public void run() throws Exception {
        init();
        start();
    }
}





