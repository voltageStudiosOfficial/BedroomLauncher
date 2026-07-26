/*
 * Bedroom Launcher (BL)
 * Copyright (C) 2025 Voltage Studios Official <voltageStudiosOfficial@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package tech.voltagestudios.dream.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import tech.voltagestudios.dream.game.account.Account
import tech.voltagestudios.dream.game.account.AccountDao
import tech.voltagestudios.dream.game.account.auth_server.data.AuthServer
import tech.voltagestudios.dream.game.account.auth_server.data.AuthServerDao
import tech.voltagestudios.dream.game.path.GamePath
import tech.voltagestudios.dream.game.path.GamePathDao

@Database(
    entities = [Account::class, AuthServer::class, GamePath::class],
    version = 2,
    exportSchema = false //默认不支持导出
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * 启动器账号
     */
    abstract fun accountDao(): AccountDao

    /**
     * 认证服务器
     */
    abstract fun authServerDao(): AuthServerDao

    /**
     * 游戏目录
     */
    abstract fun gamePathDao(): GamePathDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE accounts ADD COLUMN expiresAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * 获取全局数据库实例
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "launcher_data.db"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
