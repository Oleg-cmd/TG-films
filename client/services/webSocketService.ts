import { Client, Frame } from "@stomp/stompjs";

class WebSocketService {
  private static instance: WebSocketService | null = null;
  private stompClient: Client | null = null;
  private socket: WebSocket | null = null;
  private isConnected: boolean = false;
  private onConnectCallback: (() => void) | null = null;

  private constructor() {}

  public static getInstance(): WebSocketService {
    if (!WebSocketService.instance) {
      WebSocketService.instance = new WebSocketService();
    }
    return WebSocketService.instance;
  }

  public setOnConnectCallback(callback: () => void) {
    this.onConnectCallback = callback;
  }

  public connect(): void {
    if (this.isConnected) {
      console.log("Already connected to WebSocket.");
      return; // Подключение уже установлено, не нужно подключаться заново
    }

    // use wss for deploy
    // const WS_URL = "wss://localhost:8080/ws"; // Прямой WebSocket
    const WS_URL =
      "wss://jewellery-worship-minor-unauthorized.trycloudflare.com/ws"; // Прямой WebSocket

    this.socket = new WebSocket(WS_URL);
    this.socket.onopen = () => {
      console.log("WebSocket connection opened");
    };
    this.socket.onclose = () => {
      console.log("WebSocket connection closed");
    };

    // Создаем экземпляр клиента STOMP
    this.stompClient = new Client({
      webSocketFactory: () => this.socket!,
      onConnect: () => {
        this.isConnected = true;
        console.log("STOMP connection established");
        if (this.onConnectCallback) {
          this.onConnectCallback();
        }
      },
      onStompError: (frame: Frame) => {
        console.error("STOMP Error: " + frame.headers["message"]);
        console.error("STOMP Error details: " + frame.body);
      },
    });

    // Активируем STOMP клиент
    this.stompClient.activate();
  }

  public disconnect(): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.deactivate();
      this.isConnected = false;
      console.log("STOMP connection closed");
    }
  }

  public subscribe(
    destination: string,
    callback: (message: any) => void
  ): void {
    if (this.stompClient && this.stompClient.connected) {
      console.log(`Subscribing to ${destination}`);
      this.stompClient.subscribe(destination, callback);
    } else {
      console.error("WebSocket is not connected.");
    }
  }

  public send(destination: string, body: string): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.publish({ destination, body });
      console.log("Sent message:", body);
    } else {
      console.error("WebSocket is not connected.");
    }
  }
}

export default WebSocketService;
