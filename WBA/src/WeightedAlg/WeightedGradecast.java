package WeightedAlg;

import java.util.*;

public class WeightedGradecast {

    public int n; //number of processes
    public String[] procs;
    public int[] ports;
    public Set<Integer> bad;
    public int id; //my id
    public int value; //my preferred value
    public ListenerGrade listener;
    public double[] w; //weights
    public GradecastMsg[] msgs; //store msg received from others
    public Map<Integer, GradecastMsg[]> msgs2; //store msgs received in phase 2
    public double r; //maximum weights that can fail
    public Map<Integer, GradecastMsg[]> msgs3; //store msgs received in phase 3
    public List<int[]> grades; //score for each leader

    public WeightedGradecast(int value, int n, String[] ps, int[] ports, int id, double[] w, double r) {
        this.value = value;
        this.n = n;
        this.procs = ps;
        this.ports = ports;
        bad = new HashSet<>();
        this.id = id;
        this.listener = new ListenerGrade(ports[id], this);
        this.msgs = new GradecastMsg[n];
        msgs2 = new HashMap<>();
        this.w = w;
        this.r = r;
        msgs3 = new HashMap<>();
        this.grades = new ArrayList<>();
    }

    public void gradecast() {
        GradecastMsg msg = new GradecastMsg(id, value);
        //phase 1
        for(int i = 0; i < n; i++) {
            if(i == id) {
                //me
               this.handleGrade(msg);
            } else {
                Messager.sendMsg(msg, procs[i], ports[i]);
            }
        }
        waitForTimeout();
        //phase 2
        for(int i = 0; i < n; i++) {
            if(i == id) {
                this.handleGrade(msgs[i]);
            } else {
                Messager.sendMsg(msgs[i], procs[i],ports[i]);
            }
        }
        waitForTimeout();
        //phase 3
        for(int i = 0; i < n; i++) {
            double[] m = getMajority(msgs2, i);
            if(m[1] >= 1 - r) {
                GradecastMsg gradecastMsg = new GradecastMsg(i, (int)m[0]);
                broadcast(gradecastMsg);
            }
        }
        waitForTimeout();
        for(int i = 0; i < n; i++) {
            double[] m = getMajority(msgs3, i);
            int confidence = 0;
            int value = -1;
            if(m[1] >= 1 - r) {
                value = (int)m[0];
                confidence = 2;
            } else if(m[1] > r) {
                value = (int) m[0];
                confidence = 1;
            } else {
                confidence = 0;
            }
            grades.add(new int[]{value, confidence});
        }

    }

    public void broadcast(GradecastMsg msg) {
        for(int i = 0; i < n; i++) {
            if (i == id) {
                //me
                this.handleGrade(msg);
            } else {
                Messager.sendMsg(msg, procs[i], ports[i]);
            }
        }
    }
    public double[] getMajority(Map<Integer, GradecastMsg[]> map, int leader) {
        double[] res = new double[2];
        double c0 = 0, c1 = 0;
        GradecastMsg[] msgs = map.get(leader);
        for(int i = 0; i < n; i++) {
            GradecastMsg m = msgs[i];
            if(m == null) continue;
            if(m.value == 0) c0 += w[i];
            else c1 += w[i];
        }
        if(c0 > c1) {
            res[0] = 0;
        }
        else res[0] = 1;
        res[1] = Math.max(c0, c1);
        return res;
    }

    public void waitForTimeout() {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        while(end - start < Util.TIMEOUT) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleGrade(GradecastMsg msg) {

    }

}
