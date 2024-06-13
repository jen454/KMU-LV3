from socket import *
from urllib.parse import urlparse

PROMPT = lambda: print("\n> ", end='', flush=True)

def main():
    print("Student ID : 20223098", flush=True)
    print("Name : Jinwook Shin", flush=True)
    PROMPT()

    while True:
        buf = input()
        if not buf:
            exit(0)

        cmd = buf.strip().split()[0] # down or quit
        if not cmd:
            PROMPT()
            continue
        elif cmd == "quit":
            exit(0)
        if cmd != "down":
            print(f"Wrong command {cmd}", flush=True)
            PROMPT()
            continue

        # connect to a server
        parsed_url = urlparse(buf.strip().split()[1])
        path = parsed_url.path
        filename = path.split('/')[-1]

        # URL http 아닌 경우 예외 처리
        if parsed_url.scheme != "http":
            print(f"Only support http, not {parsed_url.scheme}", flush=True)
            PROMPT()
            continue

        # 서버 이름에 포트 번호 유무 예외 처리
        netloc_parts = parsed_url.netloc.split(":")
        serverName = netloc_parts[0] if len(netloc_parts) == 1 else ":".join(netloc_parts[:-1])
        serverPort = int(netloc_parts[-1]) if len(netloc_parts) > 1 else 80

        # tcp 연결 (예외 처리)
        try:
          clientSocket = socket(AF_INET, SOCK_STREAM)
          clientSocket.connect((serverName, serverPort))
        except gaierror as err:
            print(f"{serverName}: unknown host", flush=True)
            print(f"cannot connect to server {serverName} {serverPort}", flush=True)
            PROMPT()
            continue

        # 요청 보내기
        client_http_message = f"GET {path} HTTP/1.0\r\n"
        client_http_message += f"HOST: {serverName}\r\n"
        client_http_message += "User-agent: HW1/1.0\r\n"
        client_http_message += "Connection: close\r\n\r\n"
        clientSocket.send(client_http_message.encode())

        # 응답 받기
        response = b""
        content_length = 0
        received = False
        pre_downloaded_percentage = 0
        while True:
            data = clientSocket.recv(1024)
            if not data:
                break
            response += data
            headers, body = response.split(b"\r\n\r\n", 1)

            if not received:
                received = True
                print(client_http_message, flush=True)

                # status 확인
                status_line = response.split(b"\r\n", 1)[0]
                status_code = status_line.split()[1].decode()
                reason_phrase = status_line.split(maxsplit=2)[2].decode()
                if status_code != "200":
                    print(f"{status_code} {reason_phrase}\n", flush=True)
                    continue

                # HTTP 응답 헤더로부터 Content-Length 구하기
                for header in headers.split(b"\r\n"):
                    if header.startswith(b"Content-Length:"):
                        content_length = int(header.split(b":")[1].strip())
                        print(f"Total Size: {content_length} bytes", flush=True)
                        break

            # 파일 다운로드
            with open(filename, "wb") as f:
                f.write(body)
                downloaded_bytes = len(body)
                percentage = int(downloaded_bytes / content_length * 100)
                if percentage % 10 == 0 and percentage > pre_downloaded_percentage:
                    pre_downloaded_percentage = percentage
                    print(f"Current Downloading: {downloaded_bytes}/{content_length} (bytes) {percentage}%", flush=True)
                elif percentage == 100:
                    print(f"Current Downloading: {downloaded_bytes}/{content_length} (bytes) 100%", flush=True)
                    
        clientSocket.close()
        PROMPT()

if __name__ == "__main__":
    main()
