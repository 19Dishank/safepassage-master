package com.dhruvbuildz.safepassageapp.UI.Adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.DataSource
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Password
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PasswordViewModel
import com.dhruvbuildz.safepassageapp.Fetures.CryptographyManager
import com.dhruvbuildz.safepassageapp.UI.BottomSheet.PasswordOptionBottomSheet
import com.dhruvbuildz.safepassageapp.UI.ViewLoginActivity
import com.dhruvbuildz.safepassageapp.databinding.PasswordLayoutBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class PasswordAdapter(var passwordList: List<Password>, private val viewModel: PasswordViewModel) :
    RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder>() {

    class PasswordViewHolder(val view: PasswordLayoutBinding) : RecyclerView.ViewHolder(view.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        return PasswordViewHolder(
            PasswordLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return passwordList.size
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        val currentPassword = passwordList[position]
        val cryptographyManager = CryptographyManager(currentPassword.userId)

        holder.view.titleText.text = currentPassword.title

        val decryptedEmail = currentPassword.email?.takeIf { it.isNotEmpty() }?.let { cryptographyManager.decryptData(it) }
        val decryptedPhone = currentPassword.phone?.takeIf { it.isNotEmpty() }?.let { cryptographyManager.decryptData(it) }
        val decryptedUserName = currentPassword.userName?.takeIf { it.isNotEmpty() }?.let { cryptographyManager.decryptData(it) }


        holder.view.emailText.text = decryptedEmail ?: decryptedPhone ?: decryptedUserName ?: " "

        val url = currentPassword.url
        val logoUrl = if (!url.isNullOrEmpty()) {
            "https://logo.clearbit.com/$url"
        } else {
            null
        }


        if (logoUrl != null) {
            Glide.with(holder.itemView.context)
                .load(logoUrl)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        showTextLogo(url, currentPassword.title, holder.view.logoImage, holder.view.logoText)
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        holder.view.logoText.visibility = View.GONE
                        holder.view.logoImage.visibility = View.VISIBLE
                        return false
                    }
                })
                .into(holder.view.logoImage)
        } else {
            showTextLogo(url, currentPassword.title, holder.view.logoImage, holder.view.logoText)
        }


        holder.view.optionButton.setOnClickListener {
            val optionBottomSheet = PasswordOptionBottomSheet(currentPassword, viewModel)
            optionBottomSheet.show(
                (holder.itemView.context as AppCompatActivity).supportFragmentManager,
                optionBottomSheet.tag
            )
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ViewLoginActivity::class.java).apply {
                putExtra("PASSWORD_ID", currentPassword.passId)
            }
            holder.itemView.context.startActivity(intent)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(items: List<Password>) {
        passwordList = items
        notifyDataSetChanged()
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

}