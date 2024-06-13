package chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatThread extends Thread {

    private String name;
    private BufferedReader br;
    private PrintWriter pw;
    private Socket socket;
    List<ChatThread> onlineList;
    private int sessionNum;

    public ChatThread(Socket socket, List<ChatThread> onlineList, int defaultSession)throws Exception {

        this.socket = socket;
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter pw =  new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.br = br;
        this.pw = pw;
        this.name = br.readLine();
        this.onlineList = onlineList;
        this.onlineList.add(this);
        this.sessionNum = defaultSession;

    }

    //메시지 전송
    public void sendMessage(String msg){
        pw.println(msg);
        pw.flush();
    }

    //userlist 업데이트
    public void updateUserList() {
        try {
            File file = new File("./userList.txt");

            if(!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file);
            BufferedWriter fileWriter = new BufferedWriter(fw);

            fileWriter.write(this.onlineList.toString());
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void run() {
        updateUserList();

        //ChatThread는 사용자가 보낸 메세지를 읽어들임
        try{
            // broadcast(name + "님이 연결되었습니다.", false);
            getOnlineList(true);
            String line = null;
            while((line = br.readLine()) != null){
                //세션 이탈
                if("/quit".equals(line)){
                    break;
                }

                //헤더 분리
                String header = line.split(" ")[0];

                //온라인 유저 목록 확인
                if(header.equals("/onlineList")) {
                    getOnlineList(true);
                    continue;
                }
                //본인의 세션에 참가한 유저 확인
                if(header.equals("/sessionList")) {
                    getSessionMembers(this.sessionNum);
                    continue;
                }
                //name을 가진 유저 초대
                if(header.equals("/invite")) {
                    invite(line.split(" ")[1]);
                    continue;
                }
                //같은 세션의 유저들에게 메시지 전송
                broadcast(name + " : " + line,true);
            }
        }catch(Exception ex){
            //ChatThread가 연결이 끊어짐
            ex.printStackTrace();
        }
        finally{
            broadcast(name + "님이 연결이 끊어졌습니다.", false);
            this.onlineList.remove(this);
            updateUserList();
            try{
                br.close();
            }catch(Exception ex){
            }

            try{
                pw.close();
            }catch(Exception ex){
            }

            try{
                socket.close();
            }catch(Exception ex){
            }
        }
        
    }

    private void broadcast(String msg, boolean includeMe){
        List<ChatThread> chatThreads = new ArrayList<>();
        for(int i=0;i<this.onlineList.size();i++){
            chatThreads.add(onlineList.get(i));
        }

        try{
            for(int i=0;i<chatThreads.size();i++){
                ChatThread ct = chatThreads.get(i);
                if(!includeMe){ //메시지 작성자를 포함하지 않음
                    if(ct == this){
                        continue;
                    }
                }
                if(ct.sessionNum != this.sessionNum) {
                    continue;
                }
                ct.sendMessage(msg);
            }
        }catch(Exception ex) {
            System.out.println("///");
        }
    }

    private void getOnlineList(boolean includeMe) {
        List<ChatThread> chatThreads = new ArrayList<>();
        for(int i=0;i<this.onlineList.size();i++){
            chatThreads.add(onlineList.get(i));
        }

        try{
            for(int i=0;i<chatThreads.size();i++){
                if(chatThreads.get(i) == this){
                    chatThreads.get(i).sendMessage(chatThreads.toString());
                    break;
                }
            }
        }catch(Exception ex) {
            System.out.println("///");
        }
    }

    private void getSessionMembers(int sessionNum) {
        List<ChatThread> chatThreads = new ArrayList<>();
        for(int i=0;i<this.onlineList.size();i++){
            if(this.sessionNum == this.onlineList.get(i).sessionNum){
                chatThreads.add(this.onlineList.get(i));
            }
        }

        for(int i=0;i<chatThreads.size();i++){
            if(chatThreads.get(i) == this){
                chatThreads.get(i).sendMessage(chatThreads.toString());
                break;
            }
        }
    }

    private void invite(String name) {
        for(int i=0;i<this.onlineList.size();i++){
            if(name.equals(this.onlineList.get(i).name)){
                this.onlineList.get(i).sessionNum = this.sessionNum;
                broadcast(this.name + "님의 session에 " + name + "님이 연결되었습니다.", true);
            }
        }
    }

    @Override
    public String toString() {
        return this.name + " " + this.socket.getLocalAddress() + " " + this.socket.getPort();
    }
}