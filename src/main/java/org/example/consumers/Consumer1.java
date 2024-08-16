package org.example.consumers;

import org.example.model.Device;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Consumer1 {
    private static final String BUTCH_HOST = "localhost";
    private static final int BUTCH_PORT = 2222;
    private static List<Device> receivedValues = new ArrayList<>();

    public static void main(String[] args) {
        new Thread(() -> {
        try {
            Socket socket = new Socket(BUTCH_HOST, BUTCH_PORT);
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Object object = inputStream.readObject();
                if (object instanceof List) {
                    receivedValues = (List<Device>) object;
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
        }).start();

        Connection connection;
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/postgres";
            String user = "postgres";
            String password = "1";
            connection = DriverManager.getConnection(url, user, password);

            String sql = "INSERT INTO server_1 (device_address, server_address, value_device, value_time) VALUES (?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(sql);

            for (Device d : receivedValues) {
                for (Map.Entry<LocalDateTime, Integer> entry : d.getValues().entrySet()){
                    statement.setInt(1, d.getName());
                    statement.setInt(2, d.getTeg());
                    statement.setDouble(3, entry.getValue());
                    statement.setString(4, String.valueOf(entry.getKey()));
                }
                statement.executeUpdate();
                System.out.println(d);
            }
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static void printTest(List<Device> receivedValues) {
        for (Device d : receivedValues) {
            for (Map.Entry<LocalDateTime, Integer> entry : d.getValues().entrySet()) {
                System.out.println(entry.getKey() +
                        " - " + entry.getValue() + " - " + d.getPort() + " - " + d.getName() + " - " + d.getTeg());
            }
        }
    }

}
