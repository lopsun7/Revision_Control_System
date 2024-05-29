package network;

import core.RaftNode;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RaftServer implements Runnable {
    private ServerSocket serverSocket;
    private RaftNode raftNode;
    private ExecutorService executorService;

    public RaftServer(int port, RaftNode node) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.raftNode = node;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream())) {
            Object object = objectInputStream.readObject();
            if (object instanceof RaftRequest) {
                RaftRequest request = (RaftRequest) object;
                switch (request.getRequestType()) {
                    case REQUEST_VOTE:
                        raftNode.receiveVoteRequest(request);
                        break;
                    case ANSWER_VOTE:
                        raftNode.receiveVoteResponse(request);
                        break;
                    case APPEND_ENTRIES:
                        raftNode.appendEntries(request.getEntries());
                        break;
                    case SEND_HEARTBEAT:
                        raftNode.receiveHeartbeats(request);
                        break;
                    case ANSWER_HEARTBEAT:
                        raftNode.receiveHeartbeatsResult(request);
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
