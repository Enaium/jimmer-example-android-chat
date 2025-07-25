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

import android.content.Context
import cn.enaium.chat.interceptor.BaseEntityDraftInterceptor
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.ConnectionManager
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy
import org.sqlite.javax.SQLiteConnectionPoolDataSource

/**
 * @author Enaium
 */
fun sql(
    context: Context,
    baseEntityDraftInterceptor: BaseEntityDraftInterceptor,
): KSqlClient {
    val databaseDir = context.getExternalFilesDir("databases")
    return newKSqlClient {
        val dataSource = SQLiteConnectionPoolDataSource().apply {
            url = "jdbc:sqlite://${databaseDir}/chat.db"
        }
        dataSource.connection.use {
            it.createStatement().use { statement ->
                statement.execute(
                    """
                    create table if not exists contact(
                        id uuid primary key,
                        address text unique not null,
                        remark text default '' not null,
                        created_time timestamp not null,
                        modified_time timestamp not null
                    );
                """.trimIndent()
                )

                statement.execute(
                    """
                    create table if not exists message(
                        id uuid primary key,
                        contact_id uuid not null references contact(id),
                        me bool not null,
                        content text not null,
                        created_time timestamp not null,
                        modified_time timestamp not null
                    );
                """.trimIndent()
                )
            }
        }
        setConnectionManager(ConnectionManager.simpleConnectionManager(dataSource))
        addDraftInterceptor(baseEntityDraftInterceptor)
        setDatabaseNamingStrategy(DefaultDatabaseNamingStrategy.LOWER_CASE)
        setDialect(SQLiteDialect())
    }
}