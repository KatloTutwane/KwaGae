package com.example.kwagae.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kwagae.data.dao.ListingDao
import com.example.kwagae.data.dao.UserDao
import com.example.kwagae.data.models.Listing
import com.example.kwagae.data.models.User
import com.example.kwagae.data.converters.Converters

@Database(
    entities = [User::class, Listing::class],
    version = 3,                        // bumped: added syncedAt & pendingSync fields
    exportSchema = false
)
@TypeConverters(Converters::class)      // for List<String> amenities, timestamps etc.
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun listingDao(): ListingDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kwagae_database"
                )
                    .fallbackToDestructiveMigration()   // replace with proper migrations in prod
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}