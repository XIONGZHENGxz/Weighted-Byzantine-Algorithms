package WeightedAlg;

public class BYZ {

    public int N;
    public int f;
    public float[] w;
    public String[] server;
    public int[] port;
    public Listener ls;
    public int id;
    public float s0;
    public float s1;
    public int count;
    public boolean receQueen;
    public int queen;

    public BYZ(int n, int f, float[] w, String[] server, int[] port, int id) {
        this.N = n;
        this.f = f;
        this.w = w;
        this.server = server;
        this.port = port;
        this.ls = new Listener(port[id], this);
        ls.start();
        this.id = id;
        s0 = 0;
        s1 = 0;
        this.count = n;
        this.receQueen = false;
        this.queen = 0;
    }


    public void handleRequest(Object request) {
        if(request instanceof BYZRequest) {
            count--;
            BYZRequest req = (BYZRequest)request;
            if(req.v == 1) {
                s1 = s1 + w[req.id];
            } else {
                s0 = s0 + w[req.id];
            }
        } else if (request instanceof QueenValueReq) {
            receQueen = true;
            QueenValueReq req = (QueenValueReq)request;
            queen = req.v;
        }
    }



    public int BYZConsensus(int v) throws InterruptedException {
        for(int phase = 1; phase <= f + 1; phase++) {
            if(w[id] > 0) {
                for(int j = 0; j < server.length; j++) {
                    BYZRequest req = new BYZRequest(id, v);
                    Messager.sendMsg(req, server[j], port[j]);
                }
            }
            while (count > 0) {
                Thread.sleep(3000);
            }
            if(s1 > 1/2) {
                v = 1;
                w[id] = s1;
            } else {
                v = 0;
                w[id] = s0;
            }
            if(phase == id) {
                for(int j = 0; j != id && j < server.length; j++) {
                    QueenValueReq req = new QueenValueReq(id, v);
                    Messager.sendMsg(req, server[j], port[j]);
                    receQueen = true;
                }
            }
            while(!receQueen) {
                Thread.sleep(3000);
            }
            if(w[id] <= 3/4) {
                v = queen;
            }


        }
        return v;
    }
}

class BYZRequest {
    int id;
    int v;
    public BYZRequest(int id, int v) {
        this.id = id;
        this.v = v;
    }
}

class QueenValueReq{
    int id;
    int v;
    public QueenValueReq(int id, int v) {
        this.id = id;
        this.v = v;
    }
}
