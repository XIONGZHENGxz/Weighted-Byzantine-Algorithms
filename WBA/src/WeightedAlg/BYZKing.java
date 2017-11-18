package WeightedAlg;

public class BYZKing {
    public int N;
    public int f;
    public float[] w;
    public String[] server;
    public int[] port;
    public ListenerKing ls;
    public int id;
    public float s0;
    public float s1;
    public float su;
    public int count;
    public boolean receKing;
    public int king;

    public BYZKing(int n, int f, float[] w, String[] server, int[] port, int id) {
        this.N = n;
        this.f = f;
        this.w = w;
        this.server = server;
        this.port = port;
        this.ls = new ListenerKing(port[id], this);
        ls.start();
        this.id = id;
        this.s0 = 0;
        this.s1 = 0;
        this.su = 0;
        this.count = n;
        this.receKing = false;
        this.king = 0;
    }

    public void handleRequest(Object request) {
        if(request instanceof BYZRequest) {
            count--;
            BYZRequest req = (BYZRequest)request;
            if(req.v == 1) {
                s1 = s1 + w[req.id];
            } else if (req.v == 0) {
                s0 = s0 + w[req.id];
            } else {
                su = su + w[req.id];
            }
        } else if (request instanceof KingValueReq) {
            receKing = true;
            KingValueReq req = (KingValueReq)request;
            king = req.v;
        }
    }

    public int BYZConsensus(int v) throws InterruptedException {
        for(int phase = 1; phase <= f + 1; phase++) {
            // First phase
            if(w[id] > 0) {
                for(int j = 0; j < server.length; j++) {
                    BYZRequest req = new BYZRequest(id, v);
                    Messager.sendMsg(req, server[j], port[j]);
                }
            }
            while (count > 0) {
                Thread.sleep(3000);
            }
            if(s1 > 2/3) {
                v = 1;
                w[id] = s1;
            } else if (s0 > 2/3) {
                v = 0;
                w[id] = s0;
            } else {
                v = 2;
            }

            // Second phase
            s0 = 0;
            s1 = 0;
            su = 0;
            count = N;
            if(w[id] > 0) {
                for(int j = 0; j < server.length; j++) {
                    BYZRequest req = new BYZRequest(id, v);
                    Messager.sendMsg(req, server[j], port[j]);
                }
            }
            while (count > 0) {
                Thread.sleep(3000);
            }
            if(s1 > 1/3) {
                v = 1;
                w[id] = s1;
            } else if (s0 > 1/3) {
                v = 0;
                w[id] = s0;
            } else if(su > 1/3){
                v = 2;
                w[id] = su;
            }

            // Third phase
            if(phase == id) {
                for(int j = 0; j != id && j < server.length; j++) {
                    KingValueReq req = new KingValueReq(id, v);
                    Messager.sendMsg(req, server[j], port[j]);
                    receKing = true;
                }
            }
            while(!receKing) {
                Thread.sleep(3000);
            }
            if(v == 2 || w[id] < 2/3) {
                if(king == 2) {
                    v = 1;
                } else {
                    v = king;
                }
            }
        }
        return v;
    }
}

class KingValueReq{
    int id;
    int v;
    public KingValueReq(int id, int v) {
        this.id = id;
        this.v = v;
    }
}
