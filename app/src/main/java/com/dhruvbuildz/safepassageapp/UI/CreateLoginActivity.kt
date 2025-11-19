    package com.dhruvbuildz.safepassageapp.UI

    import android.annotation.SuppressLint
    import android.content.Intent
    import android.os.Bundle
    import android.os.Handler
    import android.os.Looper
    import android.text.Editable
    import android.text.TextWatcher
    import android.view.LayoutInflater
    import android.view.View
    import android.view.WindowManager
    import android.view.inputmethod.InputMethodManager
    import android.widget.ArrayAdapter
    import android.widget.EditText
    import android.widget.Toast
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import androidx.lifecycle.Observer
    import androidx.lifecycle.ViewModelProvider
    import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
    import com.dhruvbuildz.safepassageapp.Database.Room.Model.Category
    import com.dhruvbuildz.safepassageapp.Database.Room.Model.Password
    import com.dhruvbuildz.safepassageapp.Database.Room.Repository.CategoryRepository
    import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PasswordRepository
    import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
    import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CategoryViewModel
    import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PasswordViewModel
    import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
    import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.CategoryVMFactory
    import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.PasswordVMFactory
    import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UserVMFactory
    import com.dhruvbuildz.safepassageapp.Fetures.CryptographyManager
    import com.dhruvbuildz.safepassageapp.Fetures.PasswordManager
    import com.dhruvbuildz.safepassageapp.Fetures.Utils
    import com.dhruvbuildz.safepassageapp.R
    import com.dhruvbuildz.safepassageapp.databinding.ActivityCreateLoginBinding
    import com.google.android.material.dialog.MaterialAlertDialogBuilder
    import com.google.android.material.textfield.TextInputEditText


    class CreateLoginActivity : AppCompatActivity() {

        private val binding by lazy {
            ActivityCreateLoginBinding.inflate(layoutInflater)
        }
        private lateinit var userViewModel: UserViewModel
        private lateinit var passwordViewModel: PasswordViewModel
        private lateinit var categoryViewModel: CategoryViewModel

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
            setUpCategory()


        }

        private fun setUpViews() {

            binding.backButton.setOnClickListener {
                showDiscardChangesDialog()
            }

            binding.createButton.setOnClickListener {
                createLogin()
            }

            binding.generatePasswordBtn.setOnClickListener {
                startActivity(Intent(this, PasswordGeneratorActivity::class.java))
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

            binding.passwordInfo.setOnClickListener {
                Utils.showPasswordTipsDialog(this)
            }

            binding.addCategory.setOnClickListener {
                showAddCatDialog()
            }

            setupEditTextFocus(binding.emailTextBox, getString(R.string.add_email_address))
            setupEditTextFocus(binding.usernameTextBox, getString(R.string.add_username))
            setupEditTextFocus(binding.phoneTextBox, getString(R.string.add_phone_number))
            setupEditTextFocus(binding.urlTextBox, getString(R.string.https))
            setupEditTextFocus(binding.noteTextBox, getString(R.string.add_note))

            getEditTextFocus(binding.titleLayout, binding.titleTextBox)
            getEditTextFocus(binding.emailLayout, binding.emailTextBox)
            getEditTextFocus(binding.userNameLayout, binding.usernameTextBox)
            getEditTextFocus(binding.phoneLayout, binding.phoneTextBox)
            getEditTextFocus(binding.passwordLayout, binding.passwordTextBox)
            getEditTextFocus(binding.urlLayout, binding.urlTextBox)
            getEditTextFocus(binding.noteLayout, binding.noteTextBox)

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

        private fun createLogin() {
            val title = binding.titleTextBox.text.toString()
            if (title.isNotEmpty()) {
                addLoginData(title)
            } else {
                setTitleBoxError()
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.titleText.setTextColor(ContextCompat.getColor(this, R.color.gray))
                    binding.titleErrorText.visibility = View.GONE
                }, 3000)
            }

        }

        private fun addLoginData(title: String) {
            val email = binding.emailTextBox.text.toString().takeIf { it.isNotBlank() }
            val userName = binding.usernameTextBox.text.toString().takeIf { it.isNotBlank() }
            val number = binding.phoneTextBox.text.toString().takeIf { it.isNotBlank() }
            val passwordText = binding.passwordTextBox.text.toString().takeIf { it.isNotBlank() }
            val url = binding.urlTextBox.text.toString().takeIf { it.isNotBlank() }
            val note = binding.noteTextBox.text.toString().takeIf { it.isNotBlank() }
            val time = System.currentTimeMillis().toString()
    
            userViewModel.getUser().observe(this, Observer { user ->
                user?.let {
                    val userId = it.userId
                    val cryptographyManager = CryptographyManager(userId)
                    val encryptedEmail = email?.let { cryptographyManager.encryptData(it) }
                    val encryptedUserName = userName?.let { cryptographyManager.encryptData(it) }
                    val encryptedNumber = number?.let { cryptographyManager.encryptData(it) }
                    val encryptedPassword = passwordText?.let { cryptographyManager.encryptData(it) }
                    val encryptedNote = note?.let { cryptographyManager.encryptData(it) }
                    val categoryName = binding.categoryBox.selectedItem?.toString()

                    if (categoryName.isNullOrBlank()) {
                        val password = Password(
                            0,
                            userId,
                            title,
                            encryptedUserName,
                            encryptedEmail,
                            encryptedNumber,
                            encryptedPassword,
                            url,
                            null,
                            encryptedNote,
                            null,
                            time,
                            null,
                            null,
                        )
                        passwordViewModel.insertPassword(password)
                        Toast.makeText(this, "Login details added Successfully", Toast.LENGTH_LONG).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        categoryViewModel.getCategoryIdByName(categoryName)
                            .observe(this, Observer { categoryList ->
                                categoryList?.let { categories ->
                                    if (categories.isNotEmpty()) {
                                        val categoryId = categories[0].catId
                                        val password = Password(
                                            0,
                                            userId,
                                            title,
                                            encryptedUserName,
                                            encryptedEmail,
                                            encryptedNumber,
                                            encryptedPassword,
                                            url,
                                            null,
                                            encryptedNote,
                                            categoryId,
                                            time,
                                            null,
                                            null
                                        )
                                        passwordViewModel.insertPassword(password)
                                        
                                        android.util.Log.d("CreateLoginActivity", "Password inserted: ${password.title}")
                                        
                                        Toast.makeText(
                                            this,
                                            "Login details added Successfully",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        setResult(RESULT_OK)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Category not found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            })
                    }
                }
            })
        }

        private fun setTitleBoxError() {
            binding.titleText.setTextColor(ContextCompat.getColor(this, R.color.red))
            binding.titleErrorText.visibility = View.VISIBLE
            binding.titleLayout.performClick()
        }

        private fun updatePasswordStrength(password: String) {
            val strength = PasswordManager().getPasswordStrength(password)
            when (strength) {
                getString(R.string.strong) -> {
                    binding.apply {
                        passwordStrength.text = getString(R.string.strong)
                        passwordStrength.setTextColor(
                            ContextCompat.getColor(
                                this@CreateLoginActivity, R.color.green
                            )
                        )
                        dot.setTextColor(
                            ContextCompat.getColor(
                                this@CreateLoginActivity, R.color.green
                            )
                        )
                        passwordText.setTextColor(
                            ContextCompat.getColor(
                                this@CreateLoginActivity, R.color.green
                            )
                        )
                        passwordIcon.setColorFilter(
                            ContextCompat.getColor(
                                this@CreateLoginActivity, R.color.green
                            )
                        )
                    }
                }

                getString(R.string.weak) -> {
                    binding.apply {
                        passwordStrength.text = getString(R.string.weak)
                        passwordStrength.setTextColor(
                            ContextCompat.getColor(
                                this@CreateLoginActivity, R.color.yellow
                            )
                        )
                        dot.setTextColor(
                            ContextCompat.getColor(
                                this@CreateLoginActivity, R.color.yellow
                            )
                        )
                        passwordText.setTextColor(
                            ContextCompat.getColor(
                                this@CreateLoginActivity, R.color.yellow
                            )
                        )
                        passwordIcon.setColorFilter(
                            ContextCompat.getColor(
                                this@CreateLoginActivity, R.color.yellow
                            )
                        )
                    }
                }

                getString(R.string.vulnerable) -> {
                    binding.apply {
                        passwordStrength.text = getString(R.string.vulnerable)
                        passwordStrength.setTextColor(
                            ContextCompat.getColor(
                                this@CreateLoginActivity, R.color.red
                            )
                        )
                        dot.setTextColor(
                            ContextCompat.getColor(
                                this@CreateLoginActivity, R.color.red
                            )
                        )
                        passwordText.setTextColor(
                            ContextCompat.getColor(
                                this@CreateLoginActivity, R.color.red
                            )
                        )
                        passwordIcon.setColorFilter(
                            ContextCompat.getColor(
                                this@CreateLoginActivity, R.color.red
                            )
                        )
                    }
                }
            }
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


        private fun showAddCatDialog() {

            val editTextLy = LayoutInflater.from(this).inflate(R.layout.edittextbox, null)

            val txtBox = editTextLy.findViewById<TextInputEditText>(R.id.editTxt)


            val dialog =
                MaterialAlertDialogBuilder(this).setTitle("Add New Category").setView(editTextLy)
                    .setPositiveButton("Add") { _, _ ->
                        val inputText = txtBox.text.toString()
                        val time = System.currentTimeMillis().toString()
                        val category = Category(0, inputText, time)
                        categoryViewModel.insertCategory(category)
                    }.setNegativeButton("Cancel") { dialog, which ->
                        dialog.cancel()
                    }.create()

            dialog.show()
        }

        private fun setUpCategory() {
            categoryViewModel.getAllCategory().observe(this) { categories ->
                categories?.let {
                    val categoryNames = it.map { category -> category.catName }
                    val adapter = ArrayAdapter(
                        this, android.R.layout.simple_dropdown_item_1line, categoryNames
                    )
                    binding.categoryBox.setAdapter(adapter)
                    if (categoryNames.isNotEmpty()) {
                        binding.categoryBox.setSelection(0)
                    }
                }
            }

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