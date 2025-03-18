package by.alexhome.t1schoolproject.service;

import by.alexhome.t1schoolproject.exception.TaskNotFoundException;
import by.alexhome.t1schoolproject.model.dto.TaskDto;
import by.alexhome.t1schoolproject.model.entity.Task;
import by.alexhome.t1schoolproject.model.enums.TaskStatus;
import by.alexhome.t1schoolproject.repository.TaskRepository;
import by.alexhome.t1schoolproject.testConteiner.PostgresContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TaskServiceIntegrationTest extends PostgresContainer {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    private TaskDto taskDto;
    private Task savedTask;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        taskDto = TaskDto.builder()
                .title("Test Title")
                .description("Test Description")
                .userId(1L)
                .status(TaskStatus.COMPLETED)
                .build();

        Task task = Task.builder()
                .title("Test Title")
                .description("Test Description")
                .userId(2L)
                .status(TaskStatus.IN_PROGRESS)
                .build();
        savedTask = taskRepository.save(task);
    }

    @Test
    @DisplayName("Создание задачи")
    public void createTask() {

        TaskDto savedTaskDto = taskService.createTask(taskDto);

        assertThat(savedTaskDto).isNotNull();
        assertThat(savedTaskDto.getTitle()).isEqualTo(taskDto.getTitle());
        assertThat(savedTaskDto.getDescription()).isEqualTo(taskDto.getDescription());
        assertThat(savedTaskDto.getStatus()).isEqualTo(taskDto.getStatus());

        Task savedTask = taskRepository.findById(savedTaskDto.getId()).orElse(null);
        assertThat(savedTask).isNotNull();
        assertThat(savedTask.getTitle()).isEqualTo(taskDto.getTitle());
        assertThat(savedTask.getDescription()).isEqualTo(taskDto.getDescription());
        assertThat(savedTask.getStatus()).isEqualTo(taskDto.getStatus());
    }

    @Test
    @DisplayName("Получение задачи по ID: задача существует")
    public void getTaskById() {

        TaskDto foundTask = taskService.getTaskById(savedTask.getId());

        assertThat(foundTask).isNotNull();
        assertThat(foundTask.getId()).isEqualTo(savedTask.getId());
        assertThat(foundTask.getTitle()).isEqualTo(savedTask.getTitle());
        assertThat(foundTask.getDescription()).isEqualTo(savedTask.getDescription());
        assertThat(foundTask.getStatus()).isEqualTo(savedTask.getStatus());
    }

    @Test
    @DisplayName("Получение задачи по ID: задача не существует")
    public void getTaskByIdWhenTaskNotExistsShouldThrowTaskNotFoundException() {
        Long nonExistingId = 111L;

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> taskService.deleteTask(nonExistingId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Task with ID = " + nonExistingId + " not found", exception.getReason());
    }

    @Test
    @DisplayName("Обновление задачи")
    public void updateTask() {

        TaskDto updatedTaskDto = TaskDto.builder()
                .title("Updated Title")
                .description("Updated Description")
                .userId(2L)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        TaskDto resultTaskDto = taskService.updateTask(savedTask.getId(), updatedTaskDto);

        assertThat(resultTaskDto).isNotNull();
        assertThat(resultTaskDto.getId()).isEqualTo(savedTask.getId());
        assertThat(resultTaskDto.getTitle()).isEqualTo(updatedTaskDto.getTitle());
        assertThat(resultTaskDto.getDescription()).isEqualTo(updatedTaskDto.getDescription());
        assertThat(resultTaskDto.getUserId()).isEqualTo(updatedTaskDto.getUserId());
        assertThat(resultTaskDto.getStatus()).isEqualTo(updatedTaskDto.getStatus());

        Task updatedTask = taskRepository.findById(savedTask.getId()).orElse(null);
        assertThat(updatedTask).isNotNull();
        assertThat(updatedTask.getTitle()).isEqualTo(updatedTaskDto.getTitle());
        assertThat(updatedTask.getDescription()).isEqualTo(updatedTaskDto.getDescription());
        assertThat(updatedTask.getUserId()).isEqualTo(updatedTaskDto.getUserId());
        assertThat(updatedTask.getStatus()).isEqualTo(updatedTaskDto.getStatus());
    }

    @Test
    @DisplayName("Обновление задачи: задача не найдена, выбрасывается исключение")
    void updateTaskWhenTaskNotFoundShouldThrowTaskNotFoundException() {
        TaskDto updatedTaskDto = TaskDto.builder()
                .title("Updated Title")
                .description("Updated Description")
                .userId(2L)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        Long nonExistingId = 111L;

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(nonExistingId, updatedTaskDto));
    }

    @Test
    @DisplayName("Удаление задачи: если задача существует, она успешно удаляется")
    void deleteTask() {

        taskService.deleteTask(savedTask.getId());

        boolean exists = taskRepository.existsById(savedTask.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Удаление задачи: если задача не найдена, выбрасывается TaskNotFoundException")
    void deleteTaskWhenTaskNotFoundShouldThrowTaskNotFoundException() {
        Long nonExistingId = 111L;

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> taskService.deleteTask(nonExistingId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Task with ID = " + nonExistingId + " not found", exception.getReason());
    }

    @Test
    @DisplayName("Получение всех задач: список не пустой")
    void getAllTasks() {
        List<TaskDto> taskDtos = taskService.getAllTasks();

        assertThat(taskDtos).isNotNull();
        assertThat(taskDtos).hasSize(1);

        assertThat(taskDtos.get(0).getTitle()).isEqualTo("Test Title");
    }

    @Test
    @DisplayName("Получение всех задач: возвращается пустой список, если задач нет")
    void getAllTasksWhenNoTasksExistShouldReturnEmptyList() {

        taskRepository.deleteAll();

        List<TaskDto> taskDtos = taskService.getAllTasks();

        assertThat(taskDtos).isEmpty();
    }
}