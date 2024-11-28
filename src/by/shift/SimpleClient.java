package by.shift;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SimpleClient {
    public static void main(String[] args) {
        String host = "localhost"; // Адрес сервера
        int port = 12345;         // Порт сервера
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Подключено к серверу. Введите сообщение (введите 'exit' для выхода):");

            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                out.println(userInput); // Отправка сообщения серверу
                String response = in.readLine(); // Чтение ответа от сервера
                System.out.println("Ответ сервера: " + response);

                if ("exit".equalsIgnoreCase(userInput)) {
                    System.out.println("Завершаем подключение.");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка клиента: " + e.getMessage());
        }
    }
}
