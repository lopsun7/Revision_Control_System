package core;

import entity.File;
import entity.LogEntry;
import entity.RaftState;
import network.RaftClient;
import network.RaftRequest;
import network.RaftRequestType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RaftNode {
    private RaftState state;
    private ServerState role; // LEADER, FOLLOWER, CANDIDATE
    private Timer electionTimer;
    private Timer heartbeatTimer;
    private Random random;
    private int nodeId; // 节点的唯一标识
    private AtomicInteger voteCount; // 用于计票的原子整数
    private Map<Integer, AtomicInteger> appendEntriesAckCounter = new ConcurrentHashMap<>();
    private Map<Integer, Timer> timers = new ConcurrentHashMap<>();
    private List<String> peers; // 其他节点的地址列表
    private List<Integer> port; // 其他节点的地址列表
    private RaftClient raftClient; // 网络通信客户端
    private FileSystem fileSystem; //状态机
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
        this.fileSystem = new FileSystem();
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
        }, 5000 + random.nextInt(2500)); // 1500 to 3000 milliseconds
    }

    public synchronized void startElection() {
        state.setCurrentTerm(state.getCurrentTerm() + 1);
        System.out.println("node "+nodeId +" term "+state.getCurrentTerm()+" starts to elect a leader");
        state.setVotedFor(nodeId);
        role = ServerState.CANDIDATE;
        voteCount.set(1);  // 为自己投票

        // 发送请求投票到所有其他节点
        for (int i = 0;i < peers.size(); i++) {
            if(i+1 == nodeId) continue;
            RaftRequest voteRequest = new RaftRequest(
                    RaftRequestType.REQUEST_VOTE,
                    state.getCurrentTerm(),
                    nodeId,
                    state.getLastLogIndex(),
                    state.getLastLogTerm(),
                    state.getLog(),
                    state.getCommitIndex(),
                    -1,
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
                -1,
                voteGranted
        );
        int id = request.getCandidateId();
        System.out.println("node "+nodeId+" term "+state.getCurrentTerm()+" decides to send a " + voteGranted + " to " + id);
        raftClient.sendRequest(peers.get(id-1),port.get(id-1),voteResponse);
    }

    //接收决议票
    public synchronized void receiveVoteResponse(RaftRequest request) {
        boolean voteGranted = request.getVote();
        System.out.println("candidate "+nodeId+" term "+state.getCurrentTerm()+" receives "+voteGranted+" vote from follower node "+request.getCandidateId());
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
                System.out.println("leader： "+nodeId+fileSystem.toString());
                heartbeatTimer = new Timer();
                heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        sendHeartbeats(null,0);
                    }
                }, 0, 5000);  // 每500毫秒发送一次心跳
                // 还可以在此处初始化其他与领导者相关的任务
            }
        }
    }

    //发送心跳
    public synchronized void sendHeartbeats(List<LogEntry> append, int lockId) {
        if (role == ServerState.LEADER) {
            for (int i = 0;i < peers.size(); i++) {
                if(i+1 == nodeId) continue;
                System.out.println(i+1 + "sent");
                RaftRequest heartbeat = new RaftRequest(
                        RaftRequestType.SEND_HEARTBEAT,
                        state.getCurrentTerm(),
                        nodeId,
                        state.getLastLogIndex(),
                        state.getLastLogTerm(),
                        append,
                        state.getCommitIndex(),
                        lockId,
                        false
                );
                raftClient.sendRequest(peers.get(i),port.get(i), heartbeat);
            }
        }
    }

    //接收心跳
    public synchronized void receiveHeartbeats(RaftRequest request){
        System.out.println(nodeId+" receives a heartbeat from " + request.getCandidateId());
        //leader的term小于follwer，拒绝
        if (request.getTerm() < state.getCurrentTerm()) return;

        //leader的term大于follwer，更新follwer的term和role,重置选举计时
        state.setCurrentTerm(request.getTerm());
        role = ServerState.FOLLOWER;
        resetElectionTimer();
//        //日志为空，纯心跳，返回
//        if(request.getEntries() == null) return;

        //判断日志复制是否成功
        boolean isSuccess;
        int leaderLastLogIndex = request.getLastLogIndex();
        int leaderLastLogTerm = request.getLastLogTerm();

        //判断末条日志是否相同
        if(leaderLastLogIndex == state.getLastLogIndex() && leaderLastLogTerm == state.getLastLogTerm()){
            isSuccess = true;
            if (request.getEntries() != null) state.addAll(request.getEntries());
            //提交进度比leader慢
            if (state.getCommitIndex() < request.getLeaderCommit()){
                //commit
                int nextToCommitLogIndex = state.getCommitIndex();
                int leaderNextToCommitLogIndex = request.getLeaderCommit();
                List<LogEntry> list = state.getLog();
                while(nextToCommitLogIndex < leaderNextToCommitLogIndex){
                    LogEntry log = list.get(nextToCommitLogIndex-1);
                    fileSystem.push(log.getFilename(),log.getContent());
                    nextToCommitLogIndex++;
                }
                System.out.println(nodeId + " commit "+fileSystem.toString());
                state.setCommitIndex(nextToCommitLogIndex);
            }
        }
        else{
            isSuccess = false;
        }
        //日志为空，纯心跳，返回
        if (request.getEntries() == null) return;
        //日志不为空，返回结果
        int leaderId = request.getCandidateId();
        RaftRequest heartbeatResponse = new RaftRequest(
                RaftRequestType.ANSWER_HEARTBEAT,
                state.getCurrentTerm(),
                nodeId,
                state.getLastLogIndex(),
                state.getLastLogTerm(),
                null,
                state.getCommitIndex(),
                request.getLockId(),
                isSuccess
        );
        raftClient.sendRequest(peers.get(leaderId-1),port.get(leaderId-1),heartbeatResponse);

    }

    //接收心跳结果
    public synchronized void receiveHeartbeatsResult(RaftRequest request){
        boolean isSuccess = request.getVote();
        System.out.println("leader "+nodeId+" term "+state.getCurrentTerm()+" receives "+isSuccess+" log check from follower node "+request.getCandidateId());
        if (isSuccess) {
            int successCount = appendEntriesAckCounter.get(request.getLockId()).incrementAndGet();
            System.out.println("successCount:"+successCount);
            if (successCount >= (peers.size() / 2) + 1) {
                appendEntriesAckCounter.get(request.getLockId()).set(0);
                //commit
                int nextToCommitLogIndex = state.getCommitIndex();
                int lastLogIndex = state.getLastLogIndex();
                List<LogEntry> list = state.getLog();
                while(nextToCommitLogIndex <= lastLogIndex){
                    LogEntry log = list.get(nextToCommitLogIndex-1);
                    fileSystem.push(log.getFilename(),log.getContent());
                    nextToCommitLogIndex++;
                }
                System.out.println(nodeId + " commit "+fileSystem.toString());
                state.setCommitIndex(nextToCommitLogIndex);
            }
        }
        else {
            int followerId = request.getCandidateId();
            RaftRequest heartbeat = new RaftRequest(
                    RaftRequestType.SYNC,
                    state.getCurrentTerm(),
                    nodeId,
                    state.getLastLogIndex(),
                    state.getLastLogTerm(),
                    state.getLog(),
                    state.getCommitIndex(),
                    -1,
                    false
            );
            raftClient.sendRequest(peers.get(followerId-1),port.get(followerId-1), heartbeat);
        }
    }

    public void sync(RaftRequest request) {
        state.setLog(request.getEntries());
        System.out.println("node "+nodeId+" sync :"+state.getLog());
    }

    public boolean isLeader() {
        return role == ServerState.LEADER;
    }

    public void handlePush(String filename, String content) {
        //获取时间戳
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        //封装日志
        int leaderLastLogIndex = state.getLastLogIndex();
        LogEntry newLog = new LogEntry(leaderLastLogIndex+1,state.getCurrentTerm(),"push",filename,content,formattedDateTime);
        List<LogEntry> append = new ArrayList<>();
        append.add(newLog);

        //为每个appendEntries创建新计数器
        AtomicInteger counter = new AtomicInteger(1);

        appendEntriesAckCounter.put(leaderLastLogIndex+1, counter);
        //向其他followers发送appendEntries
        sendHeartbeats(append,leaderLastLogIndex+1);
        //本地日志更新
        state.addAll(append);

    }

    public String handlePull(String filename) {
        File file = fileSystem.pull(filename);
        String timestamp = file.getTimestamp();
        return Objects.equals(timestamp, "") ? "" : file.getContent();
    }


}
