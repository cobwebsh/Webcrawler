package org.ecorous.webcrawler

import dev.kord.common.entity.Choice
import dev.kord.common.entity.optional.Optional
import kotlinx.coroutines.selects.select
import org.ecorous.webcrawler.database.DB
import org.ecorous.webcrawler.database.DB.tagFromRow
import org.ecorous.webcrawler.database.TagsTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Tags {
    fun getTagByName(name: String): Tag {
        return transaction(DB.database) {
            TagsTable.select(TagsTable.tag eq name).single().tagFromRow()
        }
    }

    fun getTags(): List<Tag> {
        val list = mutableListOf<Tag>()
        transaction(DB.database) {
            if (!TagsTable.selectAll().empty()) TagsTable.selectAll().forEach {
                list += it.tagFromRow()
            }
        }
        return list
    }

    fun addTag(name: String, content: String): Tag {
        val tag = Tag(name, content)
        if (getTags().contains(tag)) return tag
        transaction(DB.database) {
            val ncontent = content.replace("\\n", "\n")
            TagsTable.insert {
                it[this.tag] = name
                it[this.content] = ncontent
            }
        }
        return tag
    }

    fun deleteTag(name: String) {
        transaction(DB.database) {
            TagsTable.deleteWhere {
                TagsTable.tag eq name
            }
        }
    }

    fun modifyTag(name: String, content: String) {
        deleteTag(name)
        transaction(DB.database) {
            TagsTable.insert {
                it[this.tag] = name
                it[this.content] = content
            }
        }
    }
}