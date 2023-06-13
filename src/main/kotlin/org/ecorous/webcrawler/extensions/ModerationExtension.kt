package org.ecorous.webcrawler.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.dm
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.rest.builder.message.create.embed
import org.ecorous.webcrawler.SERVER_ID
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.utils.selfMember
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.ban
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.datetime.Clock
import org.ecorous.webcrawler.ModerationLogType
import org.ecorous.webcrawler.Utils
import java.time.ZoneOffset
import java.time.temporal.TemporalAccessor

@OptIn(KordPreview::class)
class ModerationExtension : Extension() {
    override val name = ""

    override suspend fun setup() {
        publicSlashCommand(::KickArgs) {
            name = "kick"
            description = "Kick a user from the server"

            guild(SERVER_ID)  // Otherwise it'll take an hour to update
            requirePermission(Permission.KickMembers)
            requireBotPermissions(Permission.KickMembers)
            action {
                val channel = arguments.target.getDmChannel()
                channel.createEmbed {
                    title = "Kicked!"
                    description = "${arguments.target.mention}, you have been kicked from `${guild?.fetchGuild()?.name}`\nYou can re-join with a new invite link, but any further issues will be punished with a ban."
                    footer {
                        text = "Moderator: ${user.asUser().tag} (${user.asUser().id})"
                        icon = user.asUser().avatar?.url
                    }
                    timestamp = Clock.System.now()
                    field {
                        name = "Reason"
                        value = arguments.reason
                        inline = false
                    }
                    color = DISCORD_RED
                }
                guild!!.fetchGuild().kick(arguments.target.id, arguments.reason)
                Utils.sendModLog(guild!!.fetchGuild(), user.fetchMember(guild!!.id), arguments.target, ModerationLogType.KICK, arguments.reason, Clock.System.now())
                respond {
                    content = "Kicked ${arguments.target.mention}!"
                }
            }
        }

        publicSlashCommand (::BanArgs) {
            name = "ban"
            description = "Ban a user from the server"

            guild(SERVER_ID)
            requirePermission(Permission.BanMembers)
            requireBotPermissions(Permission.BanMembers)
            action {
                val channel = arguments.target.getDmChannel()
                channel.createEmbed {
                    title = "Banned!"
                    description = "${arguments.target.mention}, you have been banned from `${guild?.fetchGuild()?.name}`!\nTo appeal, message `Ecorous#9052` (<@604653220341743618>)."
                    footer {
                        text = "Moderator: ${user.asUser().tag} (${user.asUser().id})"
                        icon = user.asUser().avatar?.url
                    }
                    timestamp = Clock.System.now()
                    field {
                        name = "Reason"
                        value = arguments.reason
                        inline = false
                    }
                    color = DISCORD_RED
                }
                guild?.fetchGuild()?.ban (arguments.target.id) {
                    reason = arguments.reason
                }
                Utils.sendModLog(guild!!.fetchGuild(), user.fetchMember(guild!!.id), arguments.target, ModerationLogType.BAN, arguments.reason, Clock.System.now())

                respond {
                    content = "Banned ${arguments.target.mention} (${arguments.target.id})!"
                }
            }
        }
    }

    inner class KickArgs : Arguments() {
        val target by user {
            name = "user"
            description = "Member to kick"
        }

        val reason by string {
            name = "reason"
            description = "The reason to kick this member"
        }
    }

    inner class KickArgsaa : Arguments() {
        val target by user {
            name = "user"
            description = "Member to kick"
        }

        val reason by string {
            name = "reason"
            description = "The reason to kick this member"
        }
    }

    inner class BanArgs : Arguments() {
        val target by user {
            name = "user"
            description = "Member to ban"
        }

        val reason by string {
            name = "reason"
            description = "The reason to ban this member"
        }
    }
}