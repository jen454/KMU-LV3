package chat;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8888);

        //thread로 된 유저들의 리스트(온라인 유저 목록)
        List<ChatThread> onlineList = Collections.synchronizedList(new ArrayList<>());
        //session 번호: 기본값으로 defaultSession++ 값들이 들어가 자동적으로 같은 세션에 들어가지 않게 한다
        int defaultSession = 0;

        while(true) {
            Socket socket = serverSocket.accept();
            ChatThread chatThread = new ChatThread(socket, onlineList, defaultSession++);
            chatThread.start();
        }
    }
}