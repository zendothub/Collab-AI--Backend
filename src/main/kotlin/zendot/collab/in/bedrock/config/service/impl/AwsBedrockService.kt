package zendot.collab.`in`.bedrock.config.service.impl


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.GetSessionTokenRequest
import software.amazon.awssdk.services.sts.model.GetSessionTokenResponse
import zendot.collab.`in`.bedrock.config.service.BedrockService
import com.fasterxml.jackson.core.type.TypeReference

@Service
class AwsBedrockService : BedrockService {

    override fun interactWithAwsBedrock(prompt: String): Map<String, Any> {
        val awsCredentials = AwsBasicCredentials.create(
            "YOUR_AWS_ACCESS_KEY",  // Replace with your AWS Access Key
            "YOUR_AWS_SECRET_KEY"   // Replace with your AWS Secret Key
        )

        // Initialize AWS STS client to get temporary credentials
        val stsClient = StsClient.builder()
            .region(Region.US_EAST_1)  // Specify the region
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .build()

        // Request temporary security credentials
        val sessionTokenResponse: GetSessionTokenResponse = stsClient.getSessionToken(
            GetSessionTokenRequest.builder().build()
        )

        val sessionToken = sessionTokenResponse.credentials().sessionToken()

        // Prepare HTTP request to interact with AWS Bedrock API
        val httpClient = HttpClients.createDefault()
        val httpPost = HttpPost("https://bedrock-api-url.amazonaws.com/bedrock-endpoint") // Replace with actual API endpoint

        // Set headers for AWS authentication
        httpPost.setHeader("Authorization", "Bearer $sessionToken")
        httpPost.setHeader("Content-Type", "application/json")

        // Prepare the payload (replace with actual payload for Bedrock API)
        val payload = """
            {
                "param1": "value1",
                "param2": "value2"
            }
        """
        httpPost.entity = StringEntity(payload)

        // Execute the request and handle the response
        return try {
            val response = httpClient.execute(httpPost)
            val responseEntity = response.entity
            val responseContent = responseEntity.content.reader().use { it.readText() }

            // Parse JSON response using TypeReference for Map<String, Any>
            val objectMapper = jacksonObjectMapper()
            objectMapper.readValue<Map<String, Any>>(responseContent)  // Use the correct type here
        } catch (e: Exception) {
            e.printStackTrace()
            mapOf("error" to "Error occurred while calling AWS Bedrock API: ${e.message}")
        } finally {
            httpClient.close()
        }
    }
}
