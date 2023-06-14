package org.ecorous.webcrawler.extensions

import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalRole
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createEmbed
import kotlinx.datetime.Clock
import org.ecorous.webcrawler.SERVER_ID
import org.ecorous.webcrawler.Utils
import org.ecorous.webcrawler.database.DB

class ConfigExtension: Extension() {
    override val name = "config"

    override suspend fun setup() {
        publicSlashCommand {
            name = "config"
            description = "Configure Webcrawler"
            guild(SERVER_ID)

            publicSubCommand(::RolesArgs) {
                name = "roles"
                description = "Set up roles"

                requirePermission(Permission.ManageGuild)

                action {
                    arguments.verified?.let {
                        DB.setConfig("role.verified",it.id.value.toString())
                    }
                    arguments.moderator?.let {
                        DB.setConfig("role.moderator", it.id.value.toString())
                    }
                    arguments.admin?.let {
                        DB.setConfig("role.admin", it.id.value.toString())
                    }

                    respond {
                        content = "Updated roles config."
                    }
                }
            }
        }
    }

    inner class RolesArgs: Arguments() {
        val verified by optionalRole {
            name = "verified"
            description = "The role to give approved users"
        }
        val moderator by optionalRole {
            name = "moderator"
            description = "The role to identify moderators"
        }
        val admin by optionalRole {
            name = "administrator"
            description = "The role to identify administrators"
        }
    }
    inner class ChannelsArgs: Arguments() {
        val getAccess by optionalChannel {
            name = "get_access"
            description = "The channel where users can get access to the server"
        }
        val applications by optionalChannel {
            name = "applications"
            description = "The channel where submitted applications get sent to"
        }
        val modLogs by optionalChannel {
            name = "moderation_logs"
            description = "The channel where moderation logs get sent to"
        }
        val msgLogs by optionalChannel {
            name = "message_logs"
            description = "The channel where message logs get sent"
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