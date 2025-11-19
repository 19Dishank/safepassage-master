package com.dhruvbuildz.safepassageapp.UI

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Password
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PasswordRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PasswordViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.PasswordVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UserVMFactory
import com.dhruvbuildz.safepassageapp.Fetures.CryptographyManager
import com.dhruvbuildz.safepassageapp.Fetures.PasswordManager
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.UI.Adapter.PasswordAdapter
import com.dhruvbuildz.safepassageapp.databinding.ActivityViewListBinding
import com.dhruvbuildz.safepassageapp.Utils.SessionManager

class ViewListActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityViewListBinding.inflate(layoutInflater)
    }
    private lateinit var userViewModel: UserViewModel
    private lateinit var passwordViewModel: PasswordViewModel
    private lateinit var passwordRepository: PasswordRepository

    private lateinit var passwordAdapter: PasswordAdapter
    private lateinit var cryptographyManager: CryptographyManager
    private lateinit var passwordManager: PasswordManager
    var userId = " "
    private var listId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        this.listId = intent.getIntExtra("List_ID", -1)
        if (this.listId != -1) {
            setUpViews()
        }
    }

    private fun setUpViews() {
        setUpViewModel()
        passwordManager = PasswordManager()
        val currentUserId = SessionManager(this).getLastUserId()
        if (currentUserId.isNullOrEmpty()) {
            finish()
            return
        }
        userId = currentUserId
        cryptographyManager = CryptographyManager(userId)
        setUpRecyclerView()
        when (listId) {
            1 -> setUpReusedList()
            2 -> setUpWeakList()
            3 -> setUpVulList()
        }
    }

    private fun setUpRecyclerView() {
        binding.mainRcView.layoutManager = LinearLayoutManager(this)
        passwordAdapter = PasswordAdapter(mutableListOf(), passwordViewModel)
        binding.mainRcView.adapter = passwordAdapter
    }

    private fun setUpViewModel() {
        val userRepository = UserRepository(SafePassageDatabase(this))
        val viewModelProvider = UserVMFactory(application, userRepository)
        userViewModel = ViewModelProvider(this, viewModelProvider)[UserViewModel::class.java]

        passwordRepository = PasswordRepository(SafePassageDatabase(this))
        val passViewModelProvider = PasswordVMFactory(application, passwordRepository)
        passwordViewModel =
            ViewModelProvider(this, passViewModelProvider)[PasswordViewModel::class.java]

    }

    private fun setUpReusedList() {
        binding.headerTxt.setText(R.string.reused_passwords)
        binding.descText.setText(R.string.reused_passwords_desc)

        passwordViewModel.getAllPasswords(userId).observe(this) { passwords ->
            getReusedPasswords(passwords).observe(this) { reusedPasswords ->
                passwordAdapter.updateItems(reusedPasswords)
            }
        }
    }

    private fun setUpWeakList() {
        binding.headerTxt.setText(R.string.weak_passwords)
        binding.descText.setText(R.string.weak_passwords_desc)

        passwordViewModel.getAllPasswords(userId).observe(this) { passwords ->
            getWeakPasswords(passwords).observe(this) { weakPasswords ->
                passwordAdapter.updateItems(weakPasswords)
            }
        }
    }

    private fun setUpVulList() {
        binding.headerTxt.setText(R.string.vulnerable_passwords)
        binding.descText.setText(R.string.vulnerable_passwords_desc)

        passwordViewModel.getAllPasswords(userId).observe(this) { passwords ->
            getVulPasswords(passwords).observe(this) { vulPasswords ->
                passwordAdapter.updateItems(vulPasswords)
            }
        }
    }

    fun getWeakPasswords(passwords: List<Password>): LiveData<List<Password>> {
        val weakPasswords = mutableListOf<Password>()

        passwords.forEach { pass ->
            pass.password?.let { encryptedPassword ->
                try {
                    val decryptedPassword = cryptographyManager.decryptData(encryptedPassword)
                    if (decryptedPassword?.let { passwordManager.getPasswordStrength(it) } == "Weak") {
                        weakPasswords.add(pass)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return liveData { emit(weakPasswords) }
    }

    fun getVulPasswords(passwords: List<Password>): LiveData<List<Password>> {
        val vulPasswords = mutableListOf<Password>()

        passwords.forEach { pass ->
            pass.password?.let { encryptedPassword ->
                try {
                    val decryptedPassword = cryptographyManager.decryptData(encryptedPassword)
                    if (decryptedPassword?.let { passwordManager.getPasswordStrength(it) } == "Vulnerable") {
                        vulPasswords.add(pass)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return liveData { emit(vulPasswords) }
    }

    fun getReusedPasswords(passwords: List<Password>): LiveData<List<Password>> {
        val reusedPasswords = mutableListOf<Password>()
        val passwordOccurrences = mutableMapOf<String, MutableList<Password>>()

        passwords.forEach { pass ->
            pass.password?.let { encryptedPassword ->
                try {
                    val decryptedPassword = cryptographyManager.decryptData(encryptedPassword)

                    if (decryptedPassword != null) {
                        val occurrenceList = passwordOccurrences.getOrPut(decryptedPassword) { mutableListOf() }
                        occurrenceList.add(pass)

                        // If the password has been seen before, add all occurrences to reusedPasswords
                        if (occurrenceList.size > 1) {
                            reusedPasswords.addAll(occurrenceList)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return liveData { emit(reusedPasswords.distinct()) }
    }
}