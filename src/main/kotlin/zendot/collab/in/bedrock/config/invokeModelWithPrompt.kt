package zendot.collab.`in`.bedrock.config


import org.json.JSONObject
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse

fun invokeModelWithPrompt(prompt: String): String {
    // Create the BedrockRuntime client
    val client = BedrockRuntimeClient.builder()
        .credentialsProvider(DefaultCredentialsProvider.create())
        .region(Region.US_EAST_1) // Change to the desired region
        .build()

    val modelId = "ai21.j2-mid-v1" // Your model ID here

    // Create the request payload with the prompt
    val nativeRequestTemplate = "{ \"prompt\": \"{{prompt}}\" }"
    val nativeRequest = nativeRequestTemplate.replace("{{prompt}}", prompt)

    return try {
        // Prepare the InvokeModelRequest with the payload
        val request = InvokeModelRequest.builder()
            .body(SdkBytes.fromUtf8String(nativeRequest)) // Convert the request body to bytes
            .modelId(modelId)
            .build()

        // Send the request and capture the response
        val response: InvokeModelResponse = client.invokeModel(request)

        // Parse the response body (as JSON)
        val responseBody = JSONObject(response.body().asUtf8String())

        // Extract the text from the response JSON
        val text = responseBody.getJSONArray("completions")
            .getJSONObject(0)
            .getJSONObject("data")
            .getString("text")

        // Return the generated text from the model
        text
    } catch (e: Exception) {
        e.printStackTrace()
        "Error invoking model: ${e.message}"
    }
}