package com.dhruvbuildz.safepassageapp.UI

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UtilitiesRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UtilitiesViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UtilitiesVMFactory
import com.dhruvbuildz.safepassageapp.Fetures.PasswordManager
import com.dhruvbuildz.safepassageapp.Fetures.Utils
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.databinding.ActivityPasswordGeneratorBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class PasswordGeneratorActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPasswordGeneratorBinding.inflate(layoutInflater)
    }

    lateinit var utilitiesViewModel: UtilitiesViewModel

    private var passwordLength: Int = 10
    private val passwordManager = PasswordManager()
    private var customWords: String? = null
    private var isCustomWords = false

    private lateinit var passwordBox: EditText
    private lateinit var slider: Slider
    private lateinit var uppercaseChip: Chip
    private lateinit var lowercaseChip: Chip
    private lateinit var numbersChip: Chip
    private lateinit var symbolsChip: Chip
    private lateinit var refreshButton: ImageView
    private lateinit var copyButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupViewBindings()
        setupClickListeners()
        generatePassword()
        Utils.screenshotCheck(utilitiesViewModel,this,this)
    }

    private fun setupViewBindings() {
        passwordBox = binding.passwordBox
        slider = binding.slider
        uppercaseChip = binding.uppercaseChip
        lowercaseChip = binding.lowercaseChip
        numbersChip = binding.numbersChip
        symbolsChip = binding.symbolsChip
        refreshButton = binding.refreshButton
        copyButton = binding.copyButton
        val utilitiesRepository = UtilitiesRepository(SafePassageDatabase(this))
        val utilitiesViewModelProvider = UtilitiesVMFactory(application, utilitiesRepository)
        utilitiesViewModel =
            ViewModelProvider(this, utilitiesViewModelProvider)[UtilitiesViewModel::class.java]
    }

    private fun setupClickListeners() {
        slider.addOnChangeListener { _, value, _ ->
            passwordLength = value.toInt()
            binding.passwordLengthNum.text = passwordLength.toString()
            generatePassword()
        }

        binding.backButton.setOnClickListener { onBackPressed() }
        binding.helpButton.setOnClickListener { Utils.showPasswordTipsDialog(this) }
        binding.copyAndCloseBtn.setOnClickListener {
            copyPasswordToClipboard(passwordBox.text.toString())
            onBackPressed()
        }

        binding.addWordsButton.setOnClickListener { showWordsDialog() }

        refreshButton.setOnClickListener { generatePassword() }
        copyButton.setOnClickListener { copyPasswordToClipboard(passwordBox.text.toString()) }

        uppercaseChip.setOnCheckedChangeListener { _, _ -> generatePassword() }
        lowercaseChip.setOnCheckedChangeListener { _, _ -> generatePassword() }
        numbersChip.setOnCheckedChangeListener { _, _ -> generatePassword() }
        symbolsChip.setOnCheckedChangeListener { _, _ -> generatePassword() }
    }

    private fun generatePassword() {
        val customWordList = if (isCustomWords) {
            customWords?.split(" ")?.filter { it.isNotEmpty() } ?: emptyList()
        } else {
            emptyList()
        }

        try {
            val password = passwordManager.generatePassword(
                length = passwordLength,
                includeUppercase = uppercaseChip.isChecked,
                includeLowercase = lowercaseChip.isChecked,
                includeNumbers = numbersChip.isChecked,
                includeSymbols = symbolsChip.isChecked,
                customWords = customWordList
            )
            passwordBox.setText(password)
            updatePasswordStrength(password)
        } catch (e: IllegalArgumentException) {
            passwordBox.setText("")
            Snackbar.make(binding.root, e.message.toString(), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun copyPasswordToClipboard(password: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Generated Password", password)
        clipboard.setPrimaryClip(clip)
        Snackbar.make(binding.root, "Password copied to clipboard", Snackbar.LENGTH_LONG).show()
    }

    @SuppressLint("SetTextI18n")
    private fun showWordsDialog() {
        val editTextLy = LayoutInflater.from(this).inflate(R.layout.edittextbox, null)
        val txtBox = editTextLy.findViewById<TextInputEditText>(R.id.editTxt)

        if (isCustomWords) {
            txtBox.setText(customWords)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Your Words")
            .setMessage("You can add your name, any word, or number. e.g., Dhruv Patel 1663")
            .setView(editTextLy)
            .setPositiveButton("Add") { _, _ ->
                customWords = txtBox.text.toString()
                isCustomWords = true
                binding.addWordsButton.text = "Change Your Words"
                generatePassword()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()
            .show()
    }

    private fun updatePasswordStrength(password: String) {
        val strength = PasswordManager().getPasswordStrength(password)
        val strengthColor = when (strength) {
            getString(R.string.strong) -> R.color.darkGreen
            getString(R.string.weak) -> R.color.yellow
            getString(R.string.vulnerable) -> R.color.red
            else -> R.color.gray // fallback color
        }

        binding.passwordStrength.apply {
            text = strength
            setTextColor(ContextCompat.getColor(this@PasswordGeneratorActivity, strengthColor))
        }

        binding.passwordIcon.setColorFilter(
            ContextCompat.getColor(this@PasswordGeneratorActivity, strengthColor)
        )
    }

}
