package network;

public enum RaftRequestType {
    REQUEST_VOTE,    // 请求投票
    ANSWER_VOTE,      // 投票结果
    APPEND_ENTRIES,  // 追加日志条目和心跳
    SEND_HEARTBEAT,  // 显式心跳，如果你决定区分心跳和普通的日志追加
    ANSWER_HEARTBEAT // 回应心跳
}
