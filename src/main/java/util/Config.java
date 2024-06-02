package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Config {
    private List<String> peers;
    private List<Integer> ports;
    private int nodeId;  // 添加 nodeId 成员变量
    public Config(String configFilePath) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(configFilePath)) {
            properties.load(in);
        }
        parsePeers(properties.getProperty("peers"));
        parsePorts(properties.getProperty("port"));
        parseNodeId(properties.getProperty("nodeId"));  // 读取 nodeId
    }

    private void parseNodeId(String nodeIdString) {
        if (nodeIdString != null && !nodeIdString.isEmpty()) {
            nodeId = Integer.parseInt(nodeIdString.trim());  // 将字符串转换为整数
        } else {
            nodeId = -1;  // 默认值或错误处理
        }
    }

    private void parsePeers(String peersString) {
        if (peersString != null && !peersString.isEmpty()) {
            peers = Arrays.asList(peersString.split(","));
        } else {
            peers = new ArrayList<>();
        }
    }

    private void parsePorts(String portsString) {
        if (portsString != null && !portsString.isEmpty()) {
            String[] portStrings = portsString.split(",");
            ports = new ArrayList<>();
            for (String portString : portStrings) {
                ports.add(Integer.parseInt(portString.trim()));
            }
        } else {
            ports = new ArrayList<>();
        }
    }

    public List<String> getPeers() {
        return peers;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public int getNodeId() {
        return nodeId;  // 提供 nodeId 的 getter 方法
    }
}
