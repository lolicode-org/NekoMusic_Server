package org.lolicode.nekomusic.utils;

import java.util.Random;

public class RandomChineseIP {
    private static final int start = ipToInt("43.247.176.0");
    private static final int end = ipToInt("43.247.191.255");
    private static final Random random = new Random();

    public static String getRandomChineseIP() {
        return intToIP(start + random.nextInt(end - start));
    }

    private static int ipToInt(String ipAddress) {
        String[] ipParts = ipAddress.split("\\.");
        int ip = 0;
        for (int i = 0; i < 4; i++) {
            ip |= Integer.parseInt(ipParts[i]) << (24 - (8 * i));
        }
        return ip;
    }

    private static String intToIP(int ip) {
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
    }
}

