//package zendot.collab.`in`.bedrock.config.service.impl
//
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.fasterxml.jackson.module.kotlin.readValue
//import org.apache.http.client.methods.HttpPost
//import org.apache.http.entity.StringEntity
//import org.apache.http.impl.client.HttpClients
//import org.slf4j.Logger
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Service
//import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
//import software.amazon.awssdk.regions.Region
//import zendot.collab.`in`.bedrock.config.service.BedrockService
//import software.amazon.awssdk.services.bedrockagentruntime
//
//
//@Service
//class AwsBedrockService : BedrockService {
//
//    private val logger: Logger = LoggerFactory.getLogger(AwsBedrockService::class.java)
//
//    override fun interactWithAwsBedrock(prompt: String): Map<String, Any> {
//        return try {
//            // Prepare HTTP Client
//            val httpClient = HttpClients.createDefault()
//            val httpPost = HttpPost("https://bedrock-api-url.amazonaws.com/bedrock-endpoint") // Replace with the actual Bedrock API endpoint
//
//            // Prepare the payload (request body)
//            val objectMapper = jacksonObjectMapper()
//            val jsonPayload = objectMapper.writeValueAsString(
//                mapOf("task" to prompt) // Sending the task as the prompt (e.g., create a ticket)
//            )
//
//            // Set headers for authentication and content type
//            httpPost.setHeader("Authorization", "Bearer YOUR_BEARER_TOKEN") // Set your actual Bearer token
//            httpPost.setHeader("Content-Type", "application/json")
//            httpPost.entity = StringEntity(jsonPayload)
//
//            // Send the request to Bedrock Agent
//            val response = httpClient.execute(httpPost)
//            val responseBody = response.entity.content.reader().use { it.readText() }
//
//            // Log the response for debugging
//            logger.info("Response from Bedrock Agent: $responseBody")
//
//            // Parse and return the response as a map
//            val agentResponse: Map<String, Any> = objectMapper.readValue(responseBody)
//
//            // Check if the response asks for additional fields (this depends on how your agent is configured)
//            if (agentResponse["action"] == "askForFields") {
//                val requiredFields = agentResponse["fields"] as? List<String> ?: emptyList()
//                return mapOf("askForFields" to requiredFields)
//            }
//
//            // If no fields are required, proceed to create the Jira ticket or process the result
//            mapOf("success" to true, "message" to "Ticket created successfully")
//
//        } catch (e: Exception) {
//            logger.error("Error calling Bedrock API", e)
//            mapOf("error" to "Failed to communicate with Bedrock API: ${e.message}")
//        }
//    }
//}
