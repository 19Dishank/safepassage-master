package com.dhruvbuildz.safepassageapp.Database.Room.Repository

import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Utilities

class UtilitiesRepository(private val database: SafePassageDatabase) {

    suspend fun insertUtilities(utilities: Utilities) = database.utilitiesDao().insertUtilities(utilities)
    suspend fun updateUtilities(utilities: Utilities) = database.utilitiesDao().updateUtilities(utilities)
    suspend fun deleteUtilities(utilities: Utilities) = database.utilitiesDao().deleteUtilities(utilities)

    fun getUtilities() = database.utilitiesDao().getUtilities()

}