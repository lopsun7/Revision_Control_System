package entity;

import java.io.Serializable;

public class LogEntry implements Serializable {
    private int index;
    private int term;

//    {
//  "operation": "push",
//  "filename": "example.txt",
//  "content": "Hello, World!",
//  "timestamp": "2021-09-01T12:00:00Z"
//}
    private String operation;
    private String filename;
    private String content;
    private String timestamp;

    public LogEntry(int index, int term, String operation, String filename, String content, String timestamp) {
        this.index = index;
        this.term = term;
        this.operation = operation;
        this.filename = filename;
        this.content = content;
        this.timestamp = timestamp;
    }

    // getters and setters


    @Override
    public String toString() {
        return "LogEntry{" +
                "index=" + index +
                ", term=" + term +
                ", operation='" + operation + '\'' +
                ", filename='" + filename + '\'' +
                ", content='" + content + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

}
