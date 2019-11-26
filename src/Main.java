import spos.lab1.demo.DoubleOps;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Main {
    public static void main(String[] args) throws Exception {
        Task task = Task.ofCaseIndex(0, true);
        task.setTimeout(10, TimeUnit.SECONDS);
        ServerSocket serverSocket = new ServerSocket(80);
        while (true) {
            Socket socket = serverSocket.accept();
            try {
                new SocketHandler(socket, task).run();
            } catch (RuntimeException e) {
                if (!e.getMessage().equals("favicon request"))
                    System.err.println("Error occurred: " + e.getClass() + ": " + e.getMessage());
            }
        }
    }
}
