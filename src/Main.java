import core.RaftNode;
import entity.RaftState;
import network.RaftServer;
import util.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import core.RaftNode;
import entity.RaftState;
import network.RaftServer;
import util.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        //Config config = new Config("src/util/config.properties");
        List<String> peers = Arrays.asList("0.0.0.0", "0.0.0.0", "0.0.0.0", "0.0.0.0","0.0.0.0");//config.getPeers();
        List<Integer> ports = Arrays.asList(8000,8001,8002,8003,8004);// config.getPorts();

        // 用于存储所有服务器线程的列表
        List<Thread> serverThreads = new ArrayList<>();

        for (int i = 0; i < peers.size(); i++) {
            // 创建节点的副本列表
            List<String> peerSubset = new ArrayList<>(peers);
            List<Integer> portSubset = new ArrayList<>(ports);

            // 移除当前节点的信息，以便其他节点不尝试与自己连接
            peerSubset.remove(i);
            portSubset.remove(i);

            // 创建每个节点
            RaftNode raftNode = new RaftNode(i + 1, peerSubset, portSubset);

            // 创建并启动服务器线程
            RaftServer server = new RaftServer(ports.get(i), raftNode);
            Thread serverThread = new Thread(server, "ServerThread-" + (i + 1));
            serverThread.start();  // 启动线程

            // 将线程添加到列表中
            serverThreads.add(serverThread);
        }

        // 等待所有线程完成
        for (Thread thread : serverThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Server thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt(); // 重新设置中断状态
            }
        }
    }
}
