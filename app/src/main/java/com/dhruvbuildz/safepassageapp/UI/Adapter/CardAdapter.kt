package com.dhruvbuildz.safepassageapp.UI.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Card
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CardViewModel
import com.dhruvbuildz.safepassageapp.Fetures.CryptographyManager
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.UI.BottomSheet.CardOptionsBottomSheet
import com.dhruvbuildz.safepassageapp.databinding.CardLayoutBinding

class CardAdapter(var cards: List<Card>, private val cardViewModel: CardViewModel) :
    RecyclerView.Adapter<CardAdapter.CardViewHolder>() {


    class CardViewHolder(val view: CardLayoutBinding) : RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder(
            CardLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val currentCard = cards[position]

        val cryptographyManager = CryptographyManager(currentCard.userId)

        val decryptedCardNumber = currentCard.cardNumber?.let { cryptographyManager.decryptData(it) }
        val decryptedCvv = currentCard.cvv?.let { cryptographyManager.decryptData(it) }
        val decryptedExpDate = currentCard.expirationDate?.let { cryptographyManager.decryptData(it) }

        // Set the card details
        holder.view.cardTitleText.text = currentCard.title
        holder.view.cardNumberText.text = "•••• •••• •••• ${decryptedCardNumber?.takeLast(4)}"
        holder.view.cvvText.text = "••${decryptedCvv?.takeLast(1)}"
        holder.view.cardHolderNameText.text = currentCard.cardHolderName
        holder.view.expDateText.text = decryptedExpDate

        var isHidden = true


        holder.view.hideButton.setOnClickListener {
            isHidden = !isHidden

            if (isHidden) {
                holder.view.cardNumberText.text =
                    "•••• •••• •••• ${decryptedCardNumber?.takeLast(4)}"
                holder.view.cvvText.text = "••${decryptedCvv?.takeLast(1)}"
                holder.view.hideImg.setImageResource(R.drawable.ic_show)
            } else {
                holder.view.cardNumberText.text = decryptedCardNumber ?: ""
                holder.view.cvvText.text = decryptedCvv ?: ""
                holder.view.hideImg.setImageResource(R.drawable.ic_hide)

            }
        }
        holder.view.optionButton.setOnClickListener {
            val optionBottomSheet = CardOptionsBottomSheet(currentCard, cardViewModel)
            optionBottomSheet.show(
                (holder.itemView.context as AppCompatActivity).supportFragmentManager,
                optionBottomSheet.tag
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(items: List<Card>) {
        cards = items
        notifyDataSetChanged()
    }

}