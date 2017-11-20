package byzantine.ds.ut;

import org.omg.PortableInterceptor.INACTIVE;

import java.util.Arrays;

public class Util {
    public static final int TIMEOUT = 2000;
    public static final String root = "/zookeeper";
    public static final int ZOO_TIMEOUT = 3000;
    public static final int PAXOS_TIMEOUT = 3000;
    public static final int paxosPort = 9999;
    public static final int clientPort = 8888;
    public static final boolean DEBUG = true;
    public static final long SLEEP = 100;

    public static void waitForTimeout() {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        while(end - start < TIMEOUT) {
            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            end = System.currentTimeMillis();
        }
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int computAlpha(double[] w, double r) {
        double[] tmp = Arrays.copyOf(w, w.length);
        Arrays.sort(tmp);
        double total = 0;
        int alpha  = 0;
        for(int i = tmp.length - 1; i >= 0; i--) {
            if(total > r) break;
            alpha ++;
        }
        return alpha;
    }

    public static long getCurrTime() {
        return System.currentTimeMillis();
    }
}
