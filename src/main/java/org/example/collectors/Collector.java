package org.example.collectors;

import org.example.model.Device;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Collector {
    private static final String MODBUS_TEST_HOST = "localhost";
    private static final int MODBUS_TEST_SERVER1 = 1111;
    private static final int MODBUS_TEST_SERVER2 = 1112;
    private static final int PORT = 2222;
    private static volatile List<Device> allData = new ArrayList<>();
    private static List<Device> data = new ArrayList<>();


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

            Thread thread1 = new Thread(() -> processServerData(MODBUS_TEST_SERVER1, clientSocket));

            thread1.start();

            try {
                thread1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                outputStream.writeObject(data);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void processServerData(int serverPort, Socket clientSocket) {
        try {
            Socket socket = new Socket(MODBUS_TEST_HOST, serverPort);
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            data = (List<Device>) inputStream.readObject();

            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}
