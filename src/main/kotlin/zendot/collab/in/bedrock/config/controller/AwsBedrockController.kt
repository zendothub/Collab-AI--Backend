package zendot.collab.`in`.bedrock.config.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import zendot.collab.`in`.bedrock.config.BedrockAgentService
import java.util.concurrent.CompletableFuture

@RestController
class AwsBedrockController(private val bedrockAgentService: BedrockAgentService) {

    @PostMapping("/create-ticket")
    fun createTicket(@RequestBody requestBody: Map<String, Any>): Map<String, Any> {
        val prompt = requestBody["prompt"] as? String ?: "Create a ticket"

        val response = bedrockAgentService.converseWithAgent(prompt)
        return mapOf("response" to response)
    }

    @GetMapping("/converse", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun converse(@RequestParam prompt: String): Flux<String> {
        return bedrockAgentService.converseWithAgent(prompt)
    }

}

