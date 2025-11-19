package com.dhruvbuildz.safepassageapp.UI.Model

import com.dhruvbuildz.safepassageapp.Database.Room.Model.Card
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Document
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Password

sealed class UnifiedItem {
    data class PasswordItem(val password: Password) : UnifiedItem()
    data class CardItem(val card: Card) : UnifiedItem()
    data class DocumentItem(val document: Document) : UnifiedItem()
    
    val id: Int
        get() = when (this) {
            is PasswordItem -> password.passId
            is CardItem -> card.carId
            is DocumentItem -> document.documentId
        }
    
    val title: String
        get() = when (this) {
            is PasswordItem -> password.title
            is CardItem -> card.title
            is DocumentItem -> document.title
        }
    
    val createdAt: String?
        get() = when (this) {
            is PasswordItem -> password.createdAt
            is CardItem -> null // Card doesn't have createdAt
            is DocumentItem -> document.createdAt
        }
    
    val lastUsed: String?
        get() = when (this) {
            is PasswordItem -> password.lastUsed
            is CardItem -> null // Card doesn't have lastUsed
            is DocumentItem -> document.updatedAt
        }
}
