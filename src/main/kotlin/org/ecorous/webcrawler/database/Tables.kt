package org.ecorous.webcrawler.database

import org.jetbrains.exposed.sql.Table

object Config : Table() {
    val settingName = varchar("settingName", 100)
    val stringValue = varchar("stringValue", 100)
}

object ModerationCase : Table() {
    val id = uuid("id")
    val caseType = varchar("caseType", 15)
    val userid = long("userId")
    val moderatorId = long("moderatorId")
}