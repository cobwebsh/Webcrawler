package org.ecorous.webcrawler

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.DISCORD_WHITE
import com.kotlindiscord.kord.extensions.DISCORD_YELLOW
import com.kotlindiscord.kord.extensions.utils.getJumpUrl
import dev.kord.core.behavior.*
import dev.kord.core.behavior.channel.*
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.channel.TextChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.ecorous.webcrawler.Cases.toColor
import kotlin.coroutines.CoroutineContext



object Utils : CoroutineScope {
    fun sendModLog(guild: Guild, moderator: Member, user: User, type: CaseType, reason: String?, time: Instant, case: Case) {
        launch {
            val channel = guild.getChannelOf<TextChannel>(MODERATION_LOG_CHANNEL_ID)
            channel.createEmbed {
                title = when(type) {
                    CaseType.BAN -> "Banned"
                    CaseType.MUTE -> "Muted"
                    CaseType.KICK -> "Kicked"
                    CaseType.NOTE -> "Note"
                } + " - Case ${case.id}"
                description = when(type) {
                    CaseType.BAN -> "Member banned"
                    CaseType.MUTE -> "Member muted"
                    CaseType.KICK -> "Member kicked"
                    CaseType.NOTE -> "Note created"
                } + if(reason != null) ": $reason" else ""
                color = when(type) {
                    CaseType.BAN -> DISCORD_RED
                    CaseType.KICK -> DISCORD_YELLOW
                    CaseType.MUTE -> DISCORD_BLURPLE
                    CaseType.NOTE -> DISCORD_WHITE
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

    fun sendCaseUpdateLog(guild: Guild, caseID: Number, oldContent: String, newContent: String, moderator: Member, time: Instant) {
        launch {
            val channel = guild.getChannelOf<TextChannel>(MODERATION_LOG_CHANNEL_ID)
            channel.createEmbed {
                title = "Update to case #$caseID"
                field {
                    name = "Old contents"
                    value = oldContent
                }
                field {
                    name = "New contents"
                    value = newContent
                }
                footer {
                    text = "Updated by: ${moderator.displayName} (${moderator.username}) (ID: ${moderator.id})"
                    icon = moderator.avatar?.url
                }
                color = Cases.getCase(caseID).toColor()
                timestamp = time
            }
        }
    }

    fun String.toCase(): CaseType {
        return when(this.lowercase()) {
            "ban" -> CaseType.BAN
            "kick" -> CaseType.KICK
            "mute" -> CaseType.MUTE
            "note" -> CaseType.NOTE
            else -> throw IllegalStateException("invalid toCase() input")
        }
    }

    fun User.getUsername(): String {
        return if (this.discriminator == "0") this.username else this.tag
    }

    override val coroutineContext = Dispatchers.Default
}
