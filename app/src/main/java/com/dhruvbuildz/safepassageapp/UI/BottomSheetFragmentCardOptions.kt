package com.dhruvbuildz.safepassageapp.UI

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Card
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CardViewModel
import com.dhruvbuildz.safepassageapp.Fetures.CryptographyManager
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.databinding.BottomSheetCardOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class BottomSheetFragmentCardOptions(private val card: Card, private val viewModel: CardViewModel) :
    BottomSheetDialogFragment() {


    private val binding by lazy {
        BottomSheetCardOptionsBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val cryptographyManager = CryptographyManager(card.userId)

        val decryptedCardNumber = card.cardNumber?.takeIf { it.isNotEmpty() }?.let { cryptographyManager.decryptData(it) }
        val decryptedCvv = card.cvv?.takeIf { it.isNotEmpty() }?.let { cryptographyManager.decryptData(it) }
        val decryptedPin = card.pin?.takeIf { it.isNotEmpty() }?.let { cryptographyManager.decryptData(it) }

        binding.titleText.text = card.title

        binding.copyCardNumber.setOnClickListener {
            copyToClipboard(decryptedCardNumber ?: "--", "Card Number")
        }

        binding.copyCvv.setOnClickListener {
            copyToClipboard(decryptedCvv ?: "--", "Cvv")
        }

        binding.copyPin.setOnClickListener {
            copyToClipboard(decryptedPin ?: "--", "Pin")
        }


        binding.delete.setOnClickListener {
            showDeleteDialog()
        }

        binding.edit.setOnClickListener {
            openEditActivity()
        }


        return binding.root
    }

    private fun copyToClipboard(text: String, name: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Email", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "$name copied to clipboard", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        val window = requireDialog().window
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            it.setNavigationBarColor(resources.getColor(R.color.bg))
            it.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    override fun onStop() {
        super.onStop()
        val window = requireDialog().window
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, true)
            it.setNavigationBarColor(resources.getColor(R.color.main))
            it.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun showDeleteDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(" Delete ${card.title}")
            .setMessage("Are your sure you want to delete ${card.title} ?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteCard(card)
                Toast.makeText(requireContext(), "${card.title} deleted", Toast.LENGTH_SHORT).show()
                dismiss()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openEditActivity() {
        val intent = Intent(requireContext(), EditCardActivity::class.java).apply {
            putExtra("CARD_ID", card.carId)
        }
        startActivity(intent)
    }

    private fun showTextLogo(
        url: String?,
        title: String,
        imageView: ImageView,
        textView: TextView
    ) {

        imageView.visibility = View.GONE
        textView.visibility = View.VISIBLE

        val textToShow = (url?.take(2) ?: title.take(2)).uppercase()
        textView.text = textToShow

    }

    private fun decryptText(userId: String, keyData: String?): String {
        val cryptographyManager = CryptographyManager(userId)
        return if (keyData.isNullOrEmpty()) {
            ""
        } else {
            cryptographyManager.decryptData(keyData)
        }
    }

}