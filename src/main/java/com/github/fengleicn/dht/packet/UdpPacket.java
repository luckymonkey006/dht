package com.github.fengleicn.dht.packet;

import com.github.fengleicn.dht.bencode.BencodeObject;

import java.net.InetSocketAddress;

public class UdpPacket {
    public InetSocketAddress address;
    public BencodeObject bencodeObject;

    public UdpPacket(InetSocketAddress address, BencodeObject bencodeObject) {
        this.address = address;
        this.bencodeObject = bencodeObject;
    }
}