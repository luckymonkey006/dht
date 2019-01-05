package com.github.fengleicn.dht.modules;

import com.github.fengleicn.dht.MainApplication;
import com.github.fengleicn.dht.utils.Utils;
import com.github.fengleicn.dht.utils.structs.KBucketNode;
import com.github.fengleicn.dht.utils.structs.bencode.BencodeObject;
import com.github.fengleicn.dht.utils.structs.bencode.utils.BencodeUtil;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class TrackerServer {
    static final int SEC = 1000;
    static final int TCP_TIMEOUT = 20 * SEC;
    static final int UDP_TIMEOUT = 4 * SEC;

    public static PrintWriter trackerLog;
    private static final Random random = new Random();


    static {
        try {
            trackerLog = new PrintWriter(new FileWriter("tracker.txt", true), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] zeroByte36 = new byte[36];
    public static byte[] zeroByte4 = new byte[4];
    public static Set<String> peerBlackList = Collections.synchronizedSet(new HashSet<>());
    public static int TRANS_ID = 0xCAFEBABE;

    static {
        peerBlackList.add("0.0.0.0:0");
        peerBlackList.add("0.0.0.0:65535");
    }

//    @Test
    public static void test001(String[] args) throws InterruptedException, IOException {
        new TrackerServer().request("EEB7C79987A49F3CA816A951C404350A83C23C3C");
    }

    public static void request(String infoHash) throws InterruptedException, IOException {
        KBucketNode myKBucketNode = StartUp.myKBucketNode;
        if (myKBucketNode != null)
            peerBlackList.add(myKBucketNode.getIp() + ":" + myKBucketNode.getPort());

        String[] trackerAddresses = {
                "tracker.coppersurfer.tk:6969",
                "tracker.opentrackr.org:1337",
                "tracker.internetwarriors.net:1337",
                "9.rarbg.to:2710",
                "exodus.desync.com:6969",
                "tracker2.itzmx.com:6961",
                "tracker1.itzmx.com:8080",
                "explodie.org:6969",
                "ipv4.tracker.harry.lu:80",
                "tracker.tiny-vps.com:6969",
                "tracker.port443.xyz:6969",
                "denis.stalker.upeer.me:6969",
                "bt.xxx-tracker.com:2710",
                "tracker.torrent.eu.org:451",
                "open.demonii.si:1337",
                "tracker1.wasabii.com.tw:6969",
                "tracker.vanitycore.co:6969",
                "tracker.iamhansen.xyz:2000",
                "tracker.dler.org:6969",
                "tracker.cyberia.is:6969",
                "thetracker.org:80",
                "retracker.lanta-net.ru:2710",
                "open.stealth.si:80",
                "tracker.dyn.im:6969",
                "torrentclub.tech:6969",
                "zephir.monocul.us:6969",
                "tracker4.itzmx.com:2710",
                "tracker.open-tracker.org:1337",
                "tracker.gbitt.info:80",
                "tracker.freddit.nl:6969",
                "tracker.filepit.to:6969",
                "pubt.in:2710",
                "tracker.kamigami.org:2710",
                "tracker.justseed.it:1337",
                "packages.crunchbangplusplus.org:6969",
                "newtoncity.org:6969",
                //HTTP
                "http://tracker.internetwarriors.net:1337/announce",
                "http://tracker2.itzmx.com:6961/announce",
                "http://tracker1.itzmx.com:8080/announce",
                "http://explodie.org:6969/announce",
                "http://tracker.port443.xyz:6969/announce",
                "http://tracker1.wasabii.com.tw:6969/announce",
                "http://private.minimafia.nl:443/announce",
                "http://prestige.minimafia.nl:443/announce",
                "http://open.acgnxtracker.com:80/announce",
                "http://opentracker.xyz:80/announce",
                "http://open.trackerlist.xyz:80/announce",
                "http://tracker3.itzmx.com:6961/announce",
                "http://torrent.nwps.ws:80/announce",
                "http://torrentclub.tech:6969/announce",
                "http://tracker4.itzmx.com:2710/announce",
                "http://tracker.torrentyorg.pl:80/announce",
                "http://tracker.open-tracker.org:1337/announce",
                "http://tracker.gbitt.info:80/announce",
                "http://tracker.city9x.com:2710/announce",
                "http://t.nyaatracker.com:80/announce",
                "http://retracker.mgts.by:80/announce",
                "http://open.acgtracker.com:1096/announce",
                "http://fxtt.ru:80/announce",
                "http://bt.artvid.ru:6969/announce",
                "http://0d.kebhana.mx:443/announce",
                "http://tracker.tfile.me:80/announce.php",
                "http://tracker.tfile.me:80/announce",
                "http://tracker.tfile.co:80/announce",
                "http://share.camoe.cn:8080/announce",
                "http://peersteers.org:80/announce",
                "http://omg.wtftrackr.pw:1337/announce",
                "http://newtoncity.org:6969/announce",
        };

        Set<String> peers = new ConcurrentSkipListSet<>();

        for (String tracker : trackerAddresses) {
            if (random.nextInt(4) != 0) {
                continue;
            }
            String[] trackerSplit = tracker.split(":");
            String trackerHost = trackerSplit[0];
            int trackerPort = 80;
            if (trackerSplit.length == 2) {
                trackerPort = Integer.valueOf(trackerSplit[1]);
            }
            int finalTrackerPort = trackerPort;
            new Thread(() -> {
                try {
                    if(trackerHost.startsWith("http")){
                        peers.addAll(request(tracker,  infoHash.replaceAll("(.{2})", "%$1")));
                    }else {
                        peers.addAll(request(trackerHost, finalTrackerPort, infoHash));
                    }
                } catch (IOException e) {
                    trackerLog.println("[ERROR] In tracker: " + trackerHost + ":" + finalTrackerPort);
                    e.printStackTrace(trackerLog);
                }
            }).start();
        }
        Thread.sleep(6000); //等上面的线程结束
        trackerLog.println("[INFO]  Downloading: " + infoHash + ": \n" + "        IP: " + peers.toString() + "\n");
        Set<String> peersCopy = new HashSet<>(peers);
        final int MAX = 500;
        if (peers.size() > MAX) {
            List<String> copy = new ArrayList<>(peers);
            peersCopy = new HashSet<>(copy.subList(0, MAX));
        }
        for (String peer : peersCopy) {
            Thread.sleep(1);
            new Thread(() -> {
                ExtendBepNo9 extendBepNo9 = new ExtendBepNo9();
                String[] peerSplit = peer.split(":");
                try (
                        Socket socket = new Socket(peerSplit[0], Integer.valueOf(peerSplit[1]))
                ) {
                    socket.setSoTimeout(TCP_TIMEOUT);
                    try (
                            InputStream inputStream = socket.getInputStream();
                            OutputStream outputStream = socket.getOutputStream()
                    ) {
                        Boolean success = extendBepNo9.request(inputStream, outputStream, infoHash);
                        if (!Boolean.TRUE.equals(success)) {
                            peerBlackList.add(peerSplit[0] + ":" + peerSplit[1]);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static Set<String> request(String host, int port, String infoHash) throws IOException {
        try (
                DatagramSocket datagramSocket = new DatagramSocket(new InetSocketAddress(0))
        ) {
            Set<String> peers = new HashSet<>();
            datagramSocket.setSoTimeout(UDP_TIMEOUT);
            byte[] buf;
            /**
             * Offset  Size            Name            Value
             * 0       64-bit integer  protocol_id     0x41727101980 // magic constant
             * 8       32-bit integer  action          0 // connect
             * 12      32-bit integer  transaction_id
             * 16
             */
            buf = ByteBuffer.allocate(16).putLong(0x41727101980L).putInt(0).putInt(TRANS_ID).array();

            SocketAddress socketAddress;
            DatagramPacket packet;
            try {
                socketAddress = new InetSocketAddress(host, port);
                packet = new DatagramPacket(buf, buf.length, socketAddress);
            } catch (Exception e) {
                System.err.println(host + "  " + port);
                e.printStackTrace();
                return peers;
            }
            datagramSocket.send(packet);

            try {
                datagramSocket.receive(packet);
            } catch (Exception e) {
//            e.printStackTrace();
                return peers;
            }

            buf = packet.getData();
            byte[] transIdRecvBytes = Arrays.copyOfRange(buf, 4, 8);
            byte[] connIdRecvBytes = Arrays.copyOfRange(buf, 8, 16);
            int transIdRecv = ByteBuffer.allocate(4).put(transIdRecvBytes).getInt(0);
            if (transIdRecv != TRANS_ID)
                return peers;

            /**
             *Offset  Size    Name    Value
             * 0       64-bit integer  connection_id
             * 8       32-bit integer  action          1 // announce
             * 12      32-bit integer  transaction_id
             * 16      20-byte string  info_hash
             * 36      20-byte string  peer_id
             * 56      64-bit integer  downloaded
             * 64      64-bit integer  left
             * 72      64-bit integer  uploaded
             * 80      32-bit integer  event           0 // 0: none; 1: completed; 2: started; 3: stopped
             * 84      32-bit integer  IP address      0 // default
             * 88      32-bit integer  key
             * 92      32-bit integer  num_want        -1 // default
             * 96      16-bit integer  port
             * 98
             */

            buf = ByteBuffer.allocate(104)
                    .put(connIdRecvBytes).putInt(1)
                    .putInt(TRANS_ID).put(Utils.getBytesFromHex(infoHash))
                    .put(Utils.randomBytes(20)).put(zeroByte36)
                    .putInt(TRANS_ID).putInt(256).put(Utils.randomBytes(4)).array();
            packet = new DatagramPacket(buf, buf.length, new InetSocketAddress(host, port));
            datagramSocket.send(packet);
            int LEN = 1694;
            packet = new DatagramPacket(new byte[LEN], LEN);

            try {
                datagramSocket.receive(packet);
            } catch (Exception e) {
//            e.printStackTrace();
                return peers;
            }

            int packetLength = packet.getLength();
            buf = packet.getData();
            for (int i = 20; i + 6 <= packetLength; i += 6) {
                byte[] remoteAddress = Arrays.copyOfRange(buf, i, i + 6);
                if (peerBlackList.contains(Utils.getHostIpFromBytes(remoteAddress) + ":" + Utils.getIntFromBytes(remoteAddress)))
                    continue;
                String peerIp = Utils.getHostIpFromBytes(remoteAddress);
                int peerPort = Integer.parseInt(Utils.getIntFromBytes(remoteAddress));
                peers.add(peerIp + ":" + peerPort);
            }
            return peers;
        }
    }

    public static Set<String> request(String tracckerUrl, String requestInfoHash){
        Set<String> peers = new HashSet<>();
        try {
            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(tracckerUrl + "?" + "info_hash=" + requestInfoHash);
            HttpResponse httpResponse = closeableHttpClient.execute(httpGet);
            byte[] respBytes = EntityUtils.toByteArray(httpResponse.getEntity());
            System.out.println(new String(respBytes));
            BencodeObject bencodeObject = BencodeUtil.parse(respBytes);
            byte[] buf = bencodeObject.get("peers").castToBytes();
            for (int i = 0; i + 6 <= buf.length; i += 6) {
                byte[] remoteAddress = Arrays.copyOfRange(buf, i, i + 6);
                String peerIp = Utils.getHostIpFromBytes(remoteAddress);
                int peerPort = Integer.parseInt(Utils.getIntFromBytes(remoteAddress));
                peers.add(peerIp + ":" + peerPort);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        System.out.println(peers);
        return peers;
    }

    @Test
    public void test001(){
        System.out.println(URLEncoder.encode("EEB7C79987A49F3CA816A951C404350A83C23C3C"));
        request("http://tracker2.itzmx.com:6961/announce",  "%EE%B7%C7%99%87%A4%9F%3C%A8%16%A9%51%C4%04%35%0A%83%C2%3C%3C");

    }
}