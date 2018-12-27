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
    public static final int ANNOUNCE = 1;
    public static final int GET = 2;
    public static PrintWriter result;

    static {
        try {
            result = new PrintWriter(new FileWriter("result.txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class TorrentInfo {
        String content;
        int weight;
    }

    Map<String, TorrentInfo> storage = new ConcurrentHashMap<>();

    public static InfoHashStorage getInstance() {
        if (instance == null) {
            synchronized (InfoHashStorage.class) {
                if (instance == null) {
                    instance = new InfoHashStorage();
                    new Thread(() -> {
                        instance.downloadMetadata();
                    }).start();
                }
            }
        }
        return instance;
    }

    private InfoHashStorage() {
    }

    public synchronized void addInfoHash(String infoHash, int type) {
        int weight;
        switch (type) {
            case ANNOUNCE:
                weight = 50;
                break;
            case GET:
                weight = 1;
                break;
            default:
                throw new RuntimeException("type error");
        }
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

        while (true) {
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
                    if (o1.getValue().content != null && o2.getValue().content != null) {
                        return 0;
                    } else if (o1.getValue().content != null) {
                        return 1;
                    } else if (o2.getValue().content != null) {
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

    public void recordMetaData(String infoHash, String data) {
        synchronized(this) {
            if(infoHash.equalsIgnoreCase(TaskManager.GET_PEER_INFO_HASH))
                return;
            TorrentInfo torrentInfo = storage.get(infoHash);
            if (!data.equals(torrentInfo.content)) {
                result.write(new Date().toString() + " " + infoHash + "\t" + data + "\n");
                result.flush();
                torrentInfo.content = data;
            }
        }
    }
}
