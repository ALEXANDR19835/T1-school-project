package by.alexhome.t1schoolproject.model.dto;

import by.alexhome.t1schoolproject.model.enums.TaskStatus;
import lombok.Data;

@Data
public class TaskDto {
    Long id;
    String title;
    String description;
    Long userId;
    TaskStatus status;
}