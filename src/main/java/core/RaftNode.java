package main.java.core;

import main.java.entity.LogEntry;
import main.java.entity.RaftState;
import main.java.network.RaftClient;
import main.java.network.RaftRequest;
import main.java.network.RaftRequestType;

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
    private List<Integer> port; // 其他节点的地址列表
    private RaftClient raftClient; // 网络通信客户端
    public RaftNode(int nodeId,List<String> peers,List<Integer> port) {
        this.state = new RaftState();
        this.role = ServerState.FOLLOWER;
        this.electionTimer = new Timer();
        this.random = new Random();
        this.nodeId = nodeId;
        this.voteCount = new AtomicInteger(0);
        this.raftClient = new RaftClient();
        this.peers = peers;
        this.port = port;
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
        }, 2500 + random.nextInt(2500)); // 1500 to 3000 milliseconds
    }

    public synchronized void startElection() {
        state.setCurrentTerm(state.getCurrentTerm() + 1);
        System.out.println("node "+nodeId +" term "+state.getCurrentTerm()+" starts to elect a leader");
        state.setVotedFor(nodeId);
        role = ServerState.CANDIDATE;
        voteCount.set(1);  // 为自己投票

        // 发送请求投票到所有其他节点
        for (int i = 0;i < peers.size(); i++) {
            RaftRequest voteRequest = new RaftRequest(
                    RaftRequestType.REQUEST_VOTE,
                    state.getCurrentTerm(),
                    nodeId,
                    state.getLastLogIndex(),
                    state.getLastLogTerm(),
                    state.getLog(),
                    state.getCommitIndex(),
                    false
            );
            raftClient.sendRequest(peers.get(i),port.get(i), voteRequest);
        }

        // 重新设置选举定时器
        resetElectionTimer();
    }

    //接收投票请求，发送决议票到候选人
    public synchronized void receiveVoteRequest(RaftRequest request) {
        System.out.println("node "+nodeId+" term "+state.getCurrentTerm()+" gets vote request from candidate node " + request.getCandidateId());
        boolean voteGranted = false;
        int currentTerm = state.getCurrentTerm();
        int lastLogIndex = state.getLastLogIndex();
        int lastLogTerm = state.getLastLogTerm();

        if (request.getTerm() < currentTerm) {
            voteGranted = false; // 如果请求的任期小于当前任期，拒绝投票
        } else {
            if (request.getTerm() > currentTerm){
                state.setCurrentTerm(request.getTerm());
                state.setVotedFor(-1);
            }
            if (state.getVotedFor() == -1){
                state.setVotedFor(request.getCandidateId());
                voteGranted = true;
                resetElectionTimer();
            }
//            if (request.getTerm() > currentTerm || state.getVotedFor() == -1) {
//                state.setCurrentTerm(request.getTerm()); // 更新当前任期
//                state.setVotedFor(-1);  // 重置已投票的候选人ID
//
//                if (request.getLastLogTerm() > lastLogTerm ||
//                        (request.getLastLogTerm() == lastLogTerm && request.getLastLogIndex() >= lastLogIndex)) {
//                    state.setVotedFor(request.getCandidateId()); // 投票给候选人
//                    voteGranted = true; // 发送赞成票
//                }
//            }
        }

        // 使用 RaftClient 发送投票结果
        RaftRequest voteResponse = new RaftRequest(
                RaftRequestType.ANSWER_VOTE,
                state.getCurrentTerm(),
                nodeId,
                state.getLastLogIndex(),
                state.getLastLogTerm(),
                state.getLog(),
                state.getCommitIndex(),
                voteGranted
        );
        int id = request.getCandidateId();
        System.out.println("node "+nodeId+" term "+state.getCurrentTerm()+" decides to send a " + voteGranted + " to " + id);
        raftClient.sendRequest(peers.get(id-1),port.get(id-1),voteResponse);
    }

    //接收决议票
    public synchronized void receiveVoteResponse(RaftRequest request) {
        boolean voteGranted = request.getVote();
        System.out.println("candidate "+nodeId+" term "+state.getCurrentTerm()+" receives "+voteGranted+" vote from candidate node "+request.getCandidateId());
        if (voteGranted) {
            int votes = voteCount.incrementAndGet();
//            System.out.println("node "+nodeId+" vote number: " + votes);
            if (votes >= (peers.size() / 2) + 1) {
                role = ServerState.LEADER;
                System.out.println("node "+nodeId+" term "+state.getCurrentTerm()+" became leader for term " + state.getCurrentTerm());
                voteCount.set(0);
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

    //发送心跳
    public void sendHeartbeats() {
        if (role == ServerState.LEADER) {
            for (int i = 0;i < peers.size(); i++) {
                RaftRequest heartbeat = new RaftRequest(
                        RaftRequestType.SEND_HEARTBEAT,
                        state.getCurrentTerm(),
                        nodeId,
                        state.getLastLogIndex(),
                        state.getLastLogTerm(),
                        new ArrayList<>(), // 空日志条目列表代表心跳
                        state.getCommitIndex(),
                        false
                );
                raftClient.sendRequest(peers.get(i),port.get(i), heartbeat);
            }
        }
    }

    //接收心跳
    public synchronized void receiveHeartbeats(RaftRequest request){
        System.out.println(nodeId+" receives a heartbeat from " + request.getCandidateId());
        if (request.getTerm() > state.getCurrentTerm()){
            state.setCurrentTerm(request.getTerm());
            role = ServerState.FOLLOWER;
        }
        resetElectionTimer();
        //判断日志是否一致
        boolean isSame = true;
        int id = request.getCandidateId();
        RaftRequest heartbeatResponse = new RaftRequest(
                RaftRequestType.ANSWER_HEARTBEAT,
                state.getCurrentTerm(),
                nodeId,
                state.getLastLogIndex(),
                state.getLastLogTerm(),
                state.getLog(),
                state.getCommitIndex(),
                isSame
        );
        raftClient.sendRequest(peers.get(id-1),port.get(id-1),heartbeatResponse);

    }

    //接收心跳结果
    public void receiveHeartbeatsResult(RaftRequest request){
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
