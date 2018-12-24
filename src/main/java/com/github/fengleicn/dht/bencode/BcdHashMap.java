package com.github.fengleicn.dht.bencode;

import java.util.HashMap;

public class BcdHashMap extends HashMap<byte[], BencodeObject> {
    public BencodeObject put(String key, BencodeObject value) {
        return super.put(key.getBytes(BencodeObject.DEFAULT_CHARSET), value);
    }
}
