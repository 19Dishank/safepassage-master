package com.dhruvbuildz.safepassageapp.UI.Home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Utilities
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PinViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UtilitiesViewModel
import com.dhruvbuildz.safepassageapp.Fetures.FeedbackDialog
import com.dhruvbuildz.safepassageapp.Fetures.Utils
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.UI.BottomSheet.LockBottomSheet
import com.dhruvbuildz.safepassageapp.UI.LoginSignUp.Login_Screen
import com.dhruvbuildz.safepassageapp.UI.SetPinActivity
import com.dhruvbuildz.safepassageapp.Utils.SessionManager
import com.dhruvbuildz.safepassageapp.Utils.ThemeManager
import com.dhruvbuildz.safepassageapp.databinding.FragmentSettingBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.net.Uri

class SettingFragment : Fragment() {

    private val binding by lazy {
        FragmentSettingBinding.inflate(layoutInflater)
    }

    private lateinit var userViewModel: UserViewModel
    private lateinit var utilitiesViewModel: UtilitiesViewModel
    private lateinit var pinViewModel: PinViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        userViewModel = (activity as MainActivity).userViewModel
        utilitiesViewModel = (activity as MainActivity).utilitiesViewModel
        pinViewModel = (activity as MainActivity).pinViewModel
        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(requireContext())

        setUpUserCard()
        setUpSwitches()
        onClicks()

        return binding.root
    }

    private fun setUpUserCard() {
        val currentUserId = sessionManager.getLastUserId()
        if (currentUserId == null) {
            binding.titleText.text = "Guest"
            binding.emailText.text = "No email available"
            binding.logoText.text = "--"
            return
        }

        userViewModel.getUserById(currentUserId).observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.titleText.text = user.userName
                binding.emailText.text = user.email
                binding.buttonFeedback.isEnabled = true
                binding.buttonFeedback.setOnClickListener {
                    val feedbackDialog = FeedbackDialog(requireContext(), user)
                    feedbackDialog.showPageFeedbackDialog(
                        layoutInflater = layoutInflater,
                        dialogLayoutId = R.layout.dialog_submit_ai_tool,
                        textInputLayoutNameId = R.id.editTextName,
                        positiveButtonText = "Submit",
                        negativeButtonText = "Cancel",
                        onSubmitAction = {
                            Toast.makeText(requireContext(), "Feedback submitted", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } else {
                binding.titleText.text = "Guest"
                binding.emailText.text = "No email available"
                binding.buttonFeedback.isEnabled = false
                binding.buttonFeedback.setOnClickListener {
                    Toast.makeText(requireContext(), "Please log in to send feedback.", Toast.LENGTH_SHORT).show()
                }
            }

            val userName = binding.titleText.text.toString()
            binding.logoText.text = if (userName.length >= 2) {
                userName.substring(0, 2).uppercase()
            } else {
                userName.uppercase()
            }
        }
    }

    private fun setUpSwitches() {
        // Set up theme toggle
        setUpThemeToggle()
        
        // Set up screenshot switch
        utilitiesViewModel.getUtilities().observe(viewLifecycleOwner) { utilities ->
            val defaultUtilities = utilities ?: Utilities()

            binding.switchScreenshot.setOnCheckedChangeListener(null)
            binding.switchScreenshot.isChecked = defaultUtilities.isScreenshot

            binding.switchScreenshot.setOnCheckedChangeListener { _, isChecked ->
                updateUtilitiesState(
                    Utilities(
                        isLockApp = defaultUtilities.isLockApp,
                        lockCode = defaultUtilities.lockCode,
                        isUsePhoneLock = defaultUtilities.isUsePhoneLock,
                        isDBSync = defaultUtilities.isDBSync,
                        isScreenshot = isChecked
                    )
                )
            }
        }
    }
    
    private fun setUpThemeToggle() {
        // Initialize switch state based on current theme
        val isDarkMode = ThemeManager.isDarkMode(requireContext())
        binding.switchTheme.setOnCheckedChangeListener(null)
        binding.switchTheme.isChecked = isDarkMode
        
        // Handle theme toggle
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            val themeMode = if (isChecked) {
                ThemeManager.THEME_DARK
            } else {
                ThemeManager.THEME_LIGHT
            }
            
            ThemeManager.setThemeMode(requireContext(), themeMode)
            ThemeManager.applyTheme(themeMode)
            
            // Recreate activity to apply theme changes
            requireActivity().recreate()
        }
    }

    private fun updateUtilitiesState(utilities: Utilities) {
        utilitiesViewModel.updateUtilities(utilities)
        requireActivity().recreate()
    }

    private fun onClicks() {
        binding.signOutButton.setOnClickListener {
            showSignOutDialog()
        }

        binding.buttonLockApp.setOnClickListener {
            val lockBottomSheet = LockBottomSheet.newInstance(onBtnClick = {
                val intent = Intent(requireContext(), SetPinActivity::class.java)
                intent.putExtra("isUpdate", true)
                startActivity(intent)
            }, bioLock = false)
            lockBottomSheet.show(requireActivity().supportFragmentManager, lockBottomSheet.tag)
        }

        binding.buttonFeedback.setOnClickListener {
            Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.buttonPrivacy.setOnClickListener {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference("privacy_policy")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val url = snapshot.value as String
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), "Privacy policy URL not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load privacy policy", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun showSignOutDialog() {
        Utils.showDiscardChangesDialog(
            context = requireContext(),
            title = "Logout",
            buttonName = "Logout",
            message = "Are you sure you want to log out?",
            onPositiveClick = {
                auth.signOut()
                sessionManager.markLogout()

                Snackbar.make(binding.root, "Logged out successfully", Snackbar.LENGTH_SHORT).show()

                val intent = Intent(requireContext(), Login_Screen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        )
    }
}
