package org.ecorous.webcrawler

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.DISCORD_WHITE
import com.kotlindiscord.kord.extensions.DISCORD_YELLOW
import com.kotlindiscord.kord.extensions.utils.getJumpUrl
import dev.kord.core.behavior.*
import dev.kord.core.behavior.channel.*
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.TextChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.coroutines.CoroutineContext

enum class ModerationLogType {
    BAN,
    KICK,
    MUTE,
    NOTE,
}

enum class MessageLogType {
    EDIT,
    DELETE
}

object Utils : CoroutineScope {
    fun getNextCaseNumber(): Int {
        return -9999
    }
    fun sendModLog(guild: Guild, moderator: Member, user: User, type: ModerationLogType, reason: String?, time: Instant) {
        launch {
            val channel = guild.getChannelOf<TextChannel>(MODERATION_LOG_CHANNEL_ID)
            channel.createEmbed {
                title = when(type) {
                    ModerationLogType.BAN -> "Banned"
                    ModerationLogType.MUTE -> "Muted"
                    ModerationLogType.KICK -> "Kicked"
                    ModerationLogType.NOTE -> "Note"
                } + " - Case <case number>"
                description = when(type) {
                    ModerationLogType.BAN -> "Member banned"
                    ModerationLogType.MUTE -> "Member muted"
                    ModerationLogType.KICK -> "Member kicked"
                    ModerationLogType.NOTE -> "Note created"
                } + if(reason != null) ": $reason" else ""
                color = when(type) {
                    ModerationLogType.BAN -> DISCORD_RED
                    ModerationLogType.KICK -> DISCORD_YELLOW
                    ModerationLogType.MUTE -> DISCORD_BLURPLE
                    ModerationLogType.NOTE -> DISCORD_WHITE
                }
                author {
                    icon = user.avatar?.url
                    name = "Member: ${user.tag} (${user.id})"
                }
                footer {
                    icon = moderator.avatar?.url
                    text = "Moderator: ${moderator.tag} (${moderator.id})"
                }
                timestamp = time
            }
        }
    }

    fun sendMsgLog(guild: Guild, member: Member, message: Message, type: MessageLogType, time: Instant, oldText: String?) {
        launch {
            val channel = guild.getChannelOf<TextChannel>(MESSAGE_LOG_CHANNEL_ID)
            channel.createEmbed {
                title = "Message " + when(type) {
                    MessageLogType.EDIT -> "edited"
                    MessageLogType.DELETE -> "deleted"
                }
                field {
                    name = "Channel"
                    value = message.channel.mention
                }
                when(type) {
                    MessageLogType.EDIT -> {
                        field {
                            name = "Previous Message Contents"
                            value = "${oldText!!}\n```${oldText}```"
                        }
                        field {
                            name = "New Message Contents"
                            value = "${message.content}\n```${message.content}```"
                        }
                    }
                    else -> {
                        field {
                            name = "Message Contents"
                            value = "${message.content}\n```${message.content}```"
                        }
                    }
                }
                when(type) {
                    MessageLogType.DELETE -> {}
                    else -> {
                        field {
                            name = "Message Link"
                            value = message.getJumpUrl()
                        }
                    }
                }
                color = when(type) {
                    MessageLogType.EDIT -> DISCORD_YELLOW
                    MessageLogType.DELETE -> DISCORD_RED
                }
                footer {
                    icon = member.avatar?.url
                    text = "Member: ${member.tag} (${member.id})"
                }
                timestamp = time
            }
        }
    }

    override val coroutineContext = Dispatchers.Default
}
