package zendot.collab.`in`

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import zendot.collab.`in`.bedrock.config.BedrockAgentService

@SpringBootApplication
class CollabAiApplication {

	@Bean
	fun run(agentService: BedrockAgentService) = CommandLineRunner {
		// Simulating the prompt sent to the agent
		val prompt = "Create a ticket"

		// Call the service and print the response
		val response = agentService.converseWithAgent(prompt)
		println("Agent Response: $response")
	}

	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			runApplication<CollabAiApplication>(*args)
		}
	}
}
