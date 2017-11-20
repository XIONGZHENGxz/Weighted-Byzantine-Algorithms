package byzantine.ds.ut;

import java.io.IOException;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenerKing extends Thread{
    private int port;
    private ServerSocket serverSocket;
    private BYZKing byzKing;

    public ListenerKing(int port, BYZKing byz) {
        this.port = port;
        this.byzKing = byz;
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
                byzKing.handleRequest(request);
            } catch (IOException e) {
                System.out.println("paxos at "+port+" down");
                break;
            }
        }
    }
}