package by.shift;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadedServer {
    public static void main(String[] args) {
        int port = 12345; // Порт для подключения
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен, ожидается подключение...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Подключен клиент: " + clientSocket.getInetAddress());

                // Обработка клиента в отдельном потоке
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Ошибка сервера: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String receivedMessage;
                while ((receivedMessage = in.readLine()) != null) {
                    System.out.println("Клиент (" + clientSocket.getInetAddress() + "): " + receivedMessage);
                    // Ответ клиенту
                    out.println("Сервер получил: " + receivedMessage);

                    if ("exit".equalsIgnoreCase(receivedMessage)) {
                        System.out.println("Клиент завершил соединение: " + clientSocket.getInetAddress());
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Ошибка при работе с клиентом: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Ошибка при закрытии соединения: " + e.getMessage());
                }
            }
        }
    }
}
