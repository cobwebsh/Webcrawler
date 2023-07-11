package org.ecorous.webcrawler.extensions

import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingDefaultingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import com.kotlindiscord.kord.extensions.utils.hasPermission
import dev.kord.common.entity.ArchiveDuration
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.thread.TextChannelThread
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.flow.firstOrNull
import org.ecorous.webcrawler.*
import org.ecorous.webcrawler.database.DB

class MembershipScreeningExtension : Extension() {
	override val name = "test"

	override suspend fun setup() {
		event<MessageCreateEvent> {
			action {
				if (event.message.author == null && event.message.channelId == APPLICATIONS_CHANNEL_ID) {
                    val message = event.message
					val channel = message.channel
					val embeds = message.embeds
					if (embeds.firstOrNull() == null) {
                        return@action
					}
					val embed = message.embeds.first()
					if (embed.author != null) {
						val userText = embed.author?.name!!
						if (event.getGuildOrNull() != null) {
							val guild = event.getGuildOrNull()!!
						    val applicationMember = guild.members.firstOrNull {
                                it.username == userText
                            }
                            var buttonMessage: Message? = null
                            var applicationThread: TextChannelThread? = null
                            buttonMessage = channel.createMessage {
							    embed {
                                    author {
                                        name = applicationMember?.username
                                        icon = applicationMember?.avatar?.url
                                    }
                                    description = applicationMember?.mention
                                }
                                components {
                                    publicButton {
                                        label = "Accept"
                                        style = ButtonStyle.Success
                                        action {
                                            val memberX = member!!.fetchMember()
                                            if (!memberX.hasPermission(Permission.ManageGuild)) respondEphemeral {
                                                content = "You do not have permission to perform this action"
                                                return@action
                                            }
                                            applicationMember?.addRole(DB.getConfigSnowflake("role.verified"), "Application accepted by ${memberX.username}#${memberX.discriminator} (${memberX.id})")
                                            channel.createEmbed {
                                                author {
                                                    name = memberX.username
                                                    icon = memberX.avatar?.url
                                                }
                                                color = DISCORD_GREEN
                                                title = "User ${applicationMember?.username} accepted"
                                            }
                                            buttonMessage?.delete()
                                        }
                                    }
                                    publicButton {
                                        label = "Deny"
                                        style = ButtonStyle.Danger
                                        action {
                                            val memberX = member!!.fetchMember()
                                            if (!memberX.hasPermission(Permission.ManageGuild)) respondEphemeral {
                                                content = "You do not have permission to perform this action"
                                                return@action
                                            }
                                            val dmChannel = applicationMember!!.getDmChannel()
                                            dmChannel.createEmbed {
                                                title = "You have been denied access to the server."
                                                description = "If not, please send a messasge to Ecorous#9052 (<@604653220341743618>)"
                                            }
                                            applicationMember.kick("Application denied by ${memberX.username} (${memberX.id})")
                                            channel.createEmbed {
                                                author {
                                                    name = "${memberX.username}"
                                                    icon = memberX.avatar?.url
                                                }
                                                color = DISCORD_RED
                                                title = "User ${applicationMember.username} denied"
                                            }
                                        }
                                    }
                                    publicButton {
                                        label = "Open Thread"
                                        style = ButtonStyle.Secondary
                                        action {
                                            val memberX = member!!.fetchMember()
                                            if (memberX.hasPermission(Permission.ManageGuild)) respondEphemeral {
                                                content = "You do not have permission to perform this action"
                                            } else return@action
                                            val accessChannel = guild.getChannel(ACCESS_CHANNEL_ID) as TextChannel
                                            val thread = accessChannel.startPrivateThread("Application Thread for ${applicationMember?.username}") {
                                                autoArchiveDuration = ArchiveDuration.Day
                                            }
                                            applicationThread = thread
                                            val modRole = guild.getRole(DB.getConfigSnowflake("role.moderator"))
                                            val adminRole = guild.getRole(DB.getConfigSnowflake("role.admin"))

                                            val threadMessage = thread.createMessage("${applicationMember?.mention} Let me get all the moderators in here...")
                                            threadMessage.edit {
                                                content = "Hey ${adminRole.mention}, ${modRole.mention}, get in here!"
                                            }
                                            threadMessage.edit {
                                                content = "Hey ${applicationMember?.mention}! The moderators will be here to discuss your application shortly."
                                            }
                                        }
                                    }
                                }
						    }
						}
					}
				}
			}
		}
	}

	inner class SlapArgs : Arguments() {
		val target by user {
			name = "target"
			description = "Person you want to slap"
		}

		val weapon by coalescingDefaultingString {
			name = "weapon"

			defaultValue = "large, smelly trout"
			description = "What you want to slap with"
		}
	}

	inner class SlapSlashArgs : Arguments() {
		val target by user {
			name = "target"
			description = "Person you want to slap"
		}

		// Coalesced strings are not currently supported by slash commands
		val weapon by defaultingString {
			name = "weapon"

			defaultValue = "large, smelly trout"
			description = "What you want to slap with"
		}
	}
}
