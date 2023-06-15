package org.ecorous.webcrawler.extensions

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.*
import dev.kord.common.entity.Choice
import dev.kord.common.entity.optional.Optional
import dev.kord.core.behavior.interaction.suggest
import dev.kord.rest.builder.message.create.*
import org.ecorous.webcrawler.Tags

class TagExtension : Extension() {
    override val name: String = "tags"
    override suspend fun setup() {
        publicSlashCommand {
            name = "tag"
            description = "Manage Tags"
            publicSubCommand(::GetTagArgs) {
                name = "get"
                description = "Get a tag"
                action {
                    val tag = Tags.getTagByName(arguments.tag)
                    respond {
                        embed {
                            title = tag.name
                            description = tag.content
                            color = DISCORD_BLURPLE
                        }
                    }
                }
            }

            publicSubCommand(::CreateTagArgs) {
                name = "create"
                description = "Create a tag"
                action {
                    val tag = Tags.addTag(arguments.name, arguments.content)
                    respond {
                        content = "Created tag ${tag.name}"
                    }
                }
            }

            publicSubCommand(::DeleteTagArgs) {
                name = "delete"
                description = "Delete a tag"
                action {
                    Tags.deleteTag(arguments.name)
                    respond {
                        content = "Deleted ${arguments.name}"
                    }
                }
            }

            publicSubCommand(::ModifyTagArgs) {
                name = "modify"
                description = "Modify a tag"
                action {
                    Tags.modifyTag(arguments.name, arguments.content)
                    respond {
                        content = "Modified the content of ${arguments.name}"
                    }
                }
            }
        }

    }
    inner class GetTagArgs : Arguments() {
        val tag by stringChoice {
            name = "tag"
            description = "the tag to get"
            autoComplete {
                val x = mutableListOf<Choice<String>>()
                Tags.getTags().forEach {
                    x += Choice.StringChoice(it.name, Optional(), it.name)
                }
                suggest(x)
            }
        }
    }
    inner class CreateTagArgs : Arguments() {
        val name by string {
            name = "name"
            description = "The name of the new tag"
        }
        val content by string {
            name = "content"
            description = "Tne content of the tag"
        }
    }
    inner class DeleteTagArgs : Arguments() {
        val name by stringChoice {
            name = "name"
            description = "The tag to delete"
            autoComplete {
                val x = mutableListOf<Choice<String>>()
                Tags.getTags().forEach {
                    x += Choice.StringChoice(it.name, Optional(), it.name)
                }
                suggest(x)
            }
        }
    }
    inner class ModifyTagArgs : Arguments() {
        val name by stringChoice {
            name = "name"
            description = "The tag to modify"
            autoComplete {
                val x = mutableListOf<Choice<String>>()
                Tags.getTags().forEach {
                    x += Choice.StringChoice(it.name, Optional(), it.name)
                }
                suggest(x)
            }
        }
        val content by string {
            name = "content"
            description = "The new content of the tag"
        }
    }
}