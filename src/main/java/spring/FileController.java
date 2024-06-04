package spring;

import core.RaftNode;
import entity.File;
import network.RaftServer;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private RaftNode raftNode; // RaftNode应该能够检查当前节点的角色，并进行日志操作
    @Autowired
    private RaftServer raftServer;
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/{filename}")
    public ResponseEntity<String> pushFile(@PathVariable("filename") String filename, @RequestBody String content) {
        System.out.println("push" + filename);
        if (!raftNode.isLeader()) {
            return ResponseEntity.badRequest().body("Not a leader. Redirect or retry.");
        }
        raftNode.handlePush(filename, content);
        return ResponseEntity.ok("File pushed successfully.");
    }
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/{filename}")
    public ResponseEntity<String> pullFile(@PathVariable("filename") String filename) {
        System.out.println("pull" + filename);
        if (!raftNode.isLeader()) {
            return ResponseEntity.badRequest().body("Not a leader. Redirect or retry.");
        }
        String content = raftNode.handlePull(filename);
        if(Objects.equals(content, "")) return ResponseEntity.badRequest().body("File does not exist");
        return ResponseEntity.ok(content);
    }
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/all")
    public Map<String, File> getAll() {
        if (!raftNode.isLeader()) {
            Map<String, File> map2 = new HashMap<>();
            map2.put("Error", new File("","",""));
            return map2;
        }
        List<File> files = raftNode.handleAll();
        Map<String, File> map1 = new HashMap<>();
        for (File file: files){
            map1.put(file.getFilename(),file);
        }
        return map1;
    }
}
