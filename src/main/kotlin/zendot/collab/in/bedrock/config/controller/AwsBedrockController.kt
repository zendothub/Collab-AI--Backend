package zendot.collab.`in`.bedrock.config.controller

import org.springframework.web.bind.annotation.*
import zendot.collab.`in`.bedrock.config.BedrockAgentService

@RestController
class AwsBedrockController(private val bedrockAgentService: BedrockAgentService) {


    @PostMapping("/create-ticket")
    fun createTicket(@RequestBody requestBody: Map<String, Any>): Map<String, Any> {
        val prompt = requestBody["prompt"] as? String ?: "Create a ticket"

        // Call the service and return the response from the agent
        val response = bedrockAgentService.converseWithAgent(prompt)

        // Return the agent's response as the HTTP response
        return mapOf("response" to response)
    }

    @GetMapping("/converse")
    fun converse(@RequestParam prompt: String): String {
        return bedrockAgentService.converseWithAgent(prompt)
    }
}
