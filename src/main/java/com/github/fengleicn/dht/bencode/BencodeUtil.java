package com.github.fengleicn.dht.bencode;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.*;

import static com.github.fengleicn.dht.bencode.BencodeObject.UNICODE_UTF8;
import static com.github.fengleicn.dht.utils.Utils.*;

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

    public static BencodeObject parse(final byte[] src) {
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
                byte byteLocalPtr;
                while ((byteLocalPtr = bencodeString[pointer]) != 'e') {
                    if (byteLocalPtr != '-' && !(byteLocalPtr >= '0' && byteLocalPtr <= '9')) {
                        throw new RuntimeException("[ERROR] BencodeUtil.next(). The field token is  " + byteLocalPtr);
                    }
                    bytesOs.write(byteLocalPtr);
                    pointer++;
                }
                byte[] number = bytesOs.toByteArray();
                if (number[0] == '-' && number[1] == '0' || number[0] == '0' && number.length != 1) {
                    throw new RuntimeException("[ERROR] BencodeUtil.next(). The field number is " + Arrays.toString(number));
                }
                bencodeObj.set(new BigInteger(new String(number, UNICODE_UTF8)));
                return pointer + 1;
            default:
                if (firstByte == '0') {
                    bencodeObj.set(new byte[0]);
                    return pointer + 2;
                } else if (firstByte >= '1' && firstByte <= '9') {
                    bytesOs = new ByteArrayOutputStream();
                    while ((byteLocalPtr = bencodeString[pointer]) != ':') {
                        if (!(byteLocalPtr >= '0' && byteLocalPtr <= '9')) {
                            throw new RuntimeException("[ERROR] BencodeUtil.next(). The field token is " + byteLocalPtr);
                        }
                        bytesOs.write(byteLocalPtr);
                        pointer++;
                    }
                    number = bytesOs.toByteArray();
                    if (number[0] == '0')
                        throw new RuntimeException("[ERROR] BencodeUtil.next(). The field number is " + Arrays.toString(number));
                    pointer++;
                    int bytesLength = Integer.valueOf(new String(number, UNICODE_UTF8));
                    bencodeObj.set(Arrays.copyOfRange(bencodeString, pointer, pointer + bytesLength));
                    return pointer + bytesLength;
                } else {
                    throw new RuntimeException("[ERROR] BencodeUtil.next() occurrences error. The field firstByte is " + firstByte);
                }
        }
    }


    public static byte[] toBencodeString(BencodeObject bencodeObject) {
        byte[] ret = null;
        switch (bencodeObject.type()) {
            case MAP:
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
                            String.valueOf(entry.getKey().length).getBytes(UNICODE_UTF8),
                            getBytesFromChar(':'),
                            entry.getKey(),
                            toBencodeString(entry.getValue()));
                }
                break;
            case LIST:
                ret = new byte[]{'l'};
                List<BencodeObject> list = bencodeObject.fetch();
                for (BencodeObject b : list) {
                    ret = bytesConcat(ret, toBencodeString(b));
                }
                break;
            case BYTES:
                byte[] bytes = bencodeObject.fetch();
                ret = bytesConcat(("" + bytes.length).getBytes(UNICODE_UTF8), getBytesFromChar(':'), bytes);
                break;
            case BIG_INTEGER:
                BigInteger i = bencodeObject.fetch();
                ret = bytesConcat(getBytesFromChar('i'), i.toString().getBytes(UNICODE_UTF8));
                break;
            case ERROR:
                throw new RuntimeException("[ERROR] BencodeObject has a error.");
        }
        if (!bencodeObject.type().equals(BencodeObject.BencodeObjType.BYTES))
            ret = bytesConcat(ret, new byte[]{'e'});
        return ret;
    }


}
