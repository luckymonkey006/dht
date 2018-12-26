package com.github.fengleicn.dht.bencode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

// for Bencode encoding / decoding
public class BencodeUtil {
    static class BencodeObjectWrapper {
        BencodeObject bencodeObject;

        public BencodeObject getValue() {
            return bencodeObject;
        }

        public BencodeObject setValue(BencodeObject bencodeObject) {
            this.bencodeObject = bencodeObject;
            return bencodeObject;
        }
    }

    public static BencodeObject parse(final byte[] src) throws IOException {
        BencodeObjectWrapper bencodeObjectWrapper = new BencodeObjectWrapper();
        next(src, 0, bencodeObjectWrapper);
        return bencodeObjectWrapper.bencodeObject;
    }

    public static int next(final byte[] bencodeString, final int start, final BencodeObjectWrapper bencodeObjectWrapper) {
        byte firstByte = bencodeString[start];
        BencodeObject bencodeObj = new BencodeObject();
        bencodeObjectWrapper.setValue(bencodeObj);
        int pointer = start;
        switch (firstByte) {
            case 'd':
                pointer++;
                bencodeObj.set(new LinkedHashMap<byte[], BencodeObject>());
                while (bencodeString[pointer] != 'e') {
                    BencodeObjectWrapper bencodeObjWrapper = new BencodeObjectWrapper();
                    pointer = next(bencodeString, pointer, bencodeObjWrapper);
                    byte[] key = bencodeObjWrapper.bencodeObject.fetch();
                    pointer = next(bencodeString, pointer, bencodeObjWrapper);
                    bencodeObj.put(key, bencodeObjWrapper.bencodeObject);
                }
                return pointer + 1;
            case 'l':
                pointer++;
                bencodeObj.set(new ArrayList<BencodeObject>());
                while (bencodeString[pointer] != 'e') {
                    pointer = next(bencodeString, pointer, bencodeObjectWrapper);
                    bencodeObj.add(bencodeObjectWrapper.bencodeObject);
                }
                return pointer + 1;
            case 'i':
                pointer++;
                ByteArrayOutputStream bytesOs = new ByteArrayOutputStream();
                byte b;
                while ((b = bencodeString[pointer]) != 'e') {
                    if (b != '-' && !(b >= '0' && b <= '9')) {
                        throw new RuntimeException("[ERROR] BencodeUtil.next(). The field token is  " + b);
                    }
                    bytesOs.write(b);
                    pointer++;
                }
                byte[] number = bytesOs.toByteArray();
                if (number[0] == '-' && number[1] == '0' || number[0] == '0' && number.length != 1) {
                    throw new RuntimeException("[ERROR] BencodeUtil.next(). The field num is " + number);
                }
                bencodeObj.set(new BigInteger(new String(number, BencodeObject.UNICODE_UTF8)));
                return pointer + 1;
            default:
                if (firstByte == '0') {
                    bencodeObj.set(new byte[0]);
                    return pointer + 2;
                } else if (firstByte >= '1' && firstByte <= '9') {
                    bytesOs = new ByteArrayOutputStream();
                    while ((b = bencodeString[pointer]) != ':') {
                        if (!(b >= '0' && b <= '9')) {
                            throw new RuntimeException("[ERROR] BencodeUtil.next(). The field token is " + b);
                        }
                        bytesOs.write(b);
                        pointer++;
                    }
                    number = bytesOs.toByteArray();
                    if (number[0] == '0')
                        throw new RuntimeException("[ERROR] BencodeUtil.next(). The field num is " + number);
                    pointer++;
                    int len = Integer.valueOf(new String(number, BencodeObject.UNICODE_UTF8));
                    bencodeObj.set(Arrays.copyOfRange(bencodeString, pointer, pointer + len));
                    return pointer + len;
                } else {
                    throw new RuntimeException("[ERROR] BencodeUtil.next() occurrences error. The field firstByte is " + firstByte);
                }
        }
    }


    public static byte[] toBencodeString(BencodeObject bencodeObject) {
        byte[] ret = {};
        switch (bencodeObject.type()) {
            case BencodeObject.MAP:
                ret = new byte[]{'d'};
                Map<byte[], BencodeObject> map = bencodeObject.fetch();
                TreeMap<byte[], BencodeObject> treeMap = new TreeMap<>((byteArray1, byteArray2) -> {
                    int minLength = Math.min(byteArray1.length, byteArray2.length);
                    for (int i = 0; i < minLength; i++) {
                        if (byteArray1[i] != byteArray2[i]) {
                            return byteArray1[i] - byteArray2[i];
                        }
                    }
                    return 0;
                });
                treeMap.putAll(map);
                for (Map.Entry<byte[], BencodeObject> entry : treeMap.entrySet()) {
                    ret = bytesConcat(ret,
                            String.valueOf(entry.getKey().length).getBytes(BencodeObject.UNICODE_UTF8),
                            new byte[]{':'},
                            entry.getKey(),
                            toBencodeString(entry.getValue()));
                }
                break;
            case BencodeObject.LIST:
                ret = new byte[]{'l'};
                List<BencodeObject> list = bencodeObject.fetch();
                for (BencodeObject b : list) {
                    ret = bytesConcat(ret, toBencodeString(b));
                }
                break;
            case BencodeObject.BYTES:
                byte[] bytes = bencodeObject.fetch();
                ret = bytesConcat(("" + bytes.length).getBytes(BencodeObject.UNICODE_UTF8),
                        new byte[]{':'}, bytes);
                break;
            case BencodeObject.BIG_INTEGER:
                BigInteger i = bencodeObject.fetch();
                ret = bytesConcat(new byte[]{'i'}, i.toString().getBytes(BencodeObject.UNICODE_UTF8));
                break;
            case BencodeObject.ERROR:
                throw new RuntimeException("[ERROR] BencodeObject");
        }
        if (!bencodeObject.type().equals(BencodeObject.BYTES))
            ret = bytesConcat(ret, new byte[]{'e'});
        return ret;
    }

    private static byte[] bytesConcat(byte[]... bytesArg) {
        byte[] ret = {};
        for (byte[] i : bytesArg) {
            ret = bytesConcat0(ret, i);
        }
        return ret;
    }

    private static byte[] bytesConcat0(byte[] head, byte[] tail) {
        byte[] result = new byte[head.length + tail.length];
        System.arraycopy(head, 0, result, 0, head.length);
        System.arraycopy(tail, 0, result, head.length, tail.length);
        return result;
    }
}
