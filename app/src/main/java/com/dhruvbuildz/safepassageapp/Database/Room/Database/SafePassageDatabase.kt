package com.dhruvbuildz.safepassageapp.Database.Room.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Card
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Category
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Document
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Password
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Pin
import com.dhruvbuildz.safepassageapp.Database.Room.Model.User
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Utilities
import com.dhruvbuildz.safepassageapp.Database.Room.Database.DocumentDao
import com.dhruvbuildz.safepassageapp.Database.Room.Database.PinDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

@Database(entities = [Password::class, Category::class, User::class, Card::class, Utilities::class, Document::class, Pin::class], version = 3)
abstract class SafePassageDatabase : RoomDatabase() {

    abstract fun passwordDao(): PasswordDao
    abstract fun categoryDao(): CategoryDao
    abstract fun userDao(): UserDao
    abstract fun cardDao(): CardDao
    abstract fun utilitiesDao(): UtilitiesDao
    abstract fun documentDao(): DocumentDao
    abstract fun pinDao(): PinDao


    companion object {

        @Volatile
        private var instance: SafePassageDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: kotlin.synchronized(LOCK) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, SafePassageDatabase::class.java, "safePassageDatabase"
        ).fallbackToDestructiveMigration() // This will recreate the database if migration fails
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                instance?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        it.utilitiesDao().insertUtilities(
                            Utilities(
                                "1", false, null,
                                isUsePhoneLock = false,
                                isScreenshot = false,
                                isDBSync = false
                            )
                        )
                    }
                }
            }
        }).build()
    }

    // Method to clear all user-specific data when a new user logs in
    suspend fun resetForNewUser() {
        clearAllTables() // Clears all tables
        // Re-insert default utilities
        utilitiesDao().insertUtilities(
            Utilities(
                "1", false, null,
                isUsePhoneLock = false,
                isScreenshot = false,
                isDBSync = false
            )
        )
    }

}