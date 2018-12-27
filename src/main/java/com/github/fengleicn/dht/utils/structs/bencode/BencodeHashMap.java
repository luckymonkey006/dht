package com.github.fengleicn.dht.utils.structs.bencode;

import java.util.HashMap;

public class BencodeHashMap extends HashMap<byte[], BencodeObject> {
    public BencodeObject put(String key, BencodeObject value) {
        return super.put(key.getBytes(BencodeObject.UNICODE_UTF8), value);
    }
}
