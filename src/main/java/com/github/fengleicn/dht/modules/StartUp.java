package com.github.fengleicn.dht.modules;

import com.github.fengleicn.dht.utils.structs.KBucketNode;
import com.github.fengleicn.dht.utils.structs.UdpPacket;
import com.github.fengleicn.dht.utils.UdpUtils;
import com.github.fengleicn.dht.modules.UdpContoller;
import com.github.fengleicn.dht.utils.structs.KBucket;
import com.github.fengleicn.dht.utils.Utils;

import java.net.*;

public class StartUp {
    public static KBucketNode myKBucketNode;
    UdpContoller networkController;
    int myPort;

    private static final int SEC = 1000;
    private static final int GET_PEER_MS = 10;
    private static final int FIND_NODE_MS = 10;
    private static final int CHANGE_NODE_ID_MS = 1000 * SEC;
    public static final String GET_PEER_INFO_HASH = "EEB7C79987A49F3CA816A951C404350A83C23C3C";

    public StartUp(String myIp, int myPort) throws SocketException, UnknownHostException {
        this.myPort = myPort;
        myKBucketNode = new KBucketNode(Utils.randomBytes(20), Utils.getBytesFromHost(myIp), Utils.getBytesFromInt(myPort));
        KBucket kBucket = new KBucket(myKBucketNode);
        UdpUtils.set(kBucket);
        networkController = new UdpContoller(new DatagramSocket(myPort));
    }

    private void initKBucketNodes() throws UnknownHostException {
        String[] urls = {
                "router.utorrent.com:6881",
                "router.bittorrent.com:6881",
                "dht.transmissionbt.com:6881",
                "183.202.2.111:6627",
                "37.232.149.72:56196"
        };
        for (String url : urls) {
            String[] urlSplit = url.split(":");
            String hostName = urlSplit[0];
            String port = urlSplit[1];
            UdpUtils.kBucket.add(new KBucketNode(Utils.randomBytes(20),
                    Utils.getBytesFromHost(hostName),
                    Utils.getBytesFromInt(Integer.parseInt(port))));
        }
    }

    private void findNodeTask() {
        while (true) {
            byte[] transId = new byte[]{'f', 'n'};
            byte[] targetId = Utils.randomBytes(20);
            try {
                Thread.sleep(FIND_NODE_MS);
                KBucketNode randomNode = UdpUtils.kBucket.randomNode();
                UdpPacket udpPacket = Utils.findNode(transId, myKBucketNode.nodeId, targetId,
                        new InetSocketAddress(randomNode.getIp(), randomNode.getPort()));
                networkController.send(udpPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getPeerTask() {
        byte[] transId = new byte[]{'g', 'p'};
        while (true) {
            try {
                Thread.sleep(GET_PEER_MS);
                KBucketNode KBucketNode = UdpUtils.kBucket.randomNode();
                UdpPacket udpPacket = Utils.getPeer(transId, myKBucketNode.nodeId,
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
                    resp = UdpUtils.createResp(udpPacket, myKBucketNode.nodeId);
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
                myKBucketNode.nodeId = Utils.randomBytes(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startUp() throws Exception {
        initKBucketNodes();
        new Thread(this::findNodeTask, "FIND_NODE_THREAD").start();
        new Thread(this::getPeerTask, "GET_PEER_THREAD").start();
        new Thread(this::manageUdpNetworkTask, "HANDLER_THREAD").start();
        new Thread(this::changeNodeIdTask, "CHG_NODE_ID_THREAD").start();
    }
}





