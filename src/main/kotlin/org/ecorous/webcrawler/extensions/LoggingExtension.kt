package org.ecorous.webcrawler.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.event.guild.BanAddEvent
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kord.core.event.message.MessageUpdateEvent
import kotlinx.datetime.Clock
import org.ecorous.webcrawler.MessageLogType
import org.ecorous.webcrawler.Utils

class LoggingExtension: Extension() {
    override val name: String = "logging"
    override suspend fun setup() {
        event<MessageUpdateEvent> {
            action {
                val newContent = event.getMessage().content
                val oldContent = event.old!!.content
                if (oldContent == newContent) return@action
                val message = event.getMessage()
                val guild = message.getGuild()
                Utils.sendMsgLog(guild, message.author!!.fetchMember(event.getMessage().getGuild().id), event.getMessage(), MessageLogType.EDIT, Clock.System.now(), event.old!!.content)
            }
        }
    }
}