package org.example.producers;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.locator.BaseLocator;
import org.example.model.Device;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Producer1 {
    private static final ModbusFactory modbusFactory = new ModbusFactory();
    private static final int MAX_VALUES = 10;
    private static final int PORT = 1111;
    private static final List<Device> lastTenValues = new ArrayList<>();

    private static final int SERVER_1_PORT = 502;
    private static final int SERVER_1_DEVICE1 = 1;
    private static final int SERVER_1_DEVICE2 = 2;
    private static final int SERVER_1_TEG1 = 1;
    private static final int SERVER_1_TEG2 = 5;
    private static Device device11 = new Device(SERVER_1_DEVICE1, SERVER_1_TEG1, SERVER_1_PORT);
    private static Device device12 = new Device(SERVER_1_DEVICE1, SERVER_1_TEG2, SERVER_1_PORT);
    private static Device device21 = new Device(SERVER_1_DEVICE2, SERVER_1_TEG1, SERVER_1_PORT);
    private static Device device22 = new Device(SERVER_1_DEVICE2, SERVER_1_TEG2, SERVER_1_PORT);

    public static void main(String[] args) {
        try {
            Thread butchTread = new Thread(() -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(PORT);
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                        outputStream.writeObject(lastTenValues);
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            butchTread.start();

            while (true) {
                BaseLocator<Number> loc1 = BaseLocator.holdingRegister(SERVER_1_DEVICE1, SERVER_1_TEG1, DataType.TWO_BYTE_INT_UNSIGNED);
                Number number1 = getMaster("127.0.0.1", SERVER_1_PORT).getValue(loc1);
                device11.addValue(number1.intValue());

                addValueToList();

                Thread.sleep(200);
            }

        } catch (ModbusInitException | ModbusTransportException | ErrorResponseException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addValueToList() {
        if (lastTenValues.isEmpty()) {
            lastTenValues.add(device11);
            lastTenValues.add(device12);
            lastTenValues.add(device21);
            lastTenValues.add(device22);
        }
    }

    private static void printLastTenValues() {
        System.out.println("Last ten values:");

        for (Device d : lastTenValues) {
            for (Map.Entry<LocalDateTime, Integer> entry : d.getValues().entrySet()){
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }
            System.out.println(d);
        }
    }

    public static ModbusMaster getMaster(String host, int port) throws ModbusInitException {
        IpParameters parameters = new IpParameters();
        parameters.setHost(host);
        parameters.setPort(port);
        ModbusMaster master = modbusFactory.createTcpMaster(parameters, false);
        master.init();
        return master;
    }
}
