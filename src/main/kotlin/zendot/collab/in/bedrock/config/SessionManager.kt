package zendot.collab.`in`.bedrock.config

import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class SessionManager {
    private val sessions = ConcurrentHashMap<String, Session>()
    private val sessionTimeout = Duration.ofMinutes(15)

    data class Session(
        val id: String,
        var lastAccessed: Instant = Instant.now(),
        val messageHistory: MutableList<String> = mutableListOf()
    )

    fun createNewSession(): Session {
        val sessionId = UUID.randomUUID().toString()
        return Session(sessionId).also {
            sessions[sessionId] = it
        }
    }

    fun getSession(sessionId: String): Session? {
        return sessions[sessionId]?.also {
            if (Instant.now().isAfter(it.lastAccessed.plus(sessionTimeout))) {
                sessions.remove(sessionId)
                return null
            }
            it.lastAccessed = Instant.now()
        }
    }

    fun cleanupExpiredSessions() {
        sessions.values.removeIf { session ->
            Instant.now().isAfter(session.lastAccessed.plus(sessionTimeout))
        }
    }
}