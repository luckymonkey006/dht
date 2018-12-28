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
    private static final int GET_PEER_MS = 5;
    private static final int FIND_NODE_MS = 5;
    private static final int CHANGE_NODE_ID_MS = 2 * SEC;
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
                "51.15.179.170:6881",
                "62.210.140.229:51413",
                "95.34.15.239:27223",
                "122.192.242.220:6339",
                "180.212.196.89:48619",
                "27.202.38.250:41473",
                "118.10.51.36:58442",
                "182.86.201.250:15954",
                "58.245.180.163:20652",
                "221.6.29.221:14208",
                "176.122.177.207:51413",
                "151.80.35.171:51413",
                "117.91.24.155:37200",
                "150.249.131.77:6881",
                "45.62.213.188:49515",
                "84.238.148.98:24085",
                "140.206.152.230:10696",
                "113.250.251.124:18650",
                "87.117.47.242:27248",
                "80.251.239.120:51413",
                "24.188.74.60:45872",
                "124.114.206.97:5149",
                "213.144.20.130:57568",
                "58.62.190.117:33812",
                "121.205.99.206:21546",
                "207.180.192.206:58399",
                "1.64.189.109:22565",
                "101.68.2.125:6339",
                "46.238.0.44:58310",
                "183.83.184.42:22614",
                "27.216.56.70:7087",
                "46.164.219.203:38537",
                "5.155.26.175:8576",
                "84.238.228.59:6881",
                "60.31.74.77:19521",
                "94.231.134.42:6881",
                "111.8.128.194:1024",
                "180.119.203.157:6339",
                "60.180.165.155:13044",
                "60.222.89.149:47068",
                "173.249.33.72:35163",
                "192.131.44.90:61063",
                "140.224.130.69:17437",
                "54.247.69.34:33801",
                "178.70.153.27:6881",
                "128.68.29.70:51413",
                "113.89.3.36:26347",
                "59.62.15.238:8455",
                "86.104.210.227:50623",
                "212.83.151.178:52864",
                "78.192.50.9:51413",
                "42.238.54.136:14273",
                "117.172.170.76:20662",
                "178.117.39.46:5000",
                "2.34.33.25:44584",
                "83.252.111.218:16080",
                "110.246.229.194:1028",
                "183.149.177.109:6339",
                "5.189.160.21:20238",
                "207.148.101.132:3800",
                "218.81.53.119:51413",
                "222.80.200.121:52446",
                "110.18.236.203:36330",
                "39.68.6.132:19998",
                "117.179.175.54:22372",
                "175.44.225.172:1057",
                "77.238.65.40:47508",
                "122.238.118.157:21557",
                "95.42.31.122:52180",
                "136.59.216.141:6881",
                "165.227.143.24:51413",
                "207.180.210.81:41155",
                "139.162.115.210:38021",
                "222.209.135.213:42626",
                "221.203.55.43:31874",
                "68.204.79.1:45900",
                "85.196.153.100:51413",
                "46.39.240.46:6889",
                "5.105.124.179:57120",
                "218.76.221.131:46558",
                "212.83.160.28:51896",
                "188.76.167.77:15476",
                "211.33.72.105:40447",
                "207.180.192.206:20222",
                "49.90.15.188:59215",
                "118.120.126.227:53194",
                "123.66.35.75:8564",
                "47.184.64.112:51413",
                "183.149.32.133:6339",
                "183.147.202.16:1192",
                "119.251.202.195:5041",
                "223.72.42.183:11421",
                "37.144.108.107:46513",
                "36.5.170.58:4422",
                "222.80.248.157:25394",
                "5.103.147.96:6889",
                "118.73.132.162:1622",
                "27.192.66.225:1025",
                "183.149.43.52:6339",
                "111.8.128.160:1024",
                "77.121.165.130:42135",
                "222.133.196.189:20515",
                "213.66.189.236:33181",
                "115.171.112.121:7978",
                "183.132.49.182:10686",
                "31.211.17.48:44384",
                "180.123.4.5:21386",
                "185.154.164.24:6881",
                "36.250.92.226:20531",
                "73.143.188.35:54491",
                "35.170.63.152:63489",
                "58.37.205.168:27953",
                "62.210.167.156:55020",
                "106.115.109.133:10311",
                "54.244.230.153:16024",
                "187.250.33.103:6881",
                "188.244.35.234:51413",
                "116.192.70.13:6339",
                "74.111.98.248:51413",
                "27.150.56.188:24858",
                "60.215.159.172:27461",
                "173.249.44.180:51548",
                "115.202.171.78:6339",
                "172.90.196.137:8999",
                "219.139.52.43:2668",
                "109.252.14.228:8073",
                "27.153.113.152:6339",
                "58.176.135.126:8213",
                "81.171.27.178:6881",
                "221.2.183.105:6497",
                "112.230.237.113:6339",
                "90.242.24.245:6889",
                "64.63.73.124:1500",
                "119.129.50.91:54292",
                "112.252.102.65:1086",
                "49.84.233.77:6339",
                "176.63.8.59:11801",
                "68.103.242.133:58696",
                "124.142.213.96:9778",
                "114.84.83.129:7519",
                "51.15.218.151:51413",
                "113.103.217.221:19328",
                "114.219.22.222:28930",
                "101.68.2.210:6339",
                "106.119.217.34:10789",
                "213.113.190.201:8999",
                "154.45.216.231:1228",
                "18.191.127.79:52718",
                "109.173.116.146:51413",
                "185.45.195.193:28114",
                "116.231.137.36:6339",
                "60.180.163.118:13346",
                "114.37.173.175:10716",
                "2.26.26.6:6881",
                "130.204.247.170:37975",
                "120.37.166.109:3835",
                "117.177.41.236:58889",
                "51.15.89.165:51413",
                "42.58.150.33:8738",
                "182.117.241.168:26512",
                "92.93.179.111:53708",
                "116.27.158.199:17427",
                "54.247.69.34:23295",
                "43.227.139.220:6762",
                "118.170.35.54:20438",
                "176.234.108.82:55588",
                "101.68.0.234:6371",
                "54.38.47.75:29039",
                "207.180.192.205:59267",
                "14.132.89.158:10802",
                "5.12.85.35:53041",
                "113.205.138.180:8990",
                "46.47.91.19:58180",
                "112.120.185.173:7905",
                "171.111.146.134:29254",
                "82.94.206.196:51413",
                "112.111.227.91:6339",
                "27.208.75.150:9789",
                "112.101.103.41:12789",
                "117.35.133.83:30100",
                "180.174.61.242:7929",
                "94.236.131.134:6996",
                "39.72.233.66:13812"
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





