package zendot.collab.`in`.bedrock.config.service

interface BedrockService {
    fun interactWithAwsBedrock(prompt: String): Map<String, Any>
}
