package chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    public static void main(String[] args) throws Exception {
        if(args.length != 1){
            System.out.println("command: java .\\ChatClient 닉네임");
            return;
        }
        //사용자 이름
        String name = args[0];
        //server에 socket 연결
        Socket socket = new Socket("127.0.0.1", 8888);
        //사용자 입력 받기 및 전송
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

        // 닉네임 전송
        pw.println(name);
        pw.flush();

        // 백그라운드로 서버가 보내준 메세지를 읽여들여서 화면에 출력
        InputThread inputThread = new InputThread(br);
        inputThread.start();

        // 서버에 메시지, 명령어 전송
        try{
            String line = null;
            while((line = keyboard.readLine()) != null) {
                if("/quit".equals(line)) {
                    pw.println("/quit");
                    pw.flush();
                    break;
                }
                pw.println(line);
                pw.flush();
            }
        }catch(Exception ex){
            System.out.println("...");
        }

        br.close();
        pw.close();
        
        //연결 종료
        System.out.println("socket close!!");
        socket.close();

    }
}

//서버로부터 메시지 받기
class InputThread extends Thread {
    BufferedReader br;
    public InputThread(BufferedReader br){
        this.br = br;
    }

    @Override
    public void run() {
        try{
            String line = null;
            while((line = br.readLine()) != null){
                System.out.println(line);
            }
        }catch(Exception ex){
            System.out.println("...");
        }
    }
}