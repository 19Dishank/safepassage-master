package com.dhruvbuildz.safepassageapp.UI.BottomSheet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Password
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PasswordViewModel
import com.dhruvbuildz.safepassageapp.Fetures.CryptographyManager
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.UI.EditLoginActivity
import com.dhruvbuildz.safepassageapp.databinding.BottomSheetPasswordOptionBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class PasswordOptionBottomSheet(private val password: Password, private val viewModel: PasswordViewModel) :
    BottomSheetDialogFragment() {

    private val binding by lazy {
        BottomSheetPasswordOptionBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val cryptographyManager = CryptographyManager(password.userId)

        val decryptedEmail = password.email?.takeIf { it.isNotEmpty() }?.let { cryptographyManager.decryptData(it) }
        val decryptedPhone = password.phone?.takeIf { it.isNotEmpty() }?.let { cryptographyManager.decryptData(it) }
        val decryptedUserName = password.userName?.takeIf { it.isNotEmpty() }?.let { cryptographyManager.decryptData(it) }
        val decryptedPassword = password.password?.takeIf { it.isNotEmpty() }?.let { cryptographyManager.decryptData(it) }

        binding.titleText.text = password.title
        binding.emailText.text = decryptedEmail ?: decryptedPhone ?: decryptedUserName ?: " "
        val logoUrl = "https://logo.clearbit.com/${password.url}"
        Glide.with(this)
            .load(logoUrl)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    showTextLogo(password.url, password.title, binding.logoImage, binding.logoText)
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    binding.logoText.visibility = View.GONE
                    return false
                }
            })
            .into(binding.logoImage)

        binding.copyEmail.setOnClickListener {
            copyToClipboard(decryptedEmail ?: "--", "Email")
        }

        binding.copyUsername.setOnClickListener {
            copyToClipboard(decryptedUserName ?: "--", "Username")
        }

        binding.copyPhoneNumber.setOnClickListener {
            copyToClipboard(decryptedPhone ?: "--", "Phone Number")
        }
        binding.copyPassword.setOnClickListener {
            copyToClipboard(decryptedPassword ?: "--", "Password")
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

    private fun showDeleteDialog(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(" Delete ${password.title}")
            .setMessage("Are your sure you want to delete ${password.title} ?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deletePassword(password)
                Toast.makeText(requireContext(), "${password.title} deleted", Toast.LENGTH_SHORT).show()
                dismiss()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    private fun openEditActivity() {
        val intent = Intent(requireContext(), EditLoginActivity::class.java).apply {
            putExtra("PASSWORD_ID", password.passId)
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