package by.alexhome.t1schoolproject.service;

import by.alexhome.t1schoolproject.exception.TaskNotFoundException;
import by.alexhome.t1schoolproject.kafka.producer.KafkaProducer;
import by.alexhome.t1schoolproject.mapper.TaskMapper;
import by.alexhome.t1schoolproject.model.dto.TaskDto;
import by.alexhome.t1schoolproject.model.entity.Task;
import by.alexhome.t1schoolproject.model.enums.TaskStatus;
import by.alexhome.t1schoolproject.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private final TaskDto taskDto = TaskDto.builder()
            .id(1L)
            .title("Test Title")
            .description("Test Description")
            .userId(1L)
            .status(TaskStatus.COMPLETED)
            .build();

    private final Task task = Task.builder()
            .id(1L)
            .title("Test Title")
            .description("Test Description")
            .userId(1L)
            .status(TaskStatus.COMPLETED)
            .build();

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("Создание задачи")
    void createTask() {

        when(taskMapper.taskDTOToTask(taskDto)).thenReturn(task); // Маппинг DTO -> Entity
        when(taskRepository.save(task)).thenReturn(task); // Сохранение задачи в репозиторий
        when(taskMapper.taskToTaskDTO(task)).thenReturn(taskDto); // Маппинг Entity -> DTO

        TaskDto result = taskService.createTask(taskDto);

        assertAll(
                () -> assertEquals(taskDto.getId(), result.getId()),
                () -> assertEquals(taskDto.getTitle(), result.getTitle()),
                () -> assertEquals(taskDto.getDescription(), result.getDescription()),
                () -> assertEquals(taskDto.getUserId(), result.getUserId()),
                () -> assertEquals(taskDto.getStatus(), result.getStatus())
        );

        verify(taskMapper).taskDTOToTask(taskDto);
        verify(taskRepository).save(task);
        verify(taskMapper).taskToTaskDTO(task);
    }

    @Test
    @DisplayName("Создание задачи: должно выбросить исключение при ошибке сохранения")
    void createTaskWhenSaveFailsShouldThrowException() {
        when(taskMapper.taskDTOToTask(taskDto)).thenReturn(task);
        when(taskRepository.save(task)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> taskService.createTask(taskDto));

        verify(taskMapper).taskDTOToTask(taskDto);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("Получение задачи по ID: задача существует")
    void getTaskById() {

        Long taskId = taskDto.getId();
        Task taskFromDb = task;
        TaskDto expectedDto = taskDto;

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskFromDb));
        when(taskMapper.taskToTaskDTO(taskFromDb)).thenReturn(expectedDto);

        TaskDto result = taskService.getTaskById(taskId);

        assertEquals(expectedDto, result);
        verify(taskRepository).findById(taskId);
        verify(taskMapper).taskToTaskDTO(taskFromDb);
    }

    @Test
    @DisplayName("Получение задачи по ID: задача не существует")
    void getTaskByIdWhenTaskNotExistsShouldThrowTaskNotFoundException() {

        Long taskId = taskDto.getId();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> taskService.deleteTask(taskId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Task with ID = " + taskId + " not found", exception.getReason());

        verify(taskRepository).findById(taskId);
    }


    @Test
    @DisplayName("Обновление задачи: задача существует и статус изменился, сообщение отправлено в Kafka")
    void updateTaskWhenTaskExistsAndStatusChangedShouldSendKafkaMessage() {

        Long taskId = taskDto.getId();
        Task existingTask = Task.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .userId(task.getUserId())
                .status(TaskStatus.IN_PROGRESS)
                .build();

        Task updatedTask = Task.builder()
                .id(task.getId())
                .title(taskDto.getTitle())
                .description(taskDto.getDescription())
                .userId(taskDto.getUserId())
                .status(taskDto.getStatus())
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(updatedTask);
        when(taskMapper.taskToTaskDTO(updatedTask)).thenReturn(taskDto);

        doNothing().when(kafkaProducer).sendMessageStatusUpdate(taskDto);

        TaskDto result = taskService.updateTask(taskId, taskDto);

        assertEquals(taskDto, result);
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(existingTask);
        verify(kafkaProducer).sendMessageStatusUpdate(taskDto);
    }

    @Test
    @DisplayName("Обновление задачи: задача не найдена, выбрасывается исключение")
    void updateTaskWhenTaskNotFoundShouldThrowTaskNotFoundException() {

        Long taskId = taskDto.getId();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(taskId, taskDto));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
        verify(kafkaProducer, never()).sendMessageStatusUpdate(any());
    }

    @Test
    @DisplayName("Обновление задачи: задача существует, но статус не изменился, Kafka не вызывается")
    void updateTaskWhenStatusNotChangedShouldNotSendKafkaMessage() {

        Long taskId = taskDto.getId();
        Task existingTask = Task.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .userId(task.getUserId())
                .status(taskDto.getStatus()) // Статус такой же
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.taskToTaskDTO(existingTask)).thenReturn(taskDto);

        TaskDto result = taskService.updateTask(taskId, taskDto);

        assertEquals(taskDto, result);
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(existingTask);
        verify(kafkaProducer, never()).sendMessageStatusUpdate(any()); // Kafka не вызывается
    }

    @Test
    @DisplayName("Удаление задачи: если задача существует, она успешно удаляется")
    void deleteTask() {

        Long taskId = task.getId();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).deleteById(taskId);

        taskService.deleteTask(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).deleteById(taskId);
    }

    @Test
    @DisplayName("Удаление задачи: если задача не найдена, выбрасывается TaskNotFoundException")
    void deleteTaskWhenTaskNotFoundShouldThrowTaskNotFoundException() {

        Long taskId = task.getId();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> taskService.deleteTask(taskId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Task with ID = " + taskId + " not found", exception.getReason());

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Получение всех задач: список не пустой")
    void getAllTasks() {

        when(taskRepository.findAll()).thenReturn(List.of(task));
        when(taskMapper.taskToTaskDTO(task)).thenReturn(taskDto);

        List<TaskDto> result = taskService.getAllTasks();

        assertEquals(1, result.size());
        assertEquals(taskDto, result.get(0));

        verify(taskRepository).findAll();
        verify(taskMapper).taskToTaskDTO(task);
    }

    @Test
    @DisplayName("Получение всех задач: возвращается пустой список, если задач нет")
    void getAllTasksWhenNoTasksExistShouldReturnEmptyList() {

        when(taskRepository.findAll()).thenReturn(Collections.emptyList());

        List<TaskDto> result = taskService.getAllTasks();

        assertTrue(result.isEmpty());

        verify(taskRepository).findAll();
        verify(taskMapper, never()).taskToTaskDTO(any());
    }
}