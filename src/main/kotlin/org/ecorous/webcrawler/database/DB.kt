package org.ecorous.webcrawler.database

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
    fun setConfig(name: String, value: Long) {
        transaction (database) {
            Config.deleteWhere {
                settingName eq name
            }
            Config.insert {
                it[settingName] = name
                it[longValue] = value
            }
        }
    }
    fun getConfig(name: String, default: String): String {
        val q = Config.select { Config.settingName eq name }
        if(!q.any()) {
            return default
        }
        return q.first()[Config.stringValue]
    }
    fun getConfig(name: String, default: Long): Long {
        val q = Config.select { Config.settingName eq name }
        if(!q.any()) {
            return default
        }
        return q.first()[Config.longValue]
    }
}