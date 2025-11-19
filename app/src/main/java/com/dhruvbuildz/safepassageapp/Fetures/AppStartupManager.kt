package com.dhruvbuildz.safepassageapp.Fetures

import android.content.Context
import androidx.lifecycle.LiveData
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.User
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Utilities
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UtilitiesRepository

object AppStartupManager {
    
    enum class StartupAction {
        REGISTER,      // No user exists, show register screen
        SET_PIN,       // User exists but no PIN set, show set PIN screen
        UNLOCK_PIN     // User exists and PIN is set, show unlock screen
    }
    
    fun determineStartupAction(context: Context): LiveData<StartupAction> {
        val database = SafePassageDatabase(context)
        val userRepository = UserRepository(database)
        val utilitiesRepository = UtilitiesRepository(database)
        
        return object : LiveData<StartupAction>() {
            override fun onActive() {
                userRepository.getUser().observeForever { user ->
                    if (user != null) {
                        // User exists, check if PIN is set
                        utilitiesRepository.getUtilities().observeForever { utilities ->
                            if (utilities?.lockCode != null && utilities.lockCode!!.isNotEmpty()) {
                                value = StartupAction.UNLOCK_PIN
                            } else {
                                value = StartupAction.SET_PIN
                            }
                        }
                    } else {
                        // No user exists
                        value = StartupAction.REGISTER
                    }
                }
            }
        }
    }
    
    fun isFirstTimeUser(context: Context): Boolean {
        val database = SafePassageDatabase(context)
        val userRepository = UserRepository(database)
        val user = userRepository.getUser().value
        return user == null
    }
    
    fun isPinSet(context: Context): Boolean {
        val database = SafePassageDatabase(context)
        val utilitiesRepository = UtilitiesRepository(database)
        val utilities = utilitiesRepository.getUtilities().value
        return utilities?.lockCode != null && utilities.lockCode!!.isNotEmpty()
    }
}
