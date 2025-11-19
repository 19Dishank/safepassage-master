package com.dhruvbuildz.safepassageapp.Database.Room.Repository

import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.User

class UserRepository(private val database: SafePassageDatabase) {

    suspend fun insertUser(user: User) = database.userDao().insetUser(user)
    suspend fun updateUser(user: User) = database.userDao().updateUser(user)
    suspend fun deleteUser(user: User) = database.userDao().deleteUser(user)

    fun getAnyUser() = database.userDao().getAnyUser()

    fun getUser() = database.userDao().getAnyUser()

    fun getUserById(userId: String) = database.userDao().getUserById(userId)

}