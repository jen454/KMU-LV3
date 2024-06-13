from socket import *
import os
import sys

def send_res_header(status_code, content_type, content_length):
    response_headers = f"HTTP/1.0 {status_code}\r\n"
    response_headers += "Connection: close\r\n"
    response_headers += f"Content-Length: {content_length}\r\n"
    response_headers += f"Content-Type: {content_type}\r\n\r\n"
    
    response = response_headers.encode()
    return response

def main():
    if len(sys.argv) != 2:
        print(f"usage: {sys.argv[0]} portnum", flush=True)
        sys.exit(1)

    portnum = int(sys.argv[1])

    print("Student ID : 20223098", flush=True)
    print("Name : Jinwook Shin", flush=True)

    # 서버 소켓 생성
    serverSocket = socket(AF_INET, SOCK_STREAM)
    serverSocket.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
    serverSocket.bind(('', portnum))
    serverSocket.listen()
    
    try:
        while True:
            connectionSocket, addr = serverSocket.accept()
            print(f"Connection : Host IP {addr[0]}, Port {addr[1]}, socket {connectionSocket.fileno()}", flush=True) # initial

            data = connectionSocket.recv(1024)
            if not data:
                continue

            request_first_line = data.decode().split('\r\n')[0]
            print(request_first_line, flush=True) # command
            user_agent = data.decode().split('User-Agent: ')[1].split(' ')[0]
            print(f"User-Agent: {user_agent}", flush=True) # User-Agent

            print("{} headers".format(data.decode().count('\r\n')-2), flush=True) # headers 출력

            file_path = request_first_line.split()[1][1:]  # 요청 파일

            if os.path.exists(file_path):
                if file_path.endswith('.jpg'):
                    content_type = 'image/jpeg'
                elif file_path.endswith('.html'):
                    content_type = 'text/html'

                connectionSocket.send(send_res_header('200 OK', content_type, os.path.getsize(file_path)))

                with open(file_path, 'rb') as file:             
                    try:
                        file_data = file.read(1024)
                        send_data = 0
                        while file_data:
                            send_data += connectionSocket.send(file_data)
                            file_data = file.read(1024)
                    except Exception as e:
                        print(e, flush=True)
                print(f"finish {send_data} {os.path.getsize(file_path)}", flush=True)
            else:
                connectionSocket.send(send_res_header('404 Not Found', 'text/html', 0))
                print(f"Server Error : No such file ./{file_path}!", flush=True) # 서버 에러 출력
                
            connectionSocket.close()
            
    except KeyboardInterrupt:
        serverSocket.close()  # Ctrl+C로 인터럽트가 발생하면 서버 소켓을 닫음

if __name__ == "__main__":
    main()
