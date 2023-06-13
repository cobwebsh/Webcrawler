package org.ecorous.webcrawler.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalRole
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.kord.common.entity.Permission
import org.ecorous.webcrawler.SERVER_ID
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
                        DB.setConfig("role.verified",it.id.value.toLong())
                    }
                    arguments.moderator?.let {
                        DB.setConfig("role.moderator", it.id.value.toLong())
                    }
                    arguments.admin?.let {
                        DB.setConfig("role.admin", it.id.value.toLong())
                    }
                }
            }
        }
    }

    inner class RolesArgs: Arguments() {
        val verified by optionalRole {
            name = "Verified"
            description = "The role to give approved users"
        }
        val moderator by optionalRole {
            name = "Moderator"
            description = "The role to identify moderators"
        }
        val admin by optionalRole {
            name = "Administrator"
            description = "The role to identify administrators"
        }
    }
    inner class ChannelsArgs: Arguments() {
        val getAccess by optionalChannel {
            name = "Get Access"
            description = "The channel where users can get access to the server"
        }
        val applications by optionalChannel {
            name = "Applications"
            description = "The channel where submitted applications get sent to"
        }
        val modLogs by optionalChannel {
            name = "Moderation Logs"
            description = "The channel where moderation logs get sent to"
        }
        val msgLogs by optionalChannel {
            name = "Message Logs"
            description = "The channel where message logs get sent"
        }
    }
}