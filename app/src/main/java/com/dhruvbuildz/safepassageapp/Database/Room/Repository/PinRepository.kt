package com.dhruvbuildz.safepassageapp.Database.Room.Repository

import com.dhruvbuildz.safepassageapp.Database.Room.Database.PinDao
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Pin

class PinRepository(private val pinDao: PinDao) {

    suspend fun insertPin(pin: Pin) {
        pinDao.insertPin(pin)
    }

    suspend fun updatePin(pin: Pin) {
        pinDao.updatePin(pin)
    }

    suspend fun deletePin(pin: Pin) {
        pinDao.deletePin(pin)
    }

    fun getPinByUserId(userId: String) = pinDao.getPinByUserId(userId)

    fun hasPin(userId: String) = pinDao.hasPin(userId)
}
