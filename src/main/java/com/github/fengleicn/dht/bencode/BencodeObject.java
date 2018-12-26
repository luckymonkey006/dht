package com.github.fengleicn.dht.bencode;

import com.github.fengleicn.dht.utils.DhtUtil;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Usage:
 * BencodeObject bencodeObject = ...;
 * bencodeObject.get(int, byte[], String) => bencodeObject;
 * bencodeObject.fetch() => map, list, bigInteger, byte[];
 * bencodeObject.castI/L/S => int, long, String of bencodeObject;
 * bencodeObject.set(int, long, string, byte[], map<byte[], BencodeObject>, list<BencodeObject>)
 * bencodeObject.put/add(...)
 */
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
            throw new RuntimeException("not a list");
        }
    }


    public <T> void put(byte[] k, T o) {
        if (data instanceof Map) {
            ((Map<byte[], BencodeObject>) data).put(k, new BencodeObject(o));
        } else {
            throw new RuntimeException("not a map");
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
            if (DhtUtil.byteArraysEqual(k, key)) {
                return map.get(key);
            }
        }
        System.err.println(this.toString());
        throw new RuntimeException("not find key-value");
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

    public byte[] castB() {
        return (byte[]) data;
    }

    public long castL() {
        return ((BigInteger) data).longValue();
    }

    public int castI() {
        return Math.toIntExact(castL());
    }

    public String castS() {
        byte[] bytes = fetch();
        return new String(bytes, UNICODE_UTF8);
    }

    public final static String MAP = "Map";
    public final static String LIST = "List";
    public final static String BYTES = "byte[]";
    public final static String BIG_INTEGER = "BigInteger";
    public final static String ERROR = "null";

    String type() {
        if (data instanceof Map) {
            return MAP;
        } else if (data instanceof List) {
            return LIST;
        } else if (data instanceof BigInteger) {
            return BIG_INTEGER;
        } else if (data instanceof byte[]) {
            return BYTES;
        } else {
            return ERROR;
        }
    }

    @Test
    public void test001() {

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (data instanceof Map) {
            sb.append("{");
            for (Map.Entry<byte[], BencodeObject> e : ((Map<byte[], BencodeObject>) data).entrySet()) {
                sb.append(new String(e.getKey(), UNICODE_UTF8));
                sb.append(" : ");
                sb.append(e.getValue().toString());
                sb.append(" , ");
            }
            sb.replace(sb.length() - 3, sb.length(), "");
            sb.append("}");
            return sb.toString();
        } else if (data instanceof List) {
            sb.append("[");
            for (BencodeObject o : ((List<BencodeObject>) data)) {
                if (o.data instanceof byte[]) {
                    sb.append(new String(o.fetch(), UNICODE_UTF8));
                } else {
                    sb.append(o.toString());
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