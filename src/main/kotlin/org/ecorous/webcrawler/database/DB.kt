package org.ecorous.webcrawler.database

import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.selects.select
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object DB {
    private lateinit var database: Database;
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
}