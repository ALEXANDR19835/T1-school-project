package by.alexhome.t1schoolproject.kafka.producer;

import by.alexhome.t1schoolproject.model.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, TaskDto> kafkaTemplate;

    @Value("${spring.kafka.topic.task-updates}")
    private String taskStatusTopic;

    public void sendMessageStatusUpdate(TaskDto taskDto) {
        try {
            kafkaTemplate.send(taskStatusTopic, taskDto);
            log.info("Send in kafka successfully for task ID:  {}", taskDto.getId());
        } catch (Exception e) {
            log.error("Failed to send in kafka for task ID: {}. Error: {}",taskDto.getUserId(), e.getMessage(), e);
        }
    }
}