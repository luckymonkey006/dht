package com.github.fengleicn.dht.modules;

import com.github.fengleicn.dht.main.TaskManager;

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
            result = new PrintWriter(new FileWriter("result.txt", true));
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
        for (;;) {
            try {
                Thread.sleep(500);
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
                    if (o1.getValue().fileName != null && o2.getValue().fileName != null) {
                        return 0;
                    } else if (o1.getValue().fileName != null) {
                        return 1;
                    } else if (o2.getValue().fileName != null) {
                        return -1;
                    } else {
                        return o2.getValue().weight - o1.getValue().weight;
                    }
                });
                l.get(0).getValue().weight -= 100;
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
        synchronized(this) {
            if(infoHash.equalsIgnoreCase(TaskManager.GET_PEER_INFO_HASH))
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
