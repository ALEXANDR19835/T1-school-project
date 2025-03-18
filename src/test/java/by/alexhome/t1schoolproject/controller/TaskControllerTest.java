package by.alexhome.t1schoolproject.controller;

import by.alexhome.t1schoolproject.model.dto.TaskDto;
import by.alexhome.t1schoolproject.model.entity.Task;
import by.alexhome.t1schoolproject.model.enums.TaskStatus;
import by.alexhome.t1schoolproject.repository.TaskRepository;
import by.alexhome.t1schoolproject.testConteiner.PostgresContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class TaskControllerTest extends PostgresContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<Task> savedTasks;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        savedTasks = saveTestTasks();
    }

    @Test
    void getAllTasks() throws Exception {
        Long firstTaskId = savedTasks.get(0).getId();
        Long secondTaskId = savedTasks.get(1).getId();

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(firstTaskId))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].id").value(secondTaskId))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }

    @Test
    @DisplayName("Test getTaskById")
    public void getTaskById() throws Exception {
        Long taskId = savedTasks.get(0).getId();

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Task 1"));
    }

    @Test
    @DisplayName("Test getTaskById throws exception when task not found")
    public void getTaskByIdShouldThrowExceptionWhenTaskNotFound() throws Exception {
        mockMvc.perform(get("/tasks/{id}", 111))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("404 NOT_FOUND \"Task with ID = 111 not found\"",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    @DisplayName("Test createTask")
    public void createTask() throws Exception {
        TaskDto taskDto = TaskDto.builder()
                .title("New Task")
                .description("New Description")
                .userId(3L)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists()) // Проверяем, что id был сгенерирован
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.userId").value(3L))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Test updateTask")
    public void updateTask() throws Exception {
        Long taskId = savedTasks.get(0).getId();

        TaskDto taskDto = TaskDto.builder()
                .title("Updated Task")
                .build();

        mockMvc.perform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Updated Task"));
    }

    @Test
    @DisplayName("Test deleteTask")
    public void deleteTask() throws Exception {
        Long taskId = savedTasks.get(0).getId();

        mockMvc.perform(delete("/tasks/{id}", taskId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test deleteTask throws exception when task not found")
    public void deleteTaskShouldThrowExceptionWhenTaskNotFound() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", 111))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("404 NOT_FOUND \"Task with ID = 111 not found\"",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    private List<Task> saveTestTasks() {
        Task task1 = Task.builder()
                .title("Task 1")
                .description("Test Description 1")
                .userId(1L)
                .status(TaskStatus.COMPLETED)
                .build();
        Task task2 = Task.builder()
                .title("Task 2")
                .description("Test Description 2")
                .userId(2L)
                .status(TaskStatus.COMPLETED)
                .build();

        return taskRepository.saveAll(List.of(task1, task2));
    }
}