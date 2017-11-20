package byzantine.ds.ut;

import java.util.*;

public class BadWeightedGradecast extends WeightedGradecast {
    Random rand;
    public BadWeightedGradecast(int value, int n, String[] ps, int[] ports, int id, double[] w, double r) {
        super(value, n, ps, ports, id, w, r);
        this.rand = new Random();
    }

    public int startAgreement(int v) {
        this.value = v;
        run();
        return this.value;
    }

    public void run() {
        for(int i = 0; i < n; i++) {
            gradecast();
            int maj = 0;
            double wmaj = 0;

            double c0 = 0, c1 = 0, cc0 = 0, cc1 = 0;
            for(int t = 0; t < grades.size(); t++) {
                int[] grade = grades.get(t);
                if(grade[1] > 0) {
                    if(grade[0] == 0) c0 += w[t];
                    else c1 += w[t];

                    if(grade[1] > 1) {
                        if(grade[0] == 0) cc0 += w[t];
                        else cc1 += w[t];
                    } else {
                        bad.add(t);
                    }
                }

            }

            if(c1 > c0) {
                maj = 1;
                wmaj = cc1;
            } else {
                wmaj = cc0;
            }

            value = maj;

            if(wmaj >= 1 - r) break;
        }
    }

    public void gradecast() {
        //phase 1
        for(int i = 0; i < n; i++) {
            GradecastMsg msg = new GradecastMsg(id,rand.nextInt(2) , id);
            if(i == id) {
                //me
             //  this.handleGrade(msg);
            } else {
                Messager.sendMsg(msg, procs[i], ports[i]);
            }
        }

        waitForTimeout();
        //phase 2

        for(int i = 0; i < n; i++) {
			GradecastMsg gm = new GradecastMsg(msgs[i].leader,rand.nextInt(2), id);
            if(i == id) {
//                this.handleGrade(gm);
            } else {
                Messager.sendMsg(gm, procs[i],ports[i]);
            }
        }
        waitForTimeout();
        //phase 3
        for(int i = 0; i < n; i++) {
            double[] m = getMajority(gms, i);
//            if(m[1] >= 1 - r) {
                GradecastMsg gradecastMsg = new GradecastMsg(i, rand.nextInt(2), id);
                broadcast(gradecastMsg);
//            }
        }

		gms = new HashMap<>(); // clean map
        /*
        waitForTimeout();

        for(int i = 0; i < n; i++) {
            double[] m = getMajority(gms, i);
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
        */

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
		int leader = msg.leader;
		int value = msg.value;
		int sender = msg.sender;
		if(!gms.containsKey(leader)) gms.put(leader, new GradecastMsg[n]);
		GradecastMsg[] gm = gms.get(leader);
		gm[msg.sender] = msg;
    }


    public static void main(String...args) {
        // id value N ips weights r
        int id = Integer.parseInt(args[0]);
        int v = Integer.parseInt(args[1]);
        int[] num = new int[1];
        ConfigReader.readNumHosts(args[2], num);
        String[] servers = new String[num[0]];
        int[] ports = new int[num[0]];
        double[] w = new double[num[0]];
        ConfigReader.readWeights(args[3], w);

        ConfigReader.readServers(args[4], servers);

        for(int i = 0; i < num[0]; i++) {
            ports[i] = 5555 + i;
        }

        double r = Double.parseDouble(args[5]);
        BadWeightedGradecast wg = new BadWeightedGradecast(v, num[0], servers, ports, id, w, r);
        int out = wg.startAgreement(v);
    }

}
