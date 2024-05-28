package network;

public enum RaftRequestType {
    REQUEST_VOTE,    // 请求投票
    APPEND_ENTRIES,  // 追加日志条目和心跳
    HEARTBEAT        // 显式心跳，如果你决定区分心跳和普通的日志追加
}
