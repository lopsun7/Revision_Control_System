package core;

import entity.LogEntry;
import entity.RaftState;
import network.RaftClient;
import network.RaftRequest;
import network.RaftRequestType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RaftNode {
    private RaftState state;
    private ServerState role; // LEADER, FOLLOWER, CANDIDATE
    private Timer electionTimer;
    private Timer heartbeatTimer;
    private Random random;
    private int nodeId; // 节点的唯一标识
    private AtomicInteger voteCount; // 用于计票的原子整数
    private List<String> peers; // 其他节点的地址列表
    private RaftClient raftClient; // 网络通信客户端
    public RaftNode(int nodeId) {
        this.state = new RaftState();
        this.role = ServerState.FOLLOWER;
        this.electionTimer = new Timer();
        this.random = new Random();
        this.nodeId = nodeId;
        this.voteCount = new AtomicInteger(0);
        resetElectionTimer();
    }

    private void resetElectionTimer() {
        if (electionTimer != null) {
            electionTimer.cancel();
        }
        electionTimer = new Timer();
        electionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                startElection();
            }
        }, 1500 + random.nextInt(1500)); // 1500 to 3000 milliseconds
    }

    public synchronized void startElection() {
        state.setCurrentTerm(state.getCurrentTerm() + 1);
        state.setVotedFor(nodeId);
        role = ServerState.CANDIDATE;
        voteCount.set(1);  // 为自己投票

        // 发送请求投票到所有其他节点
        for (String peer : peers) {
            RaftRequest voteRequest = new RaftRequest(
                    RaftRequestType.REQUEST_VOTE,
                    state.getCurrentTerm(),
                    nodeId,
                    state.getLastLogIndex(),
                    state.getLastLogTerm(),
                    null,
                    -1
            );
            raftClient.sendRequest(peer, voteRequest);
        }

        // 重新设置选举定时器
        resetElectionTimer();
    }

    public void receiveVoteResponse(boolean voteGranted) {
        if (voteGranted) {
            int votes = voteCount.incrementAndGet();
            if (votes > peers.size() / 2) {
                role = ServerState.LEADER;
                System.out.println("Became leader for term " + state.getCurrentTerm());

                // 开始领导者的行为，如定期发送心跳
                if (electionTimer != null) {
                    electionTimer.cancel();
                    electionTimer = null; // 取消后将计时器置空
                }
                heartbeatTimer = new Timer();
                heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        sendHeartbeats();
                    }
                }, 0, 500);  // 每500毫秒发送一次心跳
                // 还可以在此处初始化其他与领导者相关的任务
            }
        }
    }

    public void sendHeartbeats() {
        if (role == ServerState.LEADER) {
            for (String peer : peers) {
                RaftRequest heartbeat = new RaftRequest(
                        RaftRequestType.HEARTBEAT,
                        state.getCurrentTerm(),
                        nodeId,
                        state.getLastLogIndex(),
                        state.getLastLogTerm(),
                        new ArrayList<>(), // 空日志条目列表代表心跳
                        state.getCommitIndex()
                );
                raftClient.sendRequest(peer, heartbeat);
            }
        }
    }
    public void appendEntries(List<LogEntry> entries) {
        // 实现日志条目追加逻辑
        if (entries.isEmpty()) {
            // 处理心跳
            System.out.println("Received heartbeat from leader");
            resetElectionTimer();
        } else {
            // 处理追加日志条目
            // 这里需要实现日志追加的逻辑
            System.out.println("Received log entries to append");
        }
    }
}
