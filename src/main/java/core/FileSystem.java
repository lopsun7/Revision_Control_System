package core;

import entity.File;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FileSystem {
    private Map<String, File> files;

    public FileSystem() {
        this.files = new HashMap<>();
    }

    public void push(String filename, String content){
        // 获取当前的日期时间
        LocalDateTime now = LocalDateTime.now();

        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 将日期时间转换为字符串
        String formattedDateTime = now.format(formatter);

        files.put(filename,new File(filename,content,formattedDateTime));
    }

    public File pull(String filename){
        return files.getOrDefault(filename, new File("","",""));
    }

    @Override
    public String toString() {
        return "FileSystem{" +
                "files=" + files +
                '}';
    }

    public List<File> getfiles(){
        List<File> list = new LinkedList<>();
        for (Map.Entry<String, File> entry : files.entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }
}
