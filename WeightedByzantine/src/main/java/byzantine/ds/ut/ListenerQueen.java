package byzantine.ds.ut;

import java.io.IOException;
import java.lang.Thread;
import java.net.ServerSocket;

import java.net.Socket;

public class ListenerQueen extends Thread{
    private int port;
    private ServerSocket serverSocket;
    private BYZQueen byz;

    public ListenerQueen(int port, BYZQueen byz) {
        this.port = port;
        this.byz = byz;
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
                byz.handleRequest(request);
            } catch (IOException e) {
                System.out.println("server at "+port+" down");
                break;
            }
        }
    }
}