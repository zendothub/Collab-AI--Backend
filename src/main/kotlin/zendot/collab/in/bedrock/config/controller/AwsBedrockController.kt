package zendot.collab.`in`.bedrock.config.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import zendot.collab.`in`.bedrock.config.service.impl.AwsBedrockService

@RestController
class AwsBedrockController(private val awsBedrockService: AwsBedrockService) {

    @GetMapping("/send-message")
    fun sendMessage(@RequestParam prompt: String): Map<String, Any> {
        return awsBedrockService.interactWithAwsBedrock(prompt)
    }
}
