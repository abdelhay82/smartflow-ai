package ma.smartflow.taskservice.kafkaProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.smartflow.taskservice.dtos.TaskEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskEventProducer {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;

    public void sendTaskEvent(TaskEvent event){
        log.info("sending kafka event: {} for task {}", event.getEventType(), event.getTitle());

        kafkaTemplate.send("task-event", event.getTaskId().toString(), event)
                .whenComplete((result, ex) -> {
                    if(ex != null){
                        log.error("Failed to send event: {}", ex.getMessage());
                    } else {
                        log.info("Event sent successfully to partition: {}", result.getRecordMetadata().partition());
                    }
                });
    }
}
