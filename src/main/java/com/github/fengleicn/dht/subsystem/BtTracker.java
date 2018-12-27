package com.github.fengleicn.dht.subsystem;

import com.github.fengleicn.dht.node.KBucketNode;
import com.github.fengleicn.dht.starter.BtInfoFinder;
import com.github.fengleicn.dht.utils.Utils;
import org.assertj.core.util.Lists;

import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class BtTracker {
    static final int SEC = 1000;
    static final int TCP_TIMEOUT = 20 * SEC;
    static final int UDP_TIMEOUT = 5 * SEC;

    public static FileWriter trackerLogger;
    private static final Random random = new Random();


    static {
        try {
            trackerLogger = new FileWriter("tracker.txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] zeroByte36 = new byte[36];
    public static byte[] zeroByte4 = new byte[4];
    public static Set<String> blackListSet = Collections.synchronizedSet(new HashSet<>());
    public static int TRANS_ID = 0xCAFEBABE;

    static {
        blackListSet.add("0.0.0.0:0");
        blackListSet.add("0.0.0.0:65535");
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        new BtTracker().request("EEB7C79987A49F3CA816A951C404350A83C23C3C");

    }

    public static void request(String infoHash) throws InterruptedException, IOException {
        KBucketNode localKBucketNode = BtInfoFinder.localKBucketNode;
        if (localKBucketNode != null)
            blackListSet.add(localKBucketNode.getIp() + ":" + localKBucketNode.getPort());

        String[] addresses = {
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
                "agusiq-torrents.pl:6969"
        };

        Set<String> addrSet = new HashSet<>();
        for (String address : addresses) {
            if (random.nextBoolean() && random.nextBoolean()) {
                continue;
            }
            String[] addrSplit = address.split(":");
            String host = addrSplit[0];
            int port = Integer.valueOf(addrSplit[1]);
            new Thread(() -> {
                try {
                    request(host, port, infoHash, addrSet);
                } catch (IOException e) {
                    try {
                        trackerLogger.write("tracker " + host + ":" + port + " Error" + "\n");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }).start();
        }
        Thread.sleep(4000); //等上面的线程结束
        trackerLogger.write("Downloading " + infoHash + ": \n" + "list： " + addrSet.toString() + "\n");
        trackerLogger.flush();
        Set<String> saveSet = addrSet;
        int MAX = 500;
        if(addrSet.size() > MAX){
            List<String> objList = Lists.newArrayList(addrSet);
            saveSet = new HashSet<>(objList.subList(0, MAX));
        }
        for (String addr : saveSet) {
            Thread.sleep(1);
            new Thread(() -> {
                ExtendBepNo9 extendBepNo9 = new ExtendBepNo9();
                Socket socket = null;
                try {
                    String[] remtAddr = addr.split(":");
                    socket = new Socket(remtAddr[0], Integer.valueOf(remtAddr[1]));
                    socket.setSoTimeout(TCP_TIMEOUT);
                    String isOk = extendBepNo9.request(socket, infoHash);
                    if (isOk == null) {
                        blackListSet.add(remtAddr[0] + ":" + remtAddr[1]);
                    }
                    socket.close();
                } catch (Exception ignored) {
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }).start();
        }
    }

    public static void request(String host, int port, String infoHash, Set<String> addrList) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(new InetSocketAddress("0.0.0.0", 0));
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
        try{
            socketAddress  = new InetSocketAddress(host, port);
            packet = new DatagramPacket(buf, buf.length, socketAddress);
        }catch (Exception e){
            System.err.println(host + "  " + port);
            return;
        }
        datagramSocket.send(packet);

        try {
            datagramSocket.receive(packet);
        } catch (Exception e) {
//            e.printStackTrace();
            datagramSocket.close();
            return;
        }

        buf = packet.getData();
        byte[] transIdRecvBytes = Arrays.copyOfRange(buf, 4, 8);
        byte[] connIdRecvBytes = Arrays.copyOfRange(buf, 8, 16);
        int transIdRecv = ByteBuffer.allocate(4).put(transIdRecvBytes).getInt(0);
        if (transIdRecv != TRANS_ID)
            return;

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
                .putInt(TRANS_ID).putInt(256).put(zeroByte4).array();
        packet = new DatagramPacket(buf, buf.length, new InetSocketAddress(host, port));
        datagramSocket.send(packet);
        int LEN = 1694;
        packet = new DatagramPacket(new byte[LEN], LEN);

        try {
            datagramSocket.receive(packet);
        } catch (Exception e) {
//            e.printStackTrace();
            datagramSocket.close();
            return;
        }

        int packetLength = packet.getLength();
        buf = packet.getData();
        datagramSocket.close();
        for (int i = 20; i + 6 <= packetLength; i += 6) {
            byte[] remoteAddress = Arrays.copyOfRange(buf, i, i + 6);
            if (blackListSet.contains(Utils.getHostIpFromBytes(remoteAddress) + ":" + Utils.getIntFromBytes(remoteAddress)))
                continue;
            String peerIp = Utils.getHostIpFromBytes(remoteAddress);
            int peerPort = Integer.parseInt(Utils.getIntFromBytes(remoteAddress));
            addrList.add(peerIp + ":" + peerPort);
        }
    }
}