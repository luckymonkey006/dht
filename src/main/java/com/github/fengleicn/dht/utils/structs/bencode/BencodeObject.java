package com.github.fengleicn.dht.utils.structs.bencode;

import com.github.fengleicn.dht.utils.Utils;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.fengleicn.dht.utils.Utils.getOrignalBytesString;

public class BencodeObject {
    private Object data;
    public final static Charset UNICODE_UTF8 = Charset.forName("UTF8");

    public BencodeObject() {
    }

    public <T> BencodeObject(T o) {
        set(o);
    }

    public <T> void add(T o) {
        if (data instanceof List) {
            ((List<BencodeObject>) data).add(new BencodeObject().set(o));
        } else {
            throw new RuntimeException("[ERROR] data is not a list");
        }
    }


    public <T> void put(byte[] k, T o) {
        if (data instanceof Map) {
            ((Map<byte[], BencodeObject>) data).put(k, new BencodeObject(o));
        } else {
            throw new RuntimeException("[ERROR] data is not a map");
        }
    }

    public <T> void put(String k, T o) {
        put(k.getBytes(UNICODE_UTF8), o);
    }

    public <T> BencodeObject set(T o) {
        if (o instanceof List || o instanceof Map || o instanceof BigInteger || o instanceof byte[]) {
            data = o;
        } else if (o instanceof String) {
            data = ((String) o).getBytes(UNICODE_UTF8);
        } else if (o instanceof Number) {
            if (o instanceof Double || o instanceof Float)
                throw new RuntimeException("illegal arg");
            data = new BigInteger(String.valueOf(((Number) o).longValue()));
        } else if (o instanceof BencodeObject) {
            data = ((BencodeObject) o).data;
        } else if(o == null) {
            throw new RuntimeException("arg is null");
        }else{
            throw new RuntimeException("illegal arg");
        }
        return this;
    }

    public BencodeObject get(byte[] k) {
        Map<byte[], BencodeObject> map = ((Map<byte[], BencodeObject>) data);
        Set<byte[]> keySet = map.keySet();
        for (byte[] key : keySet) {
            if (Utils.isBytesEqual(k, key)) {
                return map.get(key);
            }
        }
        System.err.println(this.toString());
        System.err.println("not find key-value");
        return null;
    }

    public BencodeObject get(String k) {
        byte[] key = k.getBytes(UNICODE_UTF8);
        return get(key);
    }

    public BencodeObject get(int k) {
        List<BencodeObject> list = (List<BencodeObject>) data;
        return list.get(k);
    }

    public <T> T fetch() {
        return (T) data;
    }

    public byte[] castToBytes() {
        return (byte[]) data;
    }

    public enum BencodeObjType {
        MAP, LIST, BYTES, BIG_INTEGER, ERROR
    }

    public BencodeObjType type() {
        if (data instanceof Map) {
            return BencodeObjType.MAP;
        } else if (data instanceof List) {
            return BencodeObjType.LIST;
        } else if (data instanceof BigInteger) {
            return BencodeObjType.BIG_INTEGER;
        } else if (data instanceof byte[]) {
            return BencodeObjType.BYTES;
        } else {
            return BencodeObjType.ERROR;
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (data instanceof Map) {
            sb.append("{");
            for (Map.Entry<byte[], BencodeObject> e : ((Map<byte[], BencodeObject>) data).entrySet()) {
                sb.append(getOrignalBytesString(e.getKey()));
                sb.append(" : ");
                sb.append(e.getValue());
                sb.append(" , ");
            }
            sb.replace(sb.length() - 3, sb.length(), "");
            sb.append("}");
            return sb.toString();
        } else if (data instanceof List) {
            sb.append("[");
            for (BencodeObject element : ((List<BencodeObject>) data)) {
                if (element.data instanceof byte[]) {
                    sb.append(new String(element.fetch(), UNICODE_UTF8));
                } else {
                    sb.append(element);
                }
                sb.append(" , ");
            }
            sb.replace(sb.length() - 3, sb.length(), "");
            sb.append("]");
            return sb.toString();
        }else if (data instanceof byte[]) {
            return new String(((byte[]) data), UNICODE_UTF8);
        } else {
            return data.toString();
        }
    }


}