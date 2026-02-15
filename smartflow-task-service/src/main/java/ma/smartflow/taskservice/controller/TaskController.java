package ma.smartflow.taskservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.smartflow.taskservice.dtos.TaskRequest;
import ma.smartflow.taskservice.dtos.TaskResponse;
import ma.smartflow.taskservice.services.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @RequestBody @Valid TaskRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Username") String username
            ){
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request, userId, username));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getMyTasks(
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(taskService.getMyTasks(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(taskService.getTask(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(taskService.updateTask(id, request, userId));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> completeTask(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(taskService.completeTask(id, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        taskService.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }
}
