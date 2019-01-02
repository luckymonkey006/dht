package com.github.fengleicn.dht.utils.structs;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
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

    public synchronized void add(KBucketNode kBucketNode) {
        if(Arrays.equals(myKBucketNode.ip, kBucketNode.ip)) {
            return;
        }
        final int MAX = 10000;
        buket.add(kBucketNode);
        if(buket.size() > MAX){
            buket = new ArrayList<>(buket.subList(0, MAX / 2));
        }
    }

    public List<KBucketNode> get(KBucketNode KBucketNode){
        Collections.shuffle(buket);
        return buket.subList(0, 20);
    }

}
