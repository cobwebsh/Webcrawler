package org.ecorous.webcrawler

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.DISCORD_YELLOW
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import org.ecorous.webcrawler.database.DB.caseFromRow
import org.ecorous.webcrawler.database.DB.database
import org.ecorous.webcrawler.database.ModerationCase
import org.ecorous.webcrawler.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Cases {
    fun getCase(number: Number): Case {
        return transaction(database) {
            return@transaction ModerationCase.select(ModerationCase.id eq number.toInt()).single().caseFromRow()
        }
    }
    fun getCase(number: String) {
        var new = number
        if (number.startsWith("#")) new = number.split('#')[1]
        getCase(new.toInt())
    }

    fun getLatestCase(): Case? {
        val cases = transaction(database) {
            ModerationCase.selectAll()
        }
        var lastCase: Int = 0
        transaction(database) { if (!cases.empty()) cases.forEach {
            val case = it.caseFromRow()
            if (lastCase < case.id) {
                lastCase = case.id
            }
        }}
        return transaction(database) { if (cases.empty()) null else
            ModerationCase.select(ModerationCase.id eq lastCase).single().caseFromRow()
        }
    }
    fun getLatestCaseNum(): Int {
        return if (getLatestCase() == null) 0 else getLatestCase()!!.id
    }

    fun getNextCaseNum(): Int {
        return getLatestCaseNum() + 1
    }

    fun Case.toColor(): Color {
        return when(type) {
            CaseType.KICK -> DISCORD_RED
            CaseType.BAN -> DISCORD_RED
            CaseType.MUTE -> DISCORD_YELLOW
            CaseType.NOTE -> DISCORD_BLURPLE
        }
    }

    fun reportCase(type: CaseType, userId: Snowflake, moderatorId: Snowflake, content: String): Case {
        val caseNum = getNextCaseNum()
        if (content.length > 512) {
            throw IllegalStateException("content too long, must not be over 512 chars (${content.length})")
        }
        transaction(database) {ModerationCase.insert {
            it[this.id] = caseNum
            it[this.caseType] = type.name
            it[this.userId] = userId.value.toLong()
            it[this.moderatorId] = moderatorId.value.toLong()
            it[this.content] = content
        }}
        return Case(
            caseNum,
            type,
            userId,
            moderatorId,
            content
        )


    }
}