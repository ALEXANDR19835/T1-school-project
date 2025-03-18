package by.alexhome.t1schoolproject.mapper;

import by.alexhome.t1schoolproject.model.dto.TaskDto;
import by.alexhome.t1schoolproject.model.entity.Task;
import by.alexhome.t1schoolproject.model.enums.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskMapperTest {

    private final TaskMapper taskMapper = Mappers.getMapper(TaskMapper.class);

    @Test
    @DisplayName("Тест Task -> TaskDto")
    void taskToTaskDTOShouldMapCorrectly() {
        Task task = Task.builder()
                .id(1L)
                .title("Test Title")
                .description("Test Description")
                .userId(1L)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        TaskDto taskDto = taskMapper.taskToTaskDTO(task);

        assertNotNull(taskDto);
        assertEquals(task.getId(), taskDto.getId());
        assertEquals(task.getTitle(), taskDto.getTitle());
        assertEquals(task.getDescription(), taskDto.getDescription());
        assertEquals(task.getUserId(), taskDto.getUserId());
        assertEquals(task.getStatus(), taskDto.getStatus());
    }

    @Test
    @DisplayName("Тест TaskDto -> Task")
    void taskDTOToTaskShouldMapCorrectly() {
        TaskDto taskDto = TaskDto.builder()
                .id(2L)
                .title("Test Title")
                .description("Test Description")
                .userId(2L)
                .status(TaskStatus.COMPLETED)
                .build();

        Task task = taskMapper.taskDTOToTask(taskDto);

        assertNotNull(task);
        assertEquals(taskDto.getId(), task.getId());
        assertEquals(taskDto.getTitle(), task.getTitle());
        assertEquals(taskDto.getDescription(), task.getDescription());
        assertEquals(taskDto.getUserId(), task.getUserId());
        assertEquals(taskDto.getStatus(), task.getStatus());
    }
}