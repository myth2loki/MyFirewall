package com.qianyu.firewall.vpn;

import com.qianyu.firewall.vpn.tcpip.IPHeader;
import com.qianyu.firewall.vpn.tcpip.TCPHeader;
import com.qianyu.firewall.vpn.tcpip.UDPHeader;

/**
 * Created by Administrator on 2018/4/23.
 */

public class Packet {
    private IPHeader mIPHeader;
    private TCPHeader mTcpHeader;
    private UDPHeader mUdpHeader;
    private byte[] mBuff;

    public Packet(byte[] buff, int length) {
        mBuff = new byte[length];
        System.arraycopy(buff, 0, mBuff, 0, length);
        mIPHeader = new IPHeader(mBuff, 0);
        if (isTcp()) {
            mTcpHeader = new TCPHeader(mBuff, 20);
        } else if (isUdp()) {
            mUdpHeader = new UDPHeader(mBuff, 20);
        }
    }

    public boolean isTcp() {
        return mIPHeader.getProtocol() == IPHeader.TCP;
    }

    public boolean isUdp() {
        return mIPHeader.getProtocol() == IPHeader.UDP;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "mIPHeader=" + mIPHeader +
                ", mTcpHeader=" + mTcpHeader +
                ", mUdpHeader=" + mUdpHeader +
                '}';
    }
}
