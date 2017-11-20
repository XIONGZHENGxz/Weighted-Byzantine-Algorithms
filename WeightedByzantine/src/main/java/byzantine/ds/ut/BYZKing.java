package byzantine.ds.ut;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class BYZKing implements WeightedAlgorithm {
    public int N;
    public double r;
    public double[] w;
    public String[] server;
    public int[] port;
    public ListenerKing ls;
    public int id;
    public double s0;
    public double s1;
    public double myweight;
    public double su;
    public int count;
    public boolean receKing;
    public int king;
    public int alpha;
    public ReentrantLock lock;

    public BYZKing(int alpha, int n, double r, double[] w, String[] server, int[] port, int id) {
        this.alpha = alpha;
        this.N = n;
        this.r = r;
        this.w = w;
        this.server = server;
        this.port = port;
        this.ls = new ListenerKing(port[id], this);
        ls.start();
        Util.sleep(10);
        this.id = id;
        this.s0 = 0;
        this.s1 = 0;
        this.su = 0;
        myweight = 0;
        this.count = n;
        this.receKing = false;
        this.king = 0;
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
                } else if (req.v == 0) {
                    s0 = s0 + w[req.id];
                } else {
                    su = su + w[req.id];
                }
            } else if (request instanceof KingValueReq) {
                receKing = true;
                KingValueReq req = (KingValueReq) request;
                king = req.v;
            }
        } finally {
            lock.unlock();
        }
    }

    public int startAgreement (int v) {
        for(int phase = 0; phase < alpha; phase ++) {
            // First phase
            count = N;
            if(Util.DEBUG) System.out.println("start phase 1");
            if(w[id] > 0) {
                BYZRequest req = new BYZRequest(id, v);
                for(int j = 0; j < server.length; j++) {
                    if(j == id) handleRequest(req);
                    else Messager.sendMsg(req, server[j], port[j]);
                }
            }

            while (count > 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(Util.DEBUG) System.out.println("finish phase 1");
            //wait for first phase messages
            //Util.waitForTimeout();

            if(s0 >= 2.0/3.0) {
                v = 0;
            } else if (s1 >= 2.0/3.0) {
                v = 1;
            } else {
                v = 2;
            }

            // Second phase
            if(Util.DEBUG) System.out.println("start phase 2");
            s0 = 0;
            s1 = 0;
            su = 0;
            count = N;
            if(w[id] > 0) {
                BYZRequest req = new BYZRequest(id, v);
                for(int j = 0; j < server.length; j++) {
                    if(j == id) handleRequest(req);
                    else Messager.sendMsg(req, server[j], port[j]);
                }
            }


            while (count > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //Util.waitForTimeout();
            if(Util.DEBUG) System.out.println("finish phase 2");

            if(s0 > 1.0/3.0) {
                v = 0;
                myweight = s0;
            } else if (s1 > 1.0/3.0) {
                v = 1;
                myweight = s1;
            } else if(su > 1.0/3.0){
                v = 2;
                myweight = su;
            }

            // Third phase
            if(phase == id) {
                KingValueReq req = new KingValueReq(id, v);
                receKing = true;
                for(int j = 0; j < server.length; j++) {
                    if(j != id) Messager.sendMsg(req, server[j], port[j]);
                }
            }

            while(!receKing) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(v == 2 || myweight < 2.0/3.0) {
                if(king == 2) {
                    v = 1;
                } else {
                    v = king;
                }
            }
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
        BYZKing byz = new BYZKing(alpha, n, r, w, servers, ports, id);
        int out = byz.startAgreement(v);
        System.out.println(id + " decided " + out);

    }
}

class KingValueReq implements Serializable{
    static final long serialVersionUID=2L;
    int id;
    int v;

    public KingValueReq(int id, int v) {
        this.id = id;
        this.v = v;
    }
}
