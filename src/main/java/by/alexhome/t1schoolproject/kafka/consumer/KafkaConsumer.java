package by.alexhome.t1schoolproject.kafka.consumer;

import by.alexhome.t1schoolproject.model.dto.TaskDto;
import by.alexhome.t1schoolproject.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "task-updates", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void listenTaskStatusUpdate(List<TaskDto> tasksDto) {
        if (tasksDto.isEmpty()) {
            return;
        }

        for (TaskDto temp : tasksDto) {
            log.info("Received task update for task ID: {}", temp.getId());
            try {
                notificationService.sendNotification(temp);
                log.info("Successfully consume task and send notification for task ID: {}", temp.getId());
            } catch (Exception ex) {
                log.error("Failed to send in kafka for task ID: {}. Error: {}", temp.getUserId(), ex.getMessage(), ex);
            }
        }
    }
}