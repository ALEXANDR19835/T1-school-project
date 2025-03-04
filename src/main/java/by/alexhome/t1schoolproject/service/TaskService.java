package by.alexhome.t1schoolproject.service;

import by.alexhome.t1schoolproject.aspect.annotation.LoggingAfter;
import by.alexhome.t1schoolproject.aspect.annotation.LoggingAround;
import by.alexhome.t1schoolproject.aspect.annotation.LoggingBefore;
import by.alexhome.t1schoolproject.aspect.annotation.LoggingThrowing;
import by.alexhome.t1schoolproject.exception.TaskNotFoundException;
import by.alexhome.t1schoolproject.kafka.producer.KafkaProducer;
import by.alexhome.t1schoolproject.mapper.TaskMapper;
import by.alexhome.t1schoolproject.model.dto.TaskDto;
import by.alexhome.t1schoolproject.model.entity.Task;
import by.alexhome.t1schoolproject.model.enums.TaskStatus;
import by.alexhome.t1schoolproject.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final KafkaProducer kafkaProducer;

    public TaskDto createTask(TaskDto taskDto) {
        Task task = taskMapper.taskDTOToTask(taskDto);
        Task savedTask = taskRepository.save(task);

        return taskMapper.taskToTaskDTO(savedTask);
    }

    @LoggingBefore
    @LoggingThrowing
    public TaskDto getTaskById(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        return taskMapper.taskToTaskDTO(task);
    }

    @LoggingThrowing
    public TaskDto updateTask(Long id, TaskDto taskDto) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        TaskStatus oldStatus = task.getStatus();
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setUserId(taskDto.getUserId());
        task.setStatus(taskDto.getStatus());

        Task updatedTask = taskRepository.save(task);
        if (oldStatus != updatedTask.getStatus() && updatedTask.getStatus() != TaskStatus.CREATE) {
            kafkaProducer.sendMessageStatusUpdate(taskMapper.taskToTaskDTO(updatedTask));
        }
        return taskMapper.taskToTaskDTO(updatedTask);
    }

    @LoggingAfter
    @LoggingThrowing
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        taskRepository.deleteById(task.getId());
    }

    @LoggingAround
    public List<TaskDto> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(taskMapper::taskToTaskDTO)
                .collect(Collectors.toList());
    }
}