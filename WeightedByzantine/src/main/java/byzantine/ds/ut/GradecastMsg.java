package byzantine.ds.ut;

import java.io.Serializable;

public class GradecastMsg implements Serializable {
    static final long serialVersionUID=2L;
    public int sender; //sender id
    public int leader;
    public int value;

    public GradecastMsg(int leader, int value, int sender) {
        this.leader = leader;
        this.value = value;
        this.sender = sender;
    }

}
