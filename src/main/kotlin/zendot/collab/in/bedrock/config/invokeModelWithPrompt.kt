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
        .region(Region.US_EAST_1)
        .build()

    val modelId = "ai21.j2-mid-v1"

    // Create the request payload with the prompt
    val nativeRequestTemplate = "{ \"prompt\": \"{{prompt}}\" }"
    val nativeRequest = nativeRequestTemplate.replace("{{prompt}}", prompt)

    return try {
        val request = InvokeModelRequest.builder()
            .body(SdkBytes.fromUtf8String(nativeRequest))
            .modelId(modelId)
            .build()

        val response: InvokeModelResponse = client.invokeModel(request)

        val responseBody = JSONObject(response.body().asUtf8String())

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