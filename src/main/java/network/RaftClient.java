package main.java.network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RaftClient {
    private ExecutorService executorService;

    public RaftClient() {
        // 创建一个固定大小的线程池
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public void sendRequest(String host, int port, RaftRequest request) {
        executorService.submit(() -> {
            try (Socket socket = new Socket(host, port);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
                objectOutputStream.writeObject(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}

