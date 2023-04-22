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
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import com.kotlindiscord.kord.extensions.utils.hasPermission
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.Color
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ArchiveDuration
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.common.entity.optional.optional
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.thread.TextChannelThread
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import org.ecorous.webcrawler.*

@OptIn(KordPreview::class)
class TestExtension : Extension() {
	override val name = "test"

	@OptIn(KordExperimental::class)
    override suspend fun setup() {
		publicSlashCommand(::SlapSlashArgs) {
			name = "slap"
			description = "Ask the bot to slap another user"

			guild(SERVER_ID)  // Otherwise it'll take an hour to update

			action {
				// Because of the DslMarker annotation KordEx uses, we need to grab Kord explicitly
				val kord = this@TestExtension.kord

				// Don't slap ourselves on request, slap the requester!
				val realTarget = if (arguments.target.id == kord.selfId) {
					member
				} else {
					arguments.target
				}

				respond {
					content = "*slaps ${realTarget?.mention} with their ${arguments.weapon}*"
				}
			}
		}

		event<MessageCreateEvent> {

			action {


                println("in message handler. author isBot: ${event.message.author?.isBot}. expected author id: $APPLICATIONS_WEBHOOK_ID. channel id: ${event.message.channelId}. expected channel id: $APPLICATIONS_CHANNEL_ID")
				if (event.message.author == null && event.message.channelId == APPLICATIONS_CHANNEL_ID) {
					println("update #1")
                    val message = event.message
					val channel = message.channel
					val embeds = message.embeds
					if (embeds.firstOrNull() == null) {
						println("no embeds")
                        return@action
					}
                    println("update #2")
					val embed = message.embeds.first()
					if (embed.author != null) {
                        println("update #3")
						val userText = embed.author?.name!!
						if (event.getGuildOrNull() != null) {
                            println("update #4")
							val guild = event.getGuildOrNull()!!
                            println("update #5: $userText")
						    val applicationMember = guild.members.firstOrNull {
                                it.tag == userText
                            }
                            println("update #6")
                            var buttonMessage: Message? = null
                            var applicationThread: TextChannelThread? = null
                            buttonMessage = channel.createMessage {
							    embed {
                                    author {
                                        name = applicationMember?.username + "#" + applicationMember?.discriminator
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
                                            applicationMember?.addRole(VERIFIED_ROLE_ID, "Application accepted by ${memberX.username}#${memberX.discriminator} (${memberX.id})")
                                            channel.createEmbed {
                                                author {
                                                    name = memberX.username + "#" + memberX.discriminator
                                                    icon = memberX.avatar?.url
                                                }
                                                color = DISCORD_GREEN
                                                title = "User ${applicationMember?.username}#${applicationMember?.discriminator} accepted"
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
                                            applicationMember.kick("Application denied by ${memberX.username}#${memberX.discriminator} (${memberX.id})")
                                            channel.createEmbed {
                                                author {
                                                    name = "${memberX.username}#${memberX.discriminator}"
                                                    icon = memberX.avatar?.url
                                                }
                                                color = DISCORD_RED
                                                title = "User ${applicationMember.username}#${applicationMember.discriminator} denied"
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
                                            val thread = accessChannel.startPrivateThread("Application Thread for ${applicationMember?.username}#${applicationMember?.discriminator}") {
                                                autoArchiveDuration = ArchiveDuration.Day
                                            }
                                            applicationThread = thread
                                            val modRole = guild.getRole(MODERATOR_ROLE_ID)
                                            val adminRole = guild.getRole(ADMIN_ROLE_ID)

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
                            println("update #7")
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
