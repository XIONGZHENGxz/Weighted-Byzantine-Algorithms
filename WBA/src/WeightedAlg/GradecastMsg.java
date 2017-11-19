package WeightedAlg;

public class GradecastMsg {
    public int sender; //sender id
    public int leader;
    public int value;

    public GradecastMsg(int leader, int value, int sender) {
        this.leader = leader;
        this.value = value;
        this.sender = sender;
    }

}
