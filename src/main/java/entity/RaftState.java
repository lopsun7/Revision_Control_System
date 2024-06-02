package main.java.entity;

import java.util.ArrayList;
import java.util.List;

public class RaftState {
    private int currentTerm;
    private int votedFor;
    private List<LogEntry> log;
    private int commitIndex;  // 已提交的最高日志条目的索引

    public RaftState() {
        this.log = new ArrayList<>();
        this.votedFor = -1;
        this.commitIndex = 0;  // 初始化为0
    }

    public void addAll(List<LogEntry> append){
        if(append == null) return;
        log.addAll(append);
    }
    public int getTermFromIndex(int index){
        return log.get(index).getTerm();
    }


    public int getLastLogIndex() {
        if (log.isEmpty()) {
            return 0;  // 返回0或其他适当的默认值
        }
        return log.get(log.size()-1).getIndex();
    }

    public int getLastLogTerm() {
        if (log.isEmpty()) {
            return 0;  // 返回0或其他适当的默认值
        }
        return  log.get(log.size()-1).getIndex();
    }

    // getters and setters

    public int getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(int commitIndex) {
        this.commitIndex = commitIndex;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(int currentTerm) {
        this.currentTerm = currentTerm;
    }

    public int getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(int votedFor) {
        this.votedFor = votedFor;
    }

    public List<LogEntry> getLog() {
        return log;
    }

    public void setLog(List<LogEntry> log) {
        this.log = log;
    }
}
