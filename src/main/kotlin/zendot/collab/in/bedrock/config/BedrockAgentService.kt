package zendot.collab.`in`.bedrock.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeAgentRequest
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeAgentResponseHandler
import software.amazon.awssdk.services.bedrockagentruntime.model.PayloadPart

import java.nio.charset.StandardCharsets

@Service
class BedrockAgentService(
    @Value("\${aws.accessKeyId}") private val accessKeyId: String,
    @Value("\${aws.secretAccessKey}") private val secretAccessKey: String,
    @Value("\${aws.region}") private val region: String,
    @Value("\${aws.agentId}") private val agentId: String,
    @Value("\${aws.agentAliasId}") private val agentAliasId: String,
    @Value("\${aws.sessionId}") private val sessionId: String
) {

    // Initialize the BedrockAgentRuntimeClient with credentials and region
    private val client: BedrockAgentRuntimeAsyncClient = BedrockAgentRuntimeAsyncClient.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            )
        )
        .region(Region.of(region))
        .build()

    /**
     * Invokes the Bedrock agent and returns a CompletableFuture<Void> that completes when the response is fully processed.
     *
     * @param inputText The input text to send to the agent.
     * @return A CompletableFuture<Void> that completes when the response is fully processed.
     */

    fun converseWithAgent(prompt: String): Flux<String> {
        val request = InvokeAgentRequest.builder()
            .agentId(agentId)
            .agentAliasId(agentAliasId)
            .sessionId(sessionId)
            .inputText(prompt)
            .build()

        // Create a Flux to stream the response chunks
        return Flux.create { sink: FluxSink<String> ->
            val handler = InvokeAgentResponseHandler.builder()
                .onResponse { response ->
                    println("Initial response received.")
                }
                .onEventStream { subscriber ->
                    subscriber.subscribe { event ->
                        when (event) {
                            is PayloadPart -> {
                                // Handle the payload chunk
                                val sdkBytes: SdkBytes = event.bytes()
                                val byteBuffer = sdkBytes.asByteBuffer() // Convert SdkBytes to ByteBuffer
                                val decodedResponse = StandardCharsets.UTF_8.decode(byteBuffer).toString()
                                println("Chunk received: $decodedResponse")
                                sink.next(decodedResponse) // Emit the chunk to the Flux
                            }

                            else -> {
                                // Handle other types of events (e.g., FilePart, ReturnControlPayload, TracePart)
                                println("Received event: ${event.sdkEventType()}")
                            }
                        }
                    }
                }
                .onError { exception ->
                    exception.printStackTrace()
                    sink.error(exception)
                }
                .onComplete {
                    sink.complete()
                }
                .build()

            // Invoke the agent asynchronously
            client.invokeAgent(request, handler)
        }
    }

}
