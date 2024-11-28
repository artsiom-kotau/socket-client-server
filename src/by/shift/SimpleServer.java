package by.shift;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class SimpleServer {
    public static void main(String[] args) {
        int port = 12345; // Порт для подключения
        List<String> book = new LinkedList<>();
        Map<String, RequestProcessor> requestMapping = new ConcurrentHashMap<>();
        requestMapping.put("/book", new RequestProcessor() {
            @Override
            public Response doGet() {
                return new Response() {
                    @Override
                    public int code() {
                        return 200;
                    }

                    @Override
                    public String body() {
                        return String.join(",", book);
                    }
                };
            }

            @Override
            public Response doPost(String body) {
                return new Response() {
                    @Override
                    public int code() {
                        return 202;
                    }

                    @Override
                    public String body() {
                        return body;
                    }
                };
            }
        });
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен, ожидается подключение...");
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            while (true) {
                // Ожидание подключения клиента
                Socket clientSocket = serverSocket.accept();
                System.out.println("Подключен клиент: " + clientSocket.getInetAddress());
                executorService.submit(new Thread(() -> {
                    try {
                        process(clientSocket, requestMapping);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));

            }
        } catch (IOException e) {
            System.out.println("Ошибка сервера: " + e.getMessage());
        }
    }

    private static void process(Socket clientSocket, Map<String, RequestProcessor> requestMapping) throws IOException {

        // Работа с клиентом
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String receivedMessage;
            while ((receivedMessage = in.readLine()) != null) {
                System.out.println("Получено: " + receivedMessage);
                // Ответ клиенту
                out.println("Сервер получил: " + receivedMessage);
                if ("exit".equalsIgnoreCase(receivedMessage)) {
                    System.out.println("Клиент завершил соединение.");
                    break;
                }

                String[] messageParts = receivedMessage.split(" ", 3);
                RequestProcessor requestProcessor = requestMapping.get(messageParts[1]);
                if (requestProcessor == null) {
                    out.println("404 Not found");
                } else {
                    Response response = switch (messageParts[0]) {
                        case "GET" -> requestProcessor.doGet();
                        case "POST" -> requestProcessor.doPost(messageParts[2]);
                        default -> throw new UnsupportedOperationException(messageParts[0]);
                    };

                    out.println(response.code() + " " + response.body());
                }



            }
        } finally {
            clientSocket.close();
        }
    }

    private interface RequestProcessor {
        Response doGet();
        Response doPost(String body);
    }

    private interface Response {
        int code();

        String body();
    }
}
