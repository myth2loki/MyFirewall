package com.qianyu.firewall.vpn;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.qianyu.firewall.BuildConfig;
import com.qianyu.firewall.R;
import com.qianyu.firewall.vpn.tcpip.CommonMethods;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FireWallVpnService extends VpnService {
    private static final String TAG = "FireWallVpnService";

    private static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything

    private ParcelFileDescriptor mVpnInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        initVpn();
        initInterceptor();
    }

    private void initVpn() {
        mVpnInterface = new Builder()
                .addAddress(VPN_ADDRESS, 32)
                .addRoute(VPN_ROUTE, 0)
                .setSession(getString(R.string.app_name))
//                .setConfigureIntent()
                .establish();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "initVpn: mVpnInterface = " + mVpnInterface);
        }
    }

    private void initInterceptor() {
        final FileInputStream fis = new FileInputStream(mVpnInterface.getFileDescriptor());
        final byte[] buff = new byte[20000];
        new Thread() {
            public void run() {
                while (true) {
                    try {
                        int length = fis.read(buff);
                        if (length == 0) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Packet packet = new Packet(buff, length);

                            if (packet.isTcp()) {
                                Log.d(TAG, "run: tcp = " + packet);

                            } else if (packet.isUdp()) {
                                if (packet.getIPHeader().getSourceIP() == CommonMethods.ipStringToInt(VPN_ADDRESS)
                                        && packet.getUdpHeader().getDestinationPort() == 53) {
                                    //这是dns packet
                                    Log.d(TAG, "run: dns request == " +
                                            CommonMethods.ipIntToString(packet.getIPHeader().getDestinationIP())
                                            + ":" + packet.getUdpHeader().getDestinationPort());
                                }
                            } else if (packet.isIcmp()) {

                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
