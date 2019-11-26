import spos.lab1.demo.DoubleOps;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class SocketHandler implements Runnable {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Task task;

    public SocketHandler(Socket s, Task task) throws IOException {
        socket = s;
        inputStream = s.getInputStream();
        outputStream = s.getOutputStream();
        this.task = task;
    }


    @Override
    public void run() {
        try {
            double x = readInput();
            new Thread(() -> {
                System.out.println("Calculating task for " + x + ", press escape to cancel...");
                try {
                    System.out.println(task.run(x));
                } catch (RuntimeException e) {
                    if (e.getCause() instanceof AbortableLatch.AbortedException) {
                        System.err.println("Calculation aborted");
                    } else throw e;
                }
            }).start();
            writeResponse("" + x);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void writeResponse(String s) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Server: Lab1\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + s.length() + "\r\n" +
                "Connection: close\r\n\r\n";
        String result = response + s;
        outputStream.write(result.getBytes());
        outputStream.flush();
    }

    private double readInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        double result = 0;
        while (true) {
            String s = reader.readLine();
            if (s == null || s.trim().length() == 0) {
                break;
            } else if (s.startsWith("GET ")) {
                String query = s.split(" ")[1];
                if (query.equals("/favicon.ico")) throw new RuntimeException("favicon request");
                URL url = new URL("http://localhost:80" + query);
                Map<String, String> map = splitUrl(url);
                if (map.containsKey("x")) {
                    result = Double.parseDouble(map.get("x"));
                } else {
                    throw new RuntimeException("GET params should contain x");
                }
            }
        }
        return result;
    }

    private static Map<String, String> splitUrl(URL url) {
        if (url.getQuery() == null) throw new NullPointerException("Query should exist");
        Map<String, String> result = new LinkedHashMap<>();
        for (String pair: url.getQuery().split("&")) {
            int idx = pair.indexOf('=');
            try {
                if (idx != -1) {
                    result.put(
                            URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                            URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                    );
                } else {
                    result.put(
                            URLDecoder.decode(pair, "UTF-8"),
                            null
                    );
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
