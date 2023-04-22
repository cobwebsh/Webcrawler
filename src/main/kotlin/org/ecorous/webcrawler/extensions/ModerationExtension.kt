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
import kotlinx.datetime.Clock
import java.time.ZoneOffset
import java.time.temporal.TemporalAccessor

@OptIn(KordPreview::class)
class ModerationExtension : Extension() {
    override val name = ""

    override suspend fun setup() {
        chatCommand(::KickArgs) {
            name = "kick"
            description = "Kick a user from the server"

            check {
                failIf(event.message.author == null)
            }

            action {
                // Because of the DslMarker annotation KordEx uses, we need to grab Kord explicitly
                val kord = this@ModerationExtension.kord

                if(arguments.reason.isEmpty()) {
                    message.respond("You must supply a kick reason!")
                } else {
                    kord.getGuildOrThrow(SERVER_ID).kick(arguments.target.id, arguments.reason)
                    message.respond("Kicked ${arguments.target.mention}")
                }
            }
        }

        publicSlashCommand(::KickArgs) {
            name = "kick"
            description = "Kick a user from the server"

            guild(SERVER_ID)  // Otherwise it'll take an hour to update
            requirePermission(Permission.KickMembers)
            requireBotPermissions(Permission.KickMembers)
            action {
                // Because of the DslMarker annotation KordEx uses, we need to grab Kord explicitly
                val kord = this@ModerationExtension.kord

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
                guild?.fetchGuild()?.kick(arguments.target.id, arguments.reason)
                
                respond {
                    content = "Kicked ${arguments.target.mention}!"
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
}