package com.github.fengleicn.dht.utils.structs;

import java.math.BigInteger;
import java.util.*;

public class KBucket {
    List<KBucketNode>[] buket;
    KBucketNode myKBucketNode;
    final static int K = 20;
    final static int KB_SIZE = 160;

    @SuppressWarnings("unchecked")
    public KBucket(KBucketNode myKBucketNode) {
        buket = new List[KB_SIZE];
        for (int i = 0; i < KB_SIZE; i++) {
            buket[i] = new Vector<>();
        }
        this.myKBucketNode = myKBucketNode;
    }

    void remove(List<KBucketNode> list) {
        list.remove(0);
    }

    public BigInteger xor(KBucketNode KBucketNode1, KBucketNode KBucketNode2) {
        byte[] buf = new byte[21];
        for (int i = 1; i < 21; i++) {
            buf[i] = (byte) (KBucketNode1.nodeId[i - 1] ^ KBucketNode2.nodeId[i - 1]);
        }
        return new BigInteger(buf);
    }

    public KBucketNode randomNode() throws Exception {
        Random r  = new Random();
        List<Integer> notEmptyList = new ArrayList<>();
        for (int i = 0; i < KB_SIZE; i++){
            if(buket[i].size() != 0){
                notEmptyList.add(i);
            }
        }
        if(notEmptyList.size() == 0)
            throw new Exception("notEmptyList is empty");
        int i = notEmptyList.get(r.nextInt(notEmptyList.size()));
        int size = buket[i].size();
        if(Arrays.equals(buket[i].get(r.nextInt(size)).ip, myKBucketNode.ip)){
            throw new Exception("a loop udp");
        }
        return buket[i].get(r.nextInt(size));
    }

    public synchronized void add(KBucketNode KBucketNode) {
        BigInteger bigInteger = xor(KBucketNode, myKBucketNode);
        for (int i = 1; i <= KB_SIZE; i++) {
            if (bigInteger.compareTo(BigInteger.valueOf(2).pow(i)) < 0) {
                if(Arrays.equals(KBucketNode.ip, myKBucketNode.ip)){
                    return;
                }
                if (buket[i - 1].size() >= K) {
                    remove(buket[i - 1]);
                }
                for(KBucketNode bucketKBucketNode : buket[i - 1]){
                    if(Arrays.equals(bucketKBucketNode.nodeId, KBucketNode.nodeId)){
                        bucketKBucketNode.ip = KBucketNode.ip;
                        bucketKBucketNode.port = KBucketNode.port;
                        return;
                    }
                }
                buket[i - 1].add(KBucketNode);
                return;
            }
        }
    }

    public List<KBucketNode> get(KBucketNode KBucketNode){
        BigInteger bigInteger = xor(KBucketNode, myKBucketNode);
        for (int i = 1; i <= KB_SIZE; i++) {
            if (bigInteger.compareTo(BigInteger.valueOf(2).pow(i)) < 0) {
                return buket[i - 1];
            }
        }
        return null;
    }

    public int nodeSize(){
        int sum = 0;
        for (List<KBucketNode> list : buket){
            sum += list.size();
        }
        return sum;
    }
}
