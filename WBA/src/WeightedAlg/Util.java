package WeightedAlg;

public class Util {
    public static final int TIMEOUT = 1000;
    public static final String root = "/zookeeper";
    public static final int ZOO_TIMEOUT = 3000;
    public static final int PAXOS_TIMEOUT = 3000;
    public static final int paxosPort = 9999;
    public static final int clientPort = 8888;
    public static final boolean DEBUG = true;
    public static long getCurrTime() {
        return System.currentTimeMillis();
    }
}
