package by.alexhome.t1schoolproject.mapper;

import by.alexhome.t1schoolproject.model.dto.TaskDto;
import by.alexhome.t1schoolproject.model.entity.Task;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskDto taskToTaskDTO(Task task);
    Task taskDTOToTask(TaskDto taskDto);
}