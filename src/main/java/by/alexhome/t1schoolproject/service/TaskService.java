package by.alexhome.t1schoolproject.service;

import by.alexhome.t1schoolproject.aspect.annotation.LoggingAfter;
import by.alexhome.t1schoolproject.aspect.annotation.LoggingAround;
import by.alexhome.t1schoolproject.aspect.annotation.LoggingBefore;
import by.alexhome.t1schoolproject.aspect.annotation.LoggingThrowing;
import by.alexhome.t1schoolproject.entity.Task;
import by.alexhome.t1schoolproject.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    @LoggingBefore
    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    @LoggingThrowing
    public Task updateTask(Long id, Task task) {
        task.setId(id);
        return taskRepository.save(task);
    }

    @LoggingAfter
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @LoggingAround
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
}