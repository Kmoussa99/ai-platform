package ci.aiplatform.chat.api;

import ci.aiplatform.chat.domain.Feedback;
import ci.aiplatform.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {
    private final ChatService service;

    @PutMapping("/{id}/feedback")
    public void feedback(@PathVariable UUID id, @RequestParam Feedback value) {
        service.feedback(id, value);
    }
}

