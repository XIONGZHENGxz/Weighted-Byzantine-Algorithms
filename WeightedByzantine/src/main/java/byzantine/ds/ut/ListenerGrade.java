package byzantine.ds.ut;

import java.io.IOException;
import java.lang.Thread;
import java.net.ServerSocket;

import java.net.Socket;

public class ListenerGrade extends Thread{
    private int port;
    private ServerSocket serverSocket;
    private WeightedGradecast gradecast;

    public ListenerGrade(int port, WeightedGradecast gradecast) {
        this.port = port;
        this.gradecast = gradecast;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true) {
            try {
                Socket socket = serverSocket.accept();
                if(Util.DEBUG) {
                    System.out.println("socket connected..."+ port);
                }
                Object request = Messager.getMsg(socket);
                gradecast.handleGrade((GradecastMsg) request);
            } catch (IOException e) {
                System.out.println("paxos at "+port+" down");
                break;
            }
        }
    }
}