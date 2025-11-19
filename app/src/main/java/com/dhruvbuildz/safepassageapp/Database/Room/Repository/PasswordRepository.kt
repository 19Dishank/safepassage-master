package com.dhruvbuildz.safepassageapp.Database.Room.Repository

import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Password

class PasswordRepository(private val database: SafePassageDatabase) {

    suspend fun insertPassword(password: Password) = database.passwordDao().insertPassword(password)
    suspend fun updatePassword(password: Password) = database.passwordDao().updatePassword(password)
    suspend fun deletePassword(password: Password) = database.passwordDao().deletePassword(password)

    fun getAllPasswords(userId: String) = database.passwordDao().getAllPasswords(userId)

    fun getPasswordById(id: Int, userId: String) = database.passwordDao().getPasswordById(id, userId)

    fun getPasswordByCategory(catId: Int, userId: String) = database.passwordDao().getPasswordByCategory(catId, userId)

    fun searchPassword(searchText: String, userId: String) = database.passwordDao().searchPassword(searchText, userId)

    fun getPasswordsSortedByTitle(userId: String) = database.passwordDao().getPasswordsSortedByTitle(userId)
    fun getPasswordsSortedByTitleDesc(userId: String) = database.passwordDao().getPasswordsSortedByTitleDesc(userId)
    fun getPasswordsSortedByNewest(userId: String) = database.passwordDao().getPasswordsSortedByNewest(userId)
    fun getPasswordsSortedByOldest(userId: String) = database.passwordDao().getPasswordsSortedByOldest(userId)
    fun getPasswordsSortedByRecentlyUsed(userId: String) = database.passwordDao().getPasswordsSortedByRecentlyUsed(userId)

}