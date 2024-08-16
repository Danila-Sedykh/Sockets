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
import java.util.ArrayList;
import java.util.List;

public class Producer2 {
    private static final ModbusFactory modbusFactory = new ModbusFactory();
    private static final int MAX_VALUES = 10;
    private static final int PORT = 1112;
    private static final List<Device> lastTenValues = new ArrayList<>();

    private static final int SERVER_PORT = 503;
    private static final int SERVER_DEVICE1 = 1;
    private static final int SERVER_1_DEVICE2 = 2;
    private static final int SERVER_1_TEG1 = 1;
    private static final int SERVER_1_TEG2 = 2;
    private static Device device11 = new Device(SERVER_DEVICE1, SERVER_1_TEG1, SERVER_PORT);
    private static Device device12 = new Device(SERVER_DEVICE1, SERVER_1_TEG2, SERVER_PORT);
    private static Device device21 = new Device(SERVER_1_DEVICE2, SERVER_1_TEG1, SERVER_PORT);
    private static Device device22 = new Device(SERVER_DEVICE1, SERVER_1_TEG2, SERVER_PORT);

    public static void main(String[] args) {
        try {
            Thread butchTread = new Thread(() -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(PORT);
                    System.out.println("Butch server started on port " + PORT);
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                        System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                        System.out.println("Data received from MODBUS server: " + lastTenValues);
                        System.out.println("Port received from MODBUS server: " + SERVER_PORT);
                        outputStream.writeObject(lastTenValues);
                        //outputStream.writeInt(SERVER_PORT);
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            butchTread.start();

            while (true) {

                BaseLocator<Number> loc1 = BaseLocator.holdingRegister(SERVER_DEVICE1, SERVER_1_TEG1, DataType.TWO_BYTE_INT_UNSIGNED);
                Number number1 = getMaster("127.0.0.1", SERVER_PORT).getValue(loc1);
                device11.addValue(number1.intValue());


                BaseLocator<Number> loc2 = BaseLocator.holdingRegister(SERVER_DEVICE1, SERVER_1_TEG2, DataType.TWO_BYTE_INT_UNSIGNED);
                Number number2 = getMaster("127.0.0.1", SERVER_PORT).getValue(loc2);
                device12.addValue(number2.intValue());

                BaseLocator<Number> loc3 = BaseLocator.holdingRegister(SERVER_1_DEVICE2, SERVER_1_TEG1, DataType.TWO_BYTE_INT_UNSIGNED);
                Number number3 = getMaster("127.0.0.1", SERVER_PORT).getValue(loc3);
                device21.addValue(number3.intValue());

                BaseLocator<Number> loc4 = BaseLocator.holdingRegister(SERVER_1_DEVICE2, SERVER_1_TEG2, DataType.TWO_BYTE_INT_UNSIGNED);
                Number number4 = getMaster("127.0.0.1", SERVER_PORT).getValue(loc4);
                device22.addValue(number4.intValue());

                /*addValueToList(number1.doubleValue());*/

                addValueToList();
                //printLastTenValues();

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
            System.out.println(d);
            for (int i = 0; i < d.getValues().size(); i++){
                System.out.println(d.getValues().get(i));
            }
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
