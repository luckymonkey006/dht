package com.github.fengleicn.dht.modules;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InfoHashStorage {
    private volatile static InfoHashStorage instance;
    public static final int WEIGHT_ANNOUNCE = 50;
    public static final int WEIGHT_GET = 1;
    public static PrintWriter result;

    static {
        try {
            result = new PrintWriter(new FileWriter("metainfo.txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class TorrentInfo {
        String fileName;
        int weight;
    }

    private Map<String, TorrentInfo> storage = new ConcurrentHashMap<>();

    public static InfoHashStorage getInstance() {
        if (instance == null) {
            synchronized (InfoHashStorage.class) {
                if (instance == null) {
                    instance = new InfoHashStorage();
                    new Thread(() -> instance.downloadMetadata()).start();
                }
            }
        }
        return instance;
    }

    private InfoHashStorage() {
    }

    public synchronized void addInfoHash(String infoHash, int weight) {
        TorrentInfo torrentInfo = storage.get(infoHash);
        if (torrentInfo == null) {
            torrentInfo = new TorrentInfo();
            torrentInfo.weight = weight;
        } else {
            torrentInfo.weight += weight;
        }
        storage.put(infoHash, torrentInfo);
    }

    public void downloadMetadata() {
        for (; ; ) {
            try {
                Thread.sleep(1000);
                if (storage.size() > 1000) {
                    synchronized (this) {
                        storage.clear();
                    }
                    continue;
                }
                if (storage.entrySet().isEmpty()) {
                    continue;
                }
                List<Map.Entry<String, TorrentInfo>> l = new ArrayList<>(storage.entrySet());
                l.sort((o1, o2) -> {
                    int o1Scale = o1.getValue().fileName != null ? 0 : 1;
                    int o2Scale = o2.getValue().fileName != null ? 0 : 1;
                    return o2.getValue().weight * o2Scale - o1.getValue().weight * o1Scale;
                });
                if (l.get(0).getValue().weight > 400) {
                    l.get(0).getValue().weight -= 400;
                } else {
                    l.get(0).getValue().weight = 0;
                }
                new Thread(() -> {
                    try {
                        TrackerServer.request(l.get(0).getKey());
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void recordMetaData(String infoHash, String fileName) {
        synchronized (this) {
            if (infoHash.equalsIgnoreCase(StartUp.GET_PEER_INFO_HASH))
                return;
            TorrentInfo torrentInfo = storage.get(infoHash);
            if (!fileName.equals(torrentInfo.fileName)) {
                torrentInfo.fileName = fileName;
                result.println(new Date().toString() + " " + infoHash + "    " + fileName);
                result.flush();
            }
        }
    }
}
