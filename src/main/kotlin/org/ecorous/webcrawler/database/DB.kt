package org.ecorous.webcrawler.database

import dev.kord.common.entity.Snowflake
import org.ecorous.webcrawler.Case
import org.ecorous.webcrawler.Utils.toCase
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object DB {
    lateinit var database: Database;
    fun setup() {
        database = Database.connect("jdbc:sqlite:webcrawler.db")

        transaction (database) {
            SchemaUtils.create(Config, ModerationCase)
        }
    }
    fun setConfig(name: String, value: String) {
        transaction (database) {
            Config.deleteWhere {
                settingName eq name
            }
            Config.insert {
                it[settingName] = name
                it[stringValue] = value
            }
        }
    }
    fun getConfigString(name: String): String {
        val q = Config.select { Config.settingName eq name }
        if(!q.any()) {
            return ""
        }
        return q.first()[Config.stringValue]
    }
    fun getConfigLong(name: String): Long {
        return transaction {
            val q = Config.select { Config.settingName eq name }
            if(!q.any()) {
                return@transaction 0
            }
            return@transaction q.first()[Config.stringValue].toLong()
        }
    }

    fun getConfigSnowflake(name: String): Snowflake {
        return Snowflake(getConfigLong(name))
    }

    fun ResultRow.caseFromRow(): Case {
        return Case(
            id = this[ModerationCase.id].value,
            type = this[ModerationCase.caseType].toCase(),
            userId = Snowflake(this[ModerationCase.userId]),
            moderatorId = Snowflake(this[ModerationCase.moderatorId]),
            content = this[ModerationCase.content]
        )
    }
}