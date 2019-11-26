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
        // I use Thread.stop() on threads to stop them everywhere in this program even though it is
        // deprecated and unsafe because it's the only proper way to completely stop the thread
        // (thread.interrupt() only asks the thread to stop, it won't stop a while(true) {} thread for example)

        // Setting up the task
        Task task = Task.ofCaseIndex(0, true);
        task.setTimeout(10, TimeUnit.SECONDS);
        // The server
        ServerSocket serverSocket = new ServerSocket(80);
        // Accepting connections in an infinite loop
        while (true) {
            Socket socket = serverSocket.accept();
            try {
                new SocketHandler(socket, task).run();
            } catch (RuntimeException e) {
                // If the error is thrown because the request is a favicon.ico request, ignore it, otherwise print an error message
                if (!e.getMessage().equals("favicon request"))
                    System.err.println("Error occurred: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}
