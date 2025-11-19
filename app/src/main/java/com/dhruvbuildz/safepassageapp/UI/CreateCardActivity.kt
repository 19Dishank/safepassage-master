package com.dhruvbuildz.safepassageapp.UI

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Card
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.CardRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CardViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.CardVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UserVMFactory
import com.dhruvbuildz.safepassageapp.Fetures.CryptographyManager
import com.dhruvbuildz.safepassageapp.Fetures.Utils
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.databinding.ActivityCreateCardBinding

class CreateCardActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCreateCardBinding.inflate(layoutInflater)
    }

    private lateinit var userViewModel: UserViewModel
    private lateinit var cardViewModel: CardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setUpViews()
        setUpViewModel()

    }

    private fun setUpViewModel() {
        val userRepository = UserRepository(SafePassageDatabase(this))
        val viewModelProvider = UserVMFactory(application, userRepository)
        userViewModel = ViewModelProvider(this, viewModelProvider)[UserViewModel::class.java]

        val cardRepository = CardRepository(SafePassageDatabase(this))
        val cardViewModelProvider = CardVMFactory(application, cardRepository)
        cardViewModel = ViewModelProvider(this, cardViewModelProvider)[CardViewModel::class.java]
    }

    private fun setUpViews() {

        binding.backButton.setOnClickListener {
            showDiscardChangesDialog()
        }

        binding.createButton.setOnClickListener {
            createLogin()
        }

        setupEditTextFocus(binding.titleTextBox, getString(R.string.untitled))
        setupEditTextFocus(binding.cardHolderTextBox, getString(R.string.add_cardHolderName))
        setupEditTextFocus(binding.cardNumberTextBox, getString(R.string.add_cardNumber))
        setupEditTextFocus(binding.expDateTextBox, getString(R.string.add_expDate))
        setupEditTextFocus(binding.cvvTextBox, getString(R.string.add_cvv))
        setupEditTextFocus(binding.pinTextBox, getString(R.string.add_pin))
        setupEditTextFocus(binding.noteTextBox, getString(R.string.add_note))


        getEditTextFocus(binding.titleLayout, binding.titleTextBox)
        getEditTextFocus(binding.cardHolderLayout, binding.cardHolderTextBox)
        getEditTextFocus(binding.cardNumberLayout, binding.cardNumberTextBox)
        getEditTextFocus(binding.expDateLayout, binding.expDateTextBox)
        getEditTextFocus(binding.cvvLayout, binding.cvvTextBox)
        getEditTextFocus(binding.pinLayout, binding.pinTextBox)
        getEditTextFocus(binding.noteLayout, binding.noteTextBox)

        setUpCardNumberBox()
        setUpExpDateBox()

    }

    private fun createLogin() {
        val title = binding.titleTextBox.text.toString()
        if (title.isNotEmpty()) {
            addCardData(title)
            super.onBackPressed()
        } else {
            setTitleBoxError()
            Handler(Looper.getMainLooper()).postDelayed({
                binding.titleText.setTextColor(ContextCompat.getColor(this, R.color.gray))
                binding.titleErrorText.visibility = View.GONE
            }, 3000)

        }

    }

    private fun setTitleBoxError() {
        binding.titleText.setTextColor(ContextCompat.getColor(this, R.color.red))
        binding.titleErrorText.visibility = View.VISIBLE
        binding.titleLayout.performClick()
    }

    private fun addCardData(title: String) {

        val cardHolderName =
            binding.cardHolderTextBox.text.toString().takeIf { it.isNotBlank() }
        val cardNumber =
            binding.cardNumberTextBox.text.toString().takeIf { it.isNotBlank() }
        val expDate = binding.expDateTextBox.text.toString().takeIf { it.isNotBlank() }
        val cvv = binding.cvvTextBox.text.toString().takeIf { it.isNotBlank() }
        val pin = binding.pinTextBox.text.toString().takeIf { it.isNotBlank() }
        val note = binding.noteTextBox.text.toString().takeIf { it.isNotBlank() }

        userViewModel.getUser().observe(this, Observer { user ->
            user?.let {
                val userId = it.userId
                val cryptographyManager = CryptographyManager(userId)
                val encryptedCardNumber = cardNumber?.let { cryptographyManager.encryptData(it) }
                val encryptedExpDate = expDate?.let { cryptographyManager.encryptData(it) }
                val encryptedCvv = cvv?.let { cryptographyManager.encryptData(it) }
                val encryptedPin = pin?.let { cryptographyManager.encryptData(it) }
                val encryptedNote = note?.let { cryptographyManager.encryptData(it) }
                val card = Card(
                    0,
                    title,
                    userId,
                    cardHolderName,
                    encryptedCardNumber,
                    encryptedExpDate,
                    encryptedCvv,
                    encryptedPin,
                    encryptedNote
                )
                cardViewModel.insertCard(card)
                
                android.util.Log.d("CreateCardActivity", "Card inserted: ${card.title}")
                
                setResult(RESULT_OK)
                this.onBackPressed()
                Toast.makeText(
                    this, "Card details added Successfully", Toast.LENGTH_LONG
                )
                    .show()
            }

        })
    }

    private fun setupEditTextFocus(editText: EditText, hint: String) {
        editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                editText.hint = hint
            } else {
                editText.hint = ""
            }
        }
    }

    private fun getEditTextFocus(view: View, editText: EditText) {
        view.setOnClickListener {
            editText.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setUpCardNumberBox() {
        binding.cardNumberTextBox.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val space = ' '

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true

                // Remove all spaces and reformat the string
                val formatted = s.toString().replace(" ", "").chunked(4).joinToString(" ")

                // Set the formatted text to the EditText
                binding.cardNumberTextBox.setText(formatted)
                binding.cardNumberTextBox.setSelection(formatted.length)

                isUpdating = false
            }
        })
    }

    private fun setUpExpDateBox() {
        binding.expDateTextBox.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val slash = " / "

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true

                val input = s.toString()
                val formatted = StringBuilder()

                // Remove the slash to handle formatting
                val digitsOnly = input.replace(" / ", "")

                // Add the slash after the first two digits (month)
                if (digitsOnly.length >= 2) {
                    formatted.append(digitsOnly.substring(0, 2))
                    if (digitsOnly.length > 2) {
                        formatted.append(slash)
                        formatted.append(digitsOnly.substring(2))
                    }
                } else {
                    formatted.append(digitsOnly)
                }

                // Set the formatted text back to the EditText
                binding.expDateTextBox.setText(formatted.toString())
                binding.expDateTextBox.setSelection(formatted.length)

                isUpdating = false
            }
        })
    }
    private fun showDiscardChangesDialog(){
        Utils.showDiscardChangesDialog(
            context = this,
            title = "Discard Changes",
            buttonName = "Discard",
            message = "Are you sure you want to discard your changes?",
            onPositiveClick = {
                this.onBackPressed()
                finish()
            }
        )
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        showDiscardChangesDialog()
    }
}