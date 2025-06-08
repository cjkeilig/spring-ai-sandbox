package com.example.demo.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * REST Controller for handling chat requests using Spring AI.
 */
@RestController
public class ChatEndpoint {

    private final ChatClient chatClient;

    // Spring AI automatically auto-configures a ChatClient.Builder bean
    public ChatEndpoint(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam(value = "message") String message) {
        // The .stream() method on ChatClient returns a Flux<ChatResponse>
        // We then map this Flux to extract the content of each generation
        return chatClient.prompt()
                .user(message)
                .stream()
                .content(); // Extracts the string content from each ChatResponse chunk
    }

    @GetMapping("/chat")
    public Mono<String> chat(@RequestParam(value = "message") String message) {
        // For a non-streaming (blocking) response
        return Mono.fromCallable(() -> chatClient.prompt()
                .user(message)
                .call()
                .content())
                .subscribeOn(Schedulers.boundedElastic());
    }
}