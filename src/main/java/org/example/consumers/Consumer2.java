package org.example.consumers;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.Device;
import org.example.model.IsolationForest;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Consumer2 extends Application {
    private static final String BUTCH_HOST = "localhost";
    private static final int BUTCH_PORT = 2222;
    private static List<Device> receivedValues = new ArrayList<>();
    private static List<LineChart<String, Number>> charts = new ArrayList<>();
    private static VBox vbox = new VBox();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static Timer timer;

    private static final int MAX_DATA_POINTS = 10;
    private static IsolationForest isolationForest;
    private static final double ANOMALY_THRESHOLD = 0.29;


    public static void main(String[] args) {
        new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = new Socket(BUTCH_HOST, BUTCH_PORT);
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

                    Object object = inputStream.readObject();
                    if (object instanceof List) {
                        receivedValues = (List<Device>) object;
                        timer = new Timer( 1000, e -> Platform.runLater(Consumer2::updateCharts));
                        timer.start();

                    }
                    isolationForest = new IsolationForest(10, 5);
                    for (Device device : receivedValues) {
                        isolationForest.train(device.getValues());
                    }
                    Thread.sleep(1000);
                }
            } catch (ClassNotFoundException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("GRAFIK");
        ScrollPane scrollPane = new ScrollPane(vbox);
        Scene scene = new Scene(scrollPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private static void updateCharts() {
        System.out.println("Updating charts...");
        if (charts.isEmpty()) {
            createCharts();
        } else {
            for (int i = 0; i < receivedValues.size(); i++) {
                Device device = receivedValues.get(i);
                LineChart<String, Number> chart = charts.get(i);
                updateChart(chart, device);
            }
        }
        if (!receivedValues.isEmpty()) {
            LinkedHashMap<String,String> anomalous = new LinkedHashMap<>();

            for (Device device : receivedValues) {
                for (Map.Entry<LocalDateTime, Integer> entry : device.getValues().entrySet()) {
                    double anomalyScore = isolationForest.anomalyScore(new double[]{entry.getValue()});
                    System.out.println("Div " + device.getName() + "anomal: " + anomalyScore +"getValue - " + entry.getValue());
                    if (anomalyScore >= ANOMALY_THRESHOLD) {
                        anomalous.put(entry.getKey().format(formatter),"Device "+ device.getName() + " Teg" + device.getTeg());
                    }
                }
            }

            for (Map.Entry<String,String> data : anomalous.entrySet()) {
                LineChart<String, Number> chart = findChartByTeg(data.getValue());
                if (chart != null) {
                    XYChart.Series<String, Number> series = chart.getData().get(0);
                    for (XYChart.Data<String, Number> point : series.getData()) {
                        if (point.getXValue().equals(data.getKey())) {
                            point.getNode().setStyle("-fx-background-color: blue; -fx-radius: 5;");
                        }
                    }
                }
            }
        }

    }
    private static LineChart<String, Number> findChartByTeg(String data) {
        for (LineChart<String, Number> chart : charts) {
            if (chart.getTitle().contains(data)) {
                return chart;
            }
        }
        return null;
    }

    private static void createCharts() {
        for (Device device : receivedValues) {

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Time");
            yAxis.setLabel("Value");

            LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Device " + device.getName() + " Teg" + device.getTeg());
            lineChart.setStyle("-fx-font-size: 16px;");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Values");

            for (Map.Entry<LocalDateTime, Integer> entry : device.getValues().entrySet()) {
                String formattedTime = entry.getKey().format(formatter);
                XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(formattedTime, entry.getValue());
                series.getData().add(dataPoint);
            }
            lineChart.getData().add(series);
            charts.add(lineChart);
            vbox.getChildren().add(lineChart);
        }
    }


    private static void updateChart(LineChart<String, Number> chart, Device device) {
        XYChart.Series<String, Number> series = chart.getData().get(0);

        List<XYChart.Data<String, Number>> dataToRemove = new ArrayList<>();

        List<XYChart.Data<String, Number>> existingData = new ArrayList<>(series.getData());

        for (Map.Entry<LocalDateTime, Integer> entry : device.getValues().entrySet()) {
            String formattedTime = entry.getKey().format(formatter);
            int value = entry.getValue();

            boolean exists = existingData.stream().anyMatch(data -> data.getXValue().equals(formattedTime));
            if (!exists) {
                XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(formattedTime, value);
                series.getData().add(dataPoint);

                if (series.getData().size() > MAX_DATA_POINTS) {
                    dataToRemove.add(series.getData().get(0));
                }
            }
        }
        series.getData().removeAll(dataToRemove);
    }
}
