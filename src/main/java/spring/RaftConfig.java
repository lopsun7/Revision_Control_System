package spring;

import core.RaftNode;
import network.RaftServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import util.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

@Configuration
public class RaftConfig  {


    @Bean
    public RaftNode raftNode() throws IOException {
        //Config config = new Config("src/main/resources/config.properties");
        List<String> peers = Arrays.asList("node1", "node2", "node3");//config.getPeers();
        List<Integer> ports = Arrays.asList(8000,8001,8002);//config.getPorts();
        int nodeId = Integer.valueOf(System.getenv("ID"));//config.getNodeId();
//        // 创建节点的副本列表
//        List<String> peerSubset = new ArrayList<>(peers);
//        List<Integer> portSubset = new ArrayList<>(ports);

//        // 移除当前节点的信息，以便其他节点不尝试与自己连接
//        peerSubset.remove(nodeId-1);
//        portSubset.remove(nodeId-1);
        return new RaftNode(nodeId,peers,ports);
        // 还可以传递其他构造函数参数
    }
    @Bean
    public RaftServer raftServer(RaftNode raftNode) throws IOException {
        //Config config = new Config("src/main/resources/config.properties");
        int nodeId = Integer.valueOf(System.getenv("ID"));
        List<Integer> ports = Arrays.asList(8000,8001,8002,8003,8004);
        int port = ports.get(nodeId - 1);  // 假设节点ID和端口列表是一一对应的
        return new RaftServer(port, raftNode);
    }

}