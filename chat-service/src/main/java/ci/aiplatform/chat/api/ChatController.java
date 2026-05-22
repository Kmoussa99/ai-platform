package ci.aiplatform.chat.api;

import ci.aiplatform.chat.domain.*;
import ci.aiplatform.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService service;

    public record CreateConvReq(String title, ContextMode contextMode) {}
    public record SendMsgReq(String content) {}

    @PostMapping
    public Conversation create(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateConvReq req) {
        return service.createConversation(jwt.getSubject(), req.title(), req.contextMode());
    }

    @GetMapping
    public List<Conversation> list(@AuthenticationPrincipal Jwt jwt) {
        return service.listConversations(jwt.getSubject());
    }

    @GetMapping("/{id}/messages")
    public List<Message> messages(@PathVariable UUID id) { return service.getMessages(id); }

    /** SSE streaming endpoint for chat. */
    @PostMapping(value = "/{id}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> send(@AuthenticationPrincipal Jwt jwt,
                                               @PathVariable UUID id,
                                               @RequestBody SendMsgReq req) {
        String level = jwt.getClaimAsString("data_access_level");
        return service.stream(id, jwt.getSubject(), level, req.content())
            .map(tok -> ServerSentEvent.<String>builder().data(tok).build())
            .concatWith(Flux.just(ServerSentEvent.<String>builder().event("done").data("").build()));
    }
}

