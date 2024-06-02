package network;

import entity.LogEntry;

import java.io.Serializable;
import java.util.List;

public class RaftRequest implements Serializable {
    private RaftRequestType requestType; // REQUEST_VOTE / APPEND_ENTRIES / HEARTBEAT
    private int term; // 任期号
    private int candidateId; // 候选人ID
    private int lastLogIndex; // 最后日志索引
    private int lastLogTerm; // 最后日志任期
    private List<LogEntry> entries; // 日志条目
    private int leaderCommit; // 领导者已提交的日志索引

    private int lockId;

    private boolean vote; //投票结果

    public RaftRequest(RaftRequestType requestType, int term, int candidateId, int lastLogIndex, int lastLogTerm, List<LogEntry> entries, int leaderCommit, int lockId, boolean vote) {
        this.requestType = requestType;
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
        this.entries = entries;
        this.leaderCommit = leaderCommit;
        this.lockId = lockId;
        this.vote = vote;
    }

// Getters and Setters


    public int getLockId() {
        return lockId;
    }

    public void setLockId(int lockId) {
        this.lockId = lockId;
    }

    public boolean getVote() {
        return vote;
    }

    public void setVote(boolean vote) {
        this.vote = vote;
    }

    public RaftRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RaftRequestType requestType) {
        this.requestType = requestType;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(int lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public int getLastLogTerm() {
        return lastLogTerm;
    }

    public void setLastLogTerm(int lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }

    public List<LogEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<LogEntry> entries) {
        this.entries = entries;
    }

    public int getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(int leaderCommit) {
        this.leaderCommit = leaderCommit;
    }
}

