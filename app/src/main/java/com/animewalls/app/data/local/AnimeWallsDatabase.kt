package com.animewalls.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.animewalls.app.data.local.dao.DownloadDao
import com.animewalls.app.data.local.dao.FavoriteDao
import com.animewalls.app.data.local.dao.RecentDao
import com.animewalls.app.data.local.entity.DownloadEntity
import com.animewalls.app.data.local.entity.FavoriteEntity
import com.animewalls.app.data.local.entity.RecentEntity

@Database(
    entities = [FavoriteEntity::class, DownloadEntity::class, RecentEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AnimeWallsDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadDao(): DownloadDao
    abstract fun recentDao(): RecentDao

    companion object {
        @Volatile private var instance: AnimeWallsDatabase? = null

        fun get(context: Context): AnimeWallsDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AnimeWallsDatabase::class.java,
                    "animewalls.db",
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
