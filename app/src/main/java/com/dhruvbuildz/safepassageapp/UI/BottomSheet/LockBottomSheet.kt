package com.dhruvbuildz.safepassageapp.UI.BottomSheet

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PinRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PinViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.PinVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UserVMFactory
import com.dhruvbuildz.safepassageapp.Fetures.CryptographyManager
import com.dhruvbuildz.safepassageapp.Fetures.HashUtils
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Pin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.dhruvbuildz.safepassageapp.databinding.FragmentLockBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.dhruvbuildz.safepassageapp.Utils.SessionManager
import java.util.concurrent.Executor

class LockBottomSheet : BottomSheetDialogFragment() {

    private val binding by lazy {
        FragmentLockBinding.inflate(layoutInflater)
    }
    private lateinit var userViewModel: UserViewModel
    private lateinit var pinViewModel: PinViewModel
    private var userId: String? = null

    lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var bioLock: Boolean = false
    private var onBtnClick: (() -> Unit)? = null
    private var onCancelled: (() -> Unit)? = null
    private var authenticated: Boolean = false

    companion object {
        private const val ARG_BIO_LOCK = "arg_bio_lock"

        fun newInstance(bioLock: Boolean, onBtnClick: () -> Unit, onCancelled: (() -> Unit)? = null): LockBottomSheet {
            return LockBottomSheet().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_BIO_LOCK, bioLock)
                }
                this.onBtnClick = onBtnClick
                this.onCancelled = onCancelled
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bioLock = it.getBoolean(ARG_BIO_LOCK)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewModel()
        setupUI()
        observeUser()
        binding.root.post {
            if (bioLock) {
                biometricAuth()
            }
        }
    }

    private fun biometricAuth() {
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(requireActivity(), executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // User cancelled or error occurred — fall back to PIN entry without dismissing
                    showToast("Use PIN to unlock")
                    binding.editText.requestFocus()
                    showKeyboard(binding.editText)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showToast("Authentication succeeded!")
                    authenticated = true
                    onBtnClick?.invoke()
                    dismiss()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Do not dismiss; allow retry. No-op here.
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .setConfirmationRequired(false)
            .build()

        biometricPrompt.authenticate(promptInfo)

    }

    private fun setUpViewModel() {
        val userRepository = UserRepository(SafePassageDatabase(requireContext()))
        val viewModelProvider = UserVMFactory(requireActivity().application, userRepository)
        userViewModel = ViewModelProvider(this, viewModelProvider)[UserViewModel::class.java]

        val pinViewModelProvider = PinVMFactory(requireActivity().application)
        pinViewModel = ViewModelProvider(this, pinViewModelProvider)[PinViewModel::class.java]
    }

    private fun setupUI() {
        binding.editText.post {
            binding.editText.requestFocus()
            showKeyboard(binding.editText)
        }

        binding.buttonUnlock.setOnClickListener {
            val pin = binding.editText.text.toString()
            if (pin.isBlank()) {
                showToast("Please enter a PIN")
            } else if (pin.length != 4) {
                showToast("PIN must be 4 digits")
            } else {
                validatePin(pin)
            }
        }
    }

    private fun observeUser() {
        val currentUserId = SessionManager(requireContext()).getLastUserId()
        if (currentUserId != null) {
            userViewModel.getUserById(currentUserId).observe(viewLifecycleOwner) { user ->
                userId = user?.userId ?: FirebaseAuth.getInstance().currentUser?.uid
                if (userId.isNullOrEmpty()) {
                    showToast("User not found")
                }
            }
        } else {
            userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId.isNullOrEmpty()) {
                showToast("User not found")
            }
        }
    }

    private fun validatePin(pin: String) {
        if (userId == null) {
            showToast("Unable to validate PIN: User ID not available")
            return
        }

        val uid = userId!!
        val cryptographyManager = CryptographyManager(uid)
        pinViewModel.getPinByUserId(uid).observe(viewLifecycleOwner, Observer { pinData ->
            pinData?.let {
                val decryptedPin = cryptographyManager.decryptData(it.encryptedPin)
                if (decryptedPin == pin) {
                    showToast("PIN Matched")
                    authenticated = true
                    onBtnClick?.invoke()
                    dismiss()
                } else {
                    // Local mismatch — try remote verification
                    verifyRemoteAndMaybePersist(uid, pin, cryptographyManager)
                }
            } ?: run {
                // No local PIN — try remote verification
                verifyRemoteAndMaybePersist(uid, pin, cryptographyManager)
            }
        })
    }

    private fun verifyRemoteAndMaybePersist(uid: String, enteredPin: String, cryptographyManager: CryptographyManager) {
        FirebaseDatabase.getInstance().reference
            .child("users").child(uid).child("pinMeta")
            .get().addOnSuccessListener { snap ->
                val salt = snap.child("salt").value?.toString()
                val pinHash = snap.child("pinHash").value?.toString()
                if (salt.isNullOrBlank() || pinHash.isNullOrBlank()) {
                    showToast("PIN not available")
                    return@addOnSuccessListener
                }
                val computed = HashUtils.hashPin(enteredPin, salt)
                if (computed == pinHash) {
                    // Recreate local PIN for this device to avoid asking again next time
                    val encrypted = cryptographyManager.encryptData(enteredPin)
                    pinViewModel.insertPin(Pin(userId = uid, encryptedPin = encrypted))
                    showToast("PIN Verified")
                    authenticated = true
                    onBtnClick?.invoke()
                    dismiss()
                } else {
                    showToast("Incorrect PIN")
                }
            }.addOnFailureListener {
                showToast("Network error. Try again")
            }
    }



    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        // Keep the bottom sheet expanded when the keyboard is shown
        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        if (!authenticated) {
            onCancelled?.invoke()
        }
    }

    private fun showKeyboard(view: View) {
        val inputMethodManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}
