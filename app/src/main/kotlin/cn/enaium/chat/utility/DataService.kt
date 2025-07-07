/*
 * Copyright (c) 2025 Enaium
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cn.enaium.chat.utility

import cn.enaium.chat.model.entity.Contact
import cn.enaium.chat.model.entity.Message
import cn.enaium.chat.model.entity.contactId
import cn.enaium.chat.model.entity.dto.ContactInput
import cn.enaium.chat.model.entity.dto.ContactView
import cn.enaium.chat.model.entity.dto.MessageInput
import cn.enaium.chat.model.entity.dto.MessageView
import cn.enaium.chat.model.entity.id
import cn.enaium.chat.model.entity.messages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.count
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.gt
import java.util.UUID

/**
 * @author Enaium
 */
class DataService(val sql: KSqlClient) {
    suspend fun saveContact(input: ContactInput) = withContext(Dispatchers.IO) {
        sql.save(input)
    }

    suspend fun getContact(id: UUID): ContactView? = withContext(Dispatchers.IO) {
        return@withContext sql.findById(ContactView::class, id)
    }

    suspend fun getContacts(): List<ContactView> = withContext(Dispatchers.IO) {
        return@withContext sql.findAll(ContactView::class)
    }

    suspend fun deleteContact(id: UUID) = withContext(Dispatchers.IO) {
        sql.deleteById(Contact::class, id)
    }

    suspend fun saveMessage(input: MessageInput) = withContext(Dispatchers.IO) {
        sql.save(input) {
            setMode(SaveMode.NON_IDEMPOTENT_UPSERT)
        }
    }

    suspend fun getMessages(contact: UUID): List<MessageView> =
        withContext(Dispatchers.IO) {
            return@withContext sql.createQuery(Message::class) {
                where(table.contactId eq contact)
                select(table.fetch(MessageView::class))
            }.execute()
        }

    suspend fun clearMessages(contact: UUID) = withContext(Dispatchers.IO) {
        sql.createDelete(Message::class) {
            where(table.contactId eq contact)
        }.execute()
    }

    suspend fun deleteMessage(id: UUID) = withContext(Dispatchers.IO) {
        sql.deleteById(Message::class, id)
    }

    suspend fun getChats(): List<ContactView> = withContext(Dispatchers.IO) {
        return@withContext sql.createQuery(Contact::class) {
            groupBy(table.id)
            having(count(table.asTableEx().messages.contactId) gt 0)
            select(table.fetch(ContactView::class))
        }.execute()
    }
}