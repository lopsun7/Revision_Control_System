package spring;

import core.RaftNode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import util.Config;

import java.io.IOException;
import java.util.List;

@Configuration
public class RaftConfig {

    @Bean
    public RaftNode raftNode() throws IOException {
        Config config = new Config("src/main/resources/config.properties");
        List<String> peers = config.getPeers();
        List<Integer> ports = config.getPorts();
        int nodeId = config.getNodeId();
        return new RaftNode(nodeId,peers,ports);
        // 还可以传递其他构造函数参数
    }
}