package byzantine.ds.ut;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;

public class BYZQueen implements WeightedAlgorithm{

    public int N;
    public double r;
    public double[] w;
    public String[] server;
    public int[] port;
    public ListenerQueen ls;
    public int id;
    public double s0;
    public double s1;
    public int count;
    public double myweight;
    public boolean receQueen;
    public int queen;
    public int alpha;
    public ReentrantLock lock;

    public BYZQueen(int alpha, int n, double r, double[] w, String[] server, int[] port, int id) {
        this.alpha = alpha;
        this.N = n;
        this.r = r;
        this.w = w;
        this.server = server;
        this.port = port;
        this.ls = new ListenerQueen(port[id], this);
        ls.start();
        Util.sleep(100);
        this.id = id;
        s0 = 0;
        s1 = 0;
        myweight = 0;
        this.count = n;
        this.receQueen = false;
        this.queen = 0;
        this.lock = new ReentrantLock();
    }


    public void handleRequest(Object request) {
        lock.lock();
        try {
            if (request instanceof BYZRequest) {
                count--;
                BYZRequest req = (BYZRequest) request;
                if (req.v == 1) {
                    s1 = s1 + w[req.id];
                } else {
                    s0 = s0 + w[req.id];
                }
            } else if (request instanceof QueenValueReq) {
                receQueen = true;
                QueenValueReq req = (QueenValueReq) request;
                queen = req.v;
            }
        } finally {
            lock.unlock();
        }

    }



    public int startAgreement(int v) {
        for(int phase = 0; phase < alpha; phase++) {
            count = N;
            if(Util.DEBUG) System.out.println("start phase 1");
            if(w[id] > 0) {
                for(int j = 0; j < server.length; j++) {
                    BYZRequest req = new BYZRequest(id, v);
                    if(j == id) handleRequest(req);
                    else Messager.sendMsg(req, server[j], port[j]);
                }
            }
            if(Util.DEBUG) System.out.println("finish phase 1");

            while (count > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

           //Util.waitForTimeout();
            if(Util.DEBUG) System.out.println("start phase 2");

            if(s1 > 1/2) {
                v = 1;
                myweight = s1;
            } else {
                v = 0;
                myweight = s0;
            }

            //phase 2
            if(phase == id) {
                QueenValueReq req = new QueenValueReq(id, v);
                receQueen = true;
                for(int j = 0; j < server.length; j++) {
                    if(j != id) {
                        Messager.sendMsg(req, server[j], port[j]);
                    }
                }
            }

            //wait for queen value
            while(!receQueen) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(myweight <= 3.0/4.0) {
                v = queen;
            }

            if(Util.DEBUG) System.out.println("finish phase 2");

        }

        return v;
    }

    public static void main(String...args) {
        // id value N weights ips r
        int id = Integer.parseInt(args[0]);
        int v = Integer.parseInt(args[1]);
        int n = Integer.parseInt(args[2]);
        String[] servers = new String[n];
        int[] ports = new int[n];
        double[] w = new double[n];
        ConfigReader.readWeights(args[3], w);

        ConfigReader.readServers(args[4], servers);

        for(int i = 0; i < n; i++) {
            ports[i] = 5555 + i;
        }


        double r = Double.parseDouble(args[5]);
        int alpha = Util.computAlpha(w, r);
        if(Util.DEBUG) System.out.println("alpha "+alpha);
        BYZQueen byz = new BYZQueen(alpha, n, r, w, servers, ports, id);
//        Util.sleep(5000);
        int out = byz.startAgreement(v);
        System.out.println(id + " decided " + out);

    }


}

class BYZRequest implements Serializable{
    static final long serialVersionUID=2L;
    int id;
    int v;
    public BYZRequest(int id, int v) {
        this.id = id;
        this.v = v;
    }
}

class QueenValueReq implements Serializable{
    static final long serialVersionUID=2L;
    int id;
    int v;
    public QueenValueReq(int id, int v) {
        this.id = id;
        this.v = v;
    }
}
