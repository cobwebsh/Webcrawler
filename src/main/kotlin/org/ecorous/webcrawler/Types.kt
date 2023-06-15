package org.ecorous.webcrawler

import dev.kord.common.entity.Snowflake

data class Case(
    val id: Int,
    val type: CaseType,
    val userId: Snowflake,
    val moderatorId: Snowflake,
    val content: String
)

data class Tag(
    val name: String,
    val content: String
)

enum class CaseType {
    BAN,
    KICK,
    MUTE,
    NOTE,
}

enum class MessageLogType {
    EDIT,
    DELETE
}