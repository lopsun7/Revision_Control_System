package entity;

public class File {
    private String filename;
    private String content;
    private String timestamp;

    public File(String fileName, String content, String timestamp) {
        this.filename = fileName;
        this.content = content;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "File{" +
                "fileName='" + filename + '\'' +
                ", content='" + content + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
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
}
