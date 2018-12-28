package com.github.fengleicn.dht.utils.structs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KBucket {
    List<KBucketNode> buket;
    KBucketNode myKBucketNode;

    @SuppressWarnings("unchecked")
    public KBucket(KBucketNode myKBucketNode) {
        buket = new ArrayList<>();
        this.myKBucketNode = myKBucketNode;
    }

    public KBucketNode randomNode() throws Exception {
        Collections.shuffle(buket);
        return buket.get(0);
    }

    public synchronized void add(KBucketNode KBucketNode) {
        buket.add(KBucketNode);
        if(buket.size() > 10000){
            buket = new ArrayList<>(buket.subList(0, 10000/2));
        }
    }

    public List<KBucketNode> get(KBucketNode KBucketNode){
        Collections.shuffle(buket);
        return buket.subList(0, 20);
    }

}
