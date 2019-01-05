package com.github.fengleicn.dht.modules;

import com.github.fengleicn.dht.MainApplication;
import com.github.fengleicn.dht.utils.Utils;
import com.github.fengleicn.dht.utils.structs.KBucketNode;
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

    @Test
    public static void test001(String[] args) throws InterruptedException, IOException {
        new TrackerServer().request("EEB7C79987A49F3CA816A951C404350A83C23C3C");
    }

    public static void request(String infoHash) throws InterruptedException, IOException {
        KBucketNode myKBucketNode = StartUp.myKBucketNode;
        if (myKBucketNode != null)
            peerBlackList.add(myKBucketNode.getIp() + ":" + myKBucketNode.getPort());

        String[] trackerAddresses = {
                "tracker.opentrackr.org:1337",
                "tracker.internetwarriors.net:1337",
                "tracker.internetwarriors.net:1337",
                "9.rarbg.to:2710",
                "exodus.desync.com:6969",
                "explodie.org:6969",
                "explodie.org:6969",
                "tracker1.itzmx.com:8080",
                "ipv4.tracker.harry.lu:80",
                "tracker1.itzmx.com:8080",
                "denis.stalker.upeer.me:6969",
                "bt.xxx-tracker.com:2710",
                "tracker.torrent.eu.org:451",
                "tracker.tiny-vps.com:6969",
                "thetracker.org:80",
                "open.demonii.si:1337",
                "tracker.port443.xyz:6969",
                "tracker.iamhansen.xyz:2000",
                "retracker.lanta-net.ru:2710",
                "open.stealth.si:80",
                "tracker.port443.xyz:6969",
                "private.minimafia.nl:443",
                "prestige.minimafia.nl:443",
                "open.acgnxtracker.com:80",
                "tracker.vanitycore.co:6969",
                "zephir.monocul.us:6969",
                "tracker.cyberia.is:6969",
                "tracker.fastdownload.xyz:443",
                "opentracker.xyz:443",
                "opentracker.xyz:80",
                "open.trackerlist.xyz:80",
                "tracker3.itzmx.com:6961",
                "torrent.nwps.ws:80",
                "tracker1.wasabii.com.tw:6969",
                "tracker.kamigami.org:2710",
                "tracker.gbitt.info:80",
                "tracker.filepit.to:6969",
                "tracker.dyn.im:6969",
                "tracker.dler.org:6969",
                "torrentclub.tech:6969",
                "pubt.in:2710",
                "tracker.gbitt.info:443",
                "tracker1.wasabii.com.tw:6969",
                "tracker.torrentyorg.pl:80",
                "tracker.gbitt.info:80",
                "tracker.city9x.com:2710",
                "torrentclub.tech:6969",
                "t.nyaatracker.com:80",
                "retracker.mgts.by:80",
                "0d.kebhana.mx:443",
                "tracker.openwebtorrent.com:443",
                "tracker.fastcast.nz:443",
                "tracker.btorrent.xyz:443",
                "tracker4.itzmx.com:2710",
                "tracker.justseed.it:1337",
                "packages.crunchbangplusplus.org:6969",
                "1337.abcvg.info:443",
                "tracker4.itzmx.com:2710",
                "tracker.tfile.me:80",
                "tracker.tfile.me:80",
                "tracker.tfile.co:80",
                "share.camoe.cn:8080",
                "peersteers.org:80",
                "agusiq-torrents.pl:6969",
                "public.popcorn-tracker.org:6969",
                "182.176.139.129:6969",
                "5.79.83.193:2710",
                "91.218.230.81:6969",
                "tracker.ilibr.org:80",
                "atrack.pow7.com",
                "bt.henbt.com:2710",
                "mgtracker.org:2710",
                "mgtracker.org:6969",
                "open.touki.ru.php",
                "p4p.arenabg.ch:1337",
                "pow7.com:80",
                "retracker.krs-ix.ru:80",
                "secure.pow7.com",
                "t1.pow7.com",
                "t2.pow7.com",
                "thetracker.org:80",
                "torrentsmd.com:8080",
                "tracker.bittor.pw:1337",
                "tracker.dutchtracking.com:80",
                "tracker.dutchtracking.nl:80",
                "tracker.edoardocolombo.eu:6969",
                "tracker.ex.ua:80",
                "tracker.kicks-ass.net:80",
                "tracker1.wasabii.com.tw:6969",
                "tracker2.itzmx.com:6961",
                "www.wareztorrent.com:80",
                "62.138.0.158:6969",
                "eddie4.nl:6969",
                "explodie.org:6969",
                "shadowshq.eddie4.nl:6969",
                "shadowshq.yi.org:6969",
                "tracker.eddie4.nl:6969",
                "tracker.mg64.net:2710",
                "tracker.sktorrent.net:6969",
                "tracker2.indowebster.com:6969",
                "tracker4.piratux.com:6969",
                "atrack.pow7.com",
                "bt.henbt.com:2710",
                "mgtracker.org:2710",
                "mgtracker.org:6969",
                "open.touki.ru.php",
                "p4p.arenabg.ch:1337",
                "pow7.com:80",
                "retracker.krs-ix.ru:80",
                "secure.pow7.com",
                "t1.pow7.com",
                "t2.pow7.com",
                "thetracker.org:80",
                "torrentsmd.com:8080",
                "tracker.bittor.pw:1337",
                "tracker.dutchtracking.com",
                "tracker.dutchtracking.com:80",
                "tracker.dutchtracking.nl:80",
                "tracker.edoardocolombo.eu:6969",
                "tracker.ex.ua:80",
                "tracker.kicks-ass.net:80",
                "tracker.mg64.net:6881",
                "tracker.tfile.me",
                "tracker1.wasabii.com.tw:6969",
                "tracker2.itzmx.com:6961",
                "tracker2.wasabii.com.tw:6969",
                "www.wareztorrent.com:80",
                "bt.xxx-tracker.com:2710",
                "eddie4.nl:6969",
                "shadowshq.eddie4.nl:6969",
                "shadowshq.yi.org:6969",
                "tracker.eddie4.nl:6969",
                "tracker.mg64.net:2710",
                "tracker.mg64.net:6969",
                "tracker.opentrackr.org:1337",
                "tracker.sktorrent.net:6969",
                "tracker2.indowebster.com:6969",
                "tracker4.piratux.com:6969",
                "tracker.coppersurfer.tk:6969",
                "tracker.opentrackr.org:1337",
                "zer0day.ch:1337",
                "zer0day.to:1337",
                "explodie.org:6969",
                "tracker.leechers-paradise.org:6969",
                "9.rarbg.com:2710",
                "9.rarbg.me:2780",
                "9.rarbg.to:2730",
                "p4p.arenabg.com:1337",
                "tracker.sktorrent.net:6969",
                "p4p.arenabg.com:1337",
                "tracker.aletorrenty.pl:2710",
                "tracker.aletorrenty.pl:2710",
                "tracker.bittorrent.am",
                "tracker.kicks-ass.net:80",
                "tracker.kicks-ass.net",
                "tracker.baravik.org:6970",
                "torrent.gresille.org:80",
                "torrent.gresille.org",
                "tracker.skyts.net:6969",
                "tracker.internetwarriors.net:1337",
                "tracker.skyts.net:6969",
                "tracker.dutchtracking.nl",
                "tracker.yoshi210.com:6969",
                "tracker.tiny-vps.com:6969",
                "tracker.internetwarriors.net:1337",
                "mgtracker.org:2710",
                "tracker.yoshi210.com:6969",
                "tracker.tiny-vps.com:6969",
                "tracker.filetracker.pl:8089",
                "tracker.ex.ua:80",
                "91.218.230.81:6969",
                "www.wareztorrent.com",
                "www.wareztorrent.com",
                "tracker.filetracker.pl:8089",
                "tracker.ex.ua",
                "tracker.calculate.ru:6969",
                "tracker.grepler.com:6969",
                "tracker.flashtorrents.org:6969",
                "tracker.bittor.pw:1337",
                "tracker.tvunderground.org.ru:3218",
                "tracker.grepler.com:6969",
                "tracker.flashtorrents.org:6969",
                "retracker.gorcomnet.ru",
                "bt.pusacg.org:8080",
                "87.248.186.252:8080",
                "tracker.kuroy.me:5944",
                "182.176.139.129:6969",
                "tracker.kuroy.me:5944",
                "retracker.krs-ix.ru",
                "open.acgtracker.com:1096",
                "open.stealth.si:80",
                "208.67.16.113:8000",
                "tracker.dler.org:6969",
                "bt2.careland.com.cn:6969",
                "open.lolicon.eu:7777",
                "tracker.opentrackr.org:1337",
                "explodie.org:6969",
                "p4p.arenabg.com:1337",
                "tracker.aletorrenty.pl:2710",
                "tracker.bittorrent.am",
                "tracker.kicks-ass.net",
                "tracker.baravik.org:6970",
                "torrent.gresille.org",
                "tracker.skyts.net:6969",
                "tracker.internetwarriors.net:1337",
                "tracker.dutchtracking.nl",
                "tracker.yoshi210.com:6969",
                "tracker.tiny-vps.com:6969",
                "www.wareztorrent.com",
                "tracker.filetracker.pl:8089",
                "tracker.ex.ua",
                "tracker.calculate.ru:6969",
                "tracker.tvunderground.org.ru:3218",
                "tracker.grepler.com:6969",
                "tracker.flashtorrents.org:6969",
                "retracker.gorcomnet.ru",
                "bt.pusacg.org:8080",
                "87.248.186.252:8080",
                "tracker.kuroy.me:5944",
                "retracker.krs-ix.ru",
                "open.acgtracker.com:1096",
                "bt2.careland.com.cn:6969",
                "open.lolicon.eu:7777",
                "www.wareztorrent.com",
                "213.163.67.56:1337",
                "213.163.67.56:1337",
                "185.86.149.205:1337",
                "74.82.52.209:6969",
                "94.23.183.33:6969",
                "74.82.52.209:6969",
                "151.80.120.114:2710",
                "109.121.134.121:1337",
                "168.235.67.63:6969",
                "109.121.134.121:1337",
                "178.33.73.26:2710",
                "178.33.73.26:2710",
                "85.17.19.180",
                "85.17.19.180:80",
                "210.244.71.25:6969",
                "85.17.19.180",
                "213.159.215.198:6970",
                "191.101.229.236:1337",
                "178.175.143.27",
                "89.234.156.205:80",
                "91.216.110.47",
                "114.55.113.60:6969",
                "195.123.209.37:1337",
                "114.55.113.60:6969",
                "210.244.71.26:6969",
                "107.150.14.110:6969",
                "5.79.249.77:6969",
                "195.123.209.37:1337",
                "37.19.5.155:2710",
                "107.150.14.110:6969",
                "5.79.249.77:6969",
                "185.5.97.139:8089",
                "194.106.216.222:80",
                "91.218.230.81:6969",
                "104.28.17.69",
                "104.28.16.69",
                "185.5.97.139:8089",
                "194.106.216.222",
                "80.246.243.18:6969",
                "37.19.5.139:6969",
                "5.79.83.193:6969",
                "46.4.109.148:6969",
                "51.254.244.161:6969",
                "188.165.253.109:1337",
                "91.217.91.21:3218",
                "37.19.5.155:6881",
                "46.4.109.148:6969",
                "51.254.244.161:6969",
                "104.28.1.30:8080",
                "81.200.2.231",
                "157.7.202.64:8080",
                "87.248.186.252:8080",
                "128.199.70.66:5944",
                "182.176.139.129:6969",
                "128.199.70.66:5944",
                "188.165.253.109:1337",
                "93.92.64.5",
                "173.254.204.71:1096",
                "195.123.209.40:80",
                "62.212.85.66:2710",
                "208.67.16.113:8000",
                "125.227.35.196:6969",
                "59.36.96.77:6969",
                "87.253.152.137",
                "158.69.146.212:7777",
                "tracker.coppersurfer.tk:6969",
                "zer0day.ch:1337",
                "tracker.leechers-paradise.org:6969",
                "9.rarbg.com:2710",
                "p4p.arenabg.com:1337",
                "tracker.sktorrent.net:6969",
                "tracker.aletorrenty.pl:2710",
                "tracker.kicks-ass.net:80",
                "torrent.gresille.org:80",
                "tracker.skyts.net:6969",
                "tracker.yoshi210.com:6969",
                "tracker.tiny-vps.com:6969",
                "tracker.internetwarriors.net:1337",
                "mgtracker.org:2710",
                "tracker.filetracker.pl:8089",
                "tracker.ex.ua:80",
                "91.218.230.81:6969",
                "tracker.grepler.com:6969",
                "tracker.flashtorrents.org:6969",
                "tracker.bittor.pw:1337",
                "tracker.kuroy.me:5944",
                "182.176.139.129:6969",
                "open.stealth.si:80",
                "208.67.16.113:8000",
                "tracker.coppersurfer.tk:6969",
                "tracker.opentrackr.org:1337",
                "zer0day.ch:1337",
                "explodie.org:6969",
                "tracker.leechers-paradise.org:6969",
                "9.rarbg.com:2710",
                "p4p.arenabg.com:1337",
                "tracker.sktorrent.net:6969",
                "p4p.arenabg.com:1337",
                "tracker.aletorrenty.pl:2710",
                "tracker.aletorrenty.pl:2710",
                "tracker.bittorrent.am",
                "tracker.kicks-ass.net:80",
                "tracker.kicks-ass.net",
                "tracker.baravik.org:6970",
                "tracker.piratepublic.com:1337",
                "213.163.67.56:1337",
                "213.163.67.56:1337",
                "185.86.149.205:1337",
                "74.82.52.209:6969",
                "94.23.183.33:6969",
                "74.82.52.209:6969",
                "151.80.120.114:2710",
                "109.121.134.121:1337",
                "168.235.67.63:6969",
                "109.121.134.121:1337",
                "178.33.73.26:2710",
                "178.33.73.26:2710",
                "85.17.19.180",
                "85.17.19.180:80",
                "210.244.71.25:6969",
                "85.17.19.180"
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
                    peers.addAll(request(trackerHost, finalTrackerPort, infoHash));
                } catch (IOException e) {
                    trackerLog.println("[ERROR] In tracker: " + trackerHost + ":" + finalTrackerPort);
                    e.printStackTrace(trackerLog);
                }
            }).start();
        }
        Thread.sleep(4000); //等上面的线程结束
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

    public static void request(String tracckerHost, String trackerPort, String requestInfoHash){

    }
}