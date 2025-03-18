package zendot.collab.`in`.bedrock.config

import org.json.JSONObject
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse
import software.amazon.awssdk.services.bedrockruntime.model.Message
import org.springframework.beans.factory.annotation.Value
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider

//
//@Service
//class BedrockAgentService {
////
////    private val client: BedrockRuntimeClient = BedrockRuntimeClient.builder()
////        .credentialsProvider(DefaultCredentialsProvider.create())
////        .region(Region.US_EAST_1)  // Specify your region
////        .build()
////
////    fun converseWithAgent(prompt: String): String {
////        val modelId = "ai21.j2-mid-v1"  // Replace with your actual model ID
////
////        // Create a message to send to the agent
////        val message = Message.builder()
////            .content(ContentBlock.fromText(prompt))
////            .role(ConversationRole.USER)
////            .build()
////
////        try {
////            // Create the request to invoke the model
////            val request = InvokeModelRequest.builder()
////                .modelId(modelId)
////                .body(SdkBytes.fromUtf8String(message.content().toString()))  // Convert content to byte array
////                .build()
////
////            // Call Bedrock API to get the response
////            val response: InvokeModelResponse = client.invokeModel(request)
////
////            // Extract response body as a String
////            val responseBody = response.body().asUtf8String()
////
////            // Log or process the response text
////            println("Response: $responseBody")
////
////            // Assuming the response contains a `completions` field with text data
////            val text = JSONObject(responseBody).getJSONArray("completions")
////                .getJSONObject(0)
////                .getJSONObject("data")
////                .getString("text")
////
////            // Return the response text
////            return text
////        } catch (e: Exception) {
////            e.printStackTrace()
////            return "Error calling Bedrock API: ${e.message}"
////        }
////    }
//
//
//}
@Service
class BedrockAgentService(
    @Value("\${aws.accessKeyId}") private val accessKeyId: String,
    @Value("\${aws.secretAccessKey}") private val secretAccessKey: String,
    @Value("\${aws.region}") private val region: String
) {

    private val client: BedrockRuntimeClient = BedrockRuntimeClient.builder()
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey))) // Use StaticCredentialsProvider
        .region(Region.of(region))  // Set region from properties
        .build()

    fun converseWithAgent(prompt: String): String {
        // Use Agent ARN (Inference Profile ARN)
        val modelArn = "arn:aws:bedrock:ap-south-1:851725265776:inference-profile/apac.amazon.nova-pro-v1:0"

        // Create a message to send to the agent
        val message = Message.builder()
            .content(ContentBlock.fromText(prompt))
            .role(ConversationRole.USER)
            .build()

        try {
            // Create the request to invoke the agent (using the Agent ARN)
            val request = InvokeModelRequest.builder()
                .modelId(modelArn)  // Agent ARN here
                .body(SdkBytes.fromUtf8String(prompt))
                .build()

            // Call Bedrock API to get the response from the agent
            val response: InvokeModelResponse = client.invokeModel(request)

            // Extract response body as a String
            val responseBody = response.body().asUtf8String()

            // Assuming the response contains a `completions` field with text data
            val text = JSONObject(responseBody).getJSONArray("completions")
                .getJSONObject(0)
                .getJSONObject("data")
                .getString("text")

            // Return the response text
            return text
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error calling Bedrock API: ${e.message}"
        }
    }
}



