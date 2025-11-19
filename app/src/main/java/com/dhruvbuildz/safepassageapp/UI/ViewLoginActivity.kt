package com.dhruvbuildz.safepassageapp.UI

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.CategoryRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PasswordRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CategoryViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PasswordViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.CategoryVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.PasswordVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UserVMFactory
import com.dhruvbuildz.safepassageapp.Utils.SessionManager
import com.dhruvbuildz.safepassageapp.Fetures.CryptographyManager
import com.dhruvbuildz.safepassageapp.Fetures.PasswordManager
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.databinding.ActivityViewLoginBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Locale

class ViewLoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityViewLoginBinding.inflate(layoutInflater)
    }
    private lateinit var userViewModel: UserViewModel
    private lateinit var passwordViewModel: PasswordViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private var passId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setUpViewModel()

        val passwordId = intent.getIntExtra("PASSWORD_ID", -1)

        passId = passwordId

        if (passwordId != -1) {
            setData(passwordId)
        }


        setUpViews()

    }

    private fun setUpViews() {

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.editButton.setOnClickListener {
            val intent = Intent(this, EditLoginActivity::class.java).apply {
                putExtra("PASSWORD_ID", passId)
            }
            this.startActivity(intent)
        }

        binding.passwordTextBox.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { copyTextAndShowSnackbar(this, "Passsword") }
        }
        binding.passwordLayout.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { copyTextAndShowSnackbar(binding.passwordTextBox, "Passsword") }
        }

        binding.emailLayout.setOnClickListener { copyTextAndShowSnackbar(binding.emailTextBox, "Email") }

        binding.userNameLayout.setOnClickListener {
            copyTextAndShowSnackbar(
                binding.usernameTextBox,
                "Username"
            )
        }
        binding.phoneLayout.setOnClickListener {
            copyTextAndShowSnackbar(
                binding.phoneTextBox,
                "Phone Number"
            )
        }


        binding.passwordTextBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val password = p0.toString()
                binding.passwordStrength.visibility = View.VISIBLE
                binding.dot.visibility = View.VISIBLE
                binding.passwordInfo.visibility = View.VISIBLE
                updatePasswordStrength(password)
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        binding.urlTextBox.setOnClickListener {
            val urlText = binding.urlTextBox.text.toString().trim()

            if (urlText.isNotEmpty()) {

                val urlToOpen = if (urlText.startsWith("http")) urlText else "https://$urlText"

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen))
                startActivity(intent)
            } else {
                Toast.makeText(this, "URL is empty", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun setData(passwordId: Int) {
        val currentUserId = SessionManager(this).getLastUserId()
        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        passwordViewModel.getPasswordById(passwordId, currentUserId).observe(this) { password ->
            password?.let {
                        passId = it.passId
                        binding.titleTextBox.setText(it.title)
                        binding.emailTextBox.setText(decryptText(it.userId, it.email))
                        binding.usernameTextBox.setText(decryptText(it.userId, it.userName))
                        binding.passwordTextBox.setText(decryptText(it.userId, it.password))
                        binding.phoneTextBox.setText(decryptText(it.userId, it.phone))
                        binding.urlTextBox.setText(it.url)
                        binding.noteTextBox.setText(decryptText(it.userId, it.note))

                        val url = it.url
                        val logoUrl = if (!url.isNullOrEmpty()) {
                            "https://logo.clearbit.com/$url"
                        } else {
                            null
                        }
                        if (logoUrl != null) {
                            Glide.with(baseContext)
                                .load(logoUrl)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        showTextLogo(
                                            url,
                                            password.title,
                                            binding.logoImage,
                                            binding.logoText
                                        )
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {

                                        binding.logoText.visibility = View.GONE
                                        binding.logoImage.visibility = View.VISIBLE
                                        return false
                                    }
                                })
                                .into(binding.logoImage)
                        } else {
                            showTextLogo(
                                url,
                                password.title,
                                binding.logoImage,
                                binding.logoText
                            )
                        }

                        binding.lastAutofillDate.text = formatTimestamp(it.lastUsed)

                        binding.modifiedDate.text = formatTimestamp(it.updatedAt)

                        binding.createdDate.text = formatTimestamp(it.createdAt)

                        if (it.email.isNullOrEmpty()) {
                            binding.emailLayout.visibility = View.GONE
                        }

                        if (it.phone.isNullOrEmpty()) {
                            binding.phoneLayout.visibility = View.GONE
                        }

                        if (it.userName.isNullOrEmpty()) {
                            binding.userNameLayout.visibility = View.GONE
                        }


                    }
        }
    }

    private fun decryptText(userId: String, keyData: String?): String {
        val cryptographyManager = CryptographyManager(userId)
        return if (keyData.isNullOrEmpty()) {
            ""
        } else {
            cryptographyManager.decryptData(keyData)
        }
    }

    private fun encryptText(userId: String, keyData: String?): String {
        val cryptographyManager = CryptographyManager(userId)
        return keyData?.let {
            if (it.isNotBlank()) cryptographyManager.encryptData(it) else ""
        } ?: ""
    }


    private fun setUpViewModel() {
        val userRepository = UserRepository(SafePassageDatabase(this))
        val viewModelProvider = UserVMFactory(application, userRepository)
        userViewModel = ViewModelProvider(this, viewModelProvider)[UserViewModel::class.java]

        val passwordRepository = PasswordRepository(SafePassageDatabase(this))
        val passViewModelProvider = PasswordVMFactory(application, passwordRepository)
        passwordViewModel =
            ViewModelProvider(this, passViewModelProvider)[PasswordViewModel::class.java]

        val catRepository = CategoryRepository(SafePassageDatabase(this))
        val catViewModelProvider = CategoryVMFactory(application, catRepository)
        categoryViewModel =
            ViewModelProvider(this, catViewModelProvider)[CategoryViewModel::class.java]
    }


    private fun updatePasswordStrength(password: String) {
        val strength = PasswordManager().getPasswordStrength(password)
        when (strength) {
            getString(R.string.strong) -> {
                binding.apply {
                    passwordStrength.text = getString(R.string.strong)
                    passwordStrength.setTextColor(
                        ContextCompat.getColor(
                            this@ViewLoginActivity, R.color.green
                        )
                    )
                    dot.setTextColor(
                        ContextCompat.getColor(
                            this@ViewLoginActivity, R.color.green
                        )
                    )
                    passwordText.setTextColor(
                        ContextCompat.getColor(
                            this@ViewLoginActivity, R.color.green
                        )
                    )
                    passwordIcon.setColorFilter(
                        ContextCompat.getColor(
                            this@ViewLoginActivity, R.color.green
                        )
                    )
                }
            }

            getString(R.string.weak) -> {
                binding.apply {
                    passwordStrength.text = getString(R.string.weak)
                    passwordStrength.setTextColor(
                        ContextCompat.getColor(
                            this@ViewLoginActivity, R.color.yellow
                        )
                    )
                    dot.setTextColor(
                        ContextCompat.getColor(
                            this@ViewLoginActivity, R.color.yellow
                        )
                    )
                    passwordText.setTextColor(
                        ContextCompat.getColor(
                            this@ViewLoginActivity, R.color.yellow
                        )
                    )
                    passwordIcon.setColorFilter(
                        ContextCompat.getColor(
                            this@ViewLoginActivity, R.color.yellow
                        )
                    )
                }
            }

            getString(R.string.vulnerable) -> {
                binding.apply {
                    passwordStrength.text = getString(R.string.vulnerable)
                    passwordStrength.setTextColor(
                        ContextCompat.getColor(
                            this@ViewLoginActivity, R.color.red
                        )
                    )
                    dot.setTextColor(
                        ContextCompat.getColor(
                            this@ViewLoginActivity, R.color.red
                        )
                    )
                    passwordText.setTextColor(
                        ContextCompat.getColor(
                            this@ViewLoginActivity, R.color.red
                        )
                    )
                    passwordIcon.setColorFilter(
                        ContextCompat.getColor(
                            this@ViewLoginActivity, R.color.red
                        )
                    )
                }
            }
        }
    }

    @SuppressLint("ShowToast")
    private fun copyTextAndShowSnackbar(textView: TextView, boxName: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = textView.text.toString()
        if (text.isNotEmpty()) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Copied Text", text))
            Snackbar.make(
                binding.root,
                "${textView.text} copied to clipboard",
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            Snackbar.make(binding.root, "$boxName is empty", Snackbar.LENGTH_SHORT).show()
        }

    }

    fun showTextLogo(
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


    fun formatTimestamp(timestamp: String?): String {
        return if (!timestamp.isNullOrEmpty()) {
            val millis = timestamp.toLongOrNull() ?: return ""
            val date = java.util.Date(millis)
            val format = SimpleDateFormat("dd MMMM yyyy hh:mm", Locale.getDefault())
            format.format(date)
        } else {
            "- - -"
        }
    }

}