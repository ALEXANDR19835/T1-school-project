package by.alexhome.t1schoolproject.service;


import by.alexhome.t1schoolproject.model.EmailProperties;
import by.alexhome.t1schoolproject.model.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender emailSender;
    private final EmailProperties emailProperties;

    public void sendNotification(TaskDto taskDto) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailProperties.getUsername());
        message.setTo(emailProperties.getUsername());
        message.setSubject("Task Status Update");
        message.setText("Task with ID: " + taskDto.getUserId() + " has been updated. New status: " + taskDto.getStatus());

        try {
            emailSender.send(message);
            log.info("Email send successfully for task ID: {}", taskDto.getUserId());
        } catch (Exception e) {
            log.error("Failed to send email for task ID: {}. Error: {}", taskDto.getUserId(), e.getMessage(), e);
        }
    }
}