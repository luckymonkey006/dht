package com.github.fengleicn.dht.modules;

import com.github.fengleicn.dht.utils.Utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ExtendBepNo9 {



    public Boolean request(InputStream inputStream, OutputStream outputStream, String infoHash) throws Exception {
        byte[] prefix = {
                19, 66, 105, 116, 84, 111, 114, 114, 101, 110, 116, 32, 112, 114,
                111, 116, 111, 99, 111, 108, 0, 0, 0, 0, 0, 16, 0, 0,
        };
        byte[] infoHashByte = Utils.getBytesFromHex(infoHash);
        byte[] peerId = Utils.randomBytes(20);
        byte[] handShakeHeader = ByteBuffer.allocate(68).put(prefix).put(infoHashByte).put(peerId).array();
        outputStream.write(handShakeHeader);
        outputStream.flush();

        byte[] remoteHSHeader = new byte[68];
        inputStream.read(remoteHSHeader);

        if (!Utils.isBytesEqual(Arrays.copyOfRange(remoteHSHeader, 0, 20), Arrays.copyOfRange(prefix, 0, 20)) || remoteHSHeader[25] != 16) {
            return null;
        }
        String extHandShakePayload = "d1:md11:ut_metadatai1eee";
        int extHandShakeLength = 2 + extHandShakePayload.length() + 4;
        byte[] extHandShake = ByteBuffer.allocate(extHandShakeLength).putInt(2 + extHandShakePayload.length())
                .put((byte) 20).put((byte) 0).put(extHandShakePayload.getBytes(StandardCharsets.UTF_8)).array();
        outputStream.write(extHandShake);
        outputStream.flush();

        byte[] remoteHsContentLenByte = new byte[4];
        inputStream.read(remoteHsContentLenByte);

        int remoteHsContentLenInt = ByteBuffer.allocate(4).put(remoteHsContentLenByte).getInt(0);
        if (remoteHsContentLenInt > 100000) {
            return null;
        }
        byte[] remoteHsContent = new byte[remoteHsContentLenInt];
        inputStream.read(remoteHsContent);

        if (remoteHsContent[0] != 20 || remoteHsContent[1] != 0) {
            return null;
        }
        String req = "d8:msg_typei0e5:piecei0ee";
        int requestLen = 2 + req.length() + 4;
        byte[] requestMD = ByteBuffer.allocate(requestLen).putInt(2 + req.length()).put((byte) 20).put((byte) 2).put(req.getBytes("ASCII")).array();
        outputStream.write(requestMD);
        outputStream.flush();

        Thread.sleep(3000);

        int loopTimes1 = 0;
        int loopTimes2 = 0;
        end:
        while (true) {
            if (inputStream.read() != '4') {
                if (loopTimes2 > 10000) {
                    break;
                }
                loopTimes2++;
                continue;
            }
            byte[] pattern = new byte[5];
            inputStream.read(pattern);
            if (Utils.isBytesEqual(pattern, ":name".getBytes())) {
                StringBuilder sb = new StringBuilder();
                for (; ; ) {
                    int c = inputStream.read();
                    if (c >= '0' && c <= '9') {
                        sb.append(Character.toChars(c));
                        if(sb.length() > 5){
                            break end;
                        }
                    } else if (c == ':') {
                        int nameLen = Integer.valueOf(sb.toString());
                        if (nameLen > 10000) {
                            return null;
                        }
                        byte[] name = new byte[nameLen];
                        inputStream.read(name);

                        InfoHashStorage.getInstance().recordMetaData(infoHash, new String(name, "utf8"));
                        break end;
                    } else {
                        break end;
                    }
                }
            } else {
                if (loopTimes1 > 500) {
                    return null;
                }
                loopTimes1++;
                continue;
            }
        }
        return true;
    }
}
