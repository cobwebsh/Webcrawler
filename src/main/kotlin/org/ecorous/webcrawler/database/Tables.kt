package org.ecorous.webcrawler.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object Config : Table() {
    val settingName = varchar("settingName", 100)
    val stringValue = varchar("stringValue", 100)
}

object ModerationCase : IntIdTable() {
    val caseType = varchar("caseType", 15)
    val userId = long("userId")
    val moderatorId = long("moderatorId")
    val content = varchar("content", 512)
}