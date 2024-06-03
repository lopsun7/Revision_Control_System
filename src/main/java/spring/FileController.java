package spring;

import core.RaftNode;
import network.RaftServer;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private RaftNode raftNode; // RaftNode应该能够检查当前节点的角色，并进行日志操作
    @Autowired
    private RaftServer raftServer;

    @PostMapping("/{filename}")
    public ResponseEntity<String> pushFile(@PathVariable("filename") String filename, @RequestBody String content) {
        System.out.println("push" + filename);
        if (!raftNode.isLeader()) {
            return ResponseEntity.badRequest().body("Not a leader. Redirect or retry.");
        }
        raftNode.handlePush(filename, content);
        return ResponseEntity.ok("File pushed successfully.");
    }

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
}
