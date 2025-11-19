    package com.dhruvbuildz.safepassageapp.UI.Home

    import android.content.Intent
    import android.os.Bundle
    import androidx.fragment.app.Fragment
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Observer
    import com.dhruvbuildz.safepassageapp.Database.Room.Model.Password
    import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PasswordViewModel
    import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
    import com.dhruvbuildz.safepassageapp.Fetures.CryptographyManager
    import com.dhruvbuildz.safepassageapp.Fetures.PasswordManager
    import com.dhruvbuildz.safepassageapp.UI.ViewListActivity
    import com.dhruvbuildz.safepassageapp.databinding.FragmentPassMonitorBinding
    import kotlinx.coroutines.launch

    class PassMonitorFragment : Fragment() {

        lateinit var passwordViewModel: PasswordViewModel
        lateinit var userViewModel: UserViewModel
        private lateinit var cryptographyManager: CryptographyManager
        private lateinit var passwordManager: PasswordManager
        var userId = " "

        private val binding by lazy {
            FragmentPassMonitorBinding.inflate(layoutInflater)
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {

            setUp()



            return binding.root
        }

        fun setUp() {
            passwordViewModel = (activity as MainActivity).passwordViewModel
            val mainActivity = (activity as MainActivity)
            userViewModel = mainActivity.userViewModel
            passwordManager = PasswordManager()

            val currentUserId = mainActivity.getCurrentUserId()
            if (currentUserId != null) {
                userViewModel.getUserById(currentUserId).observe(viewLifecycleOwner, Observer { user ->
                    user?.let {
                        userId = it.userId
                        cryptographyManager = CryptographyManager(userId)
                        observePasswords()
                    }
                })
            }
            setUpViewClicks()

        }

        private fun setUpViewClicks(){
            binding.reusedLayout.setOnClickListener {
                val intent = Intent(requireContext(), ViewListActivity::class.java).apply {
                    putExtra("List_ID", 1)
                }
                startActivity(intent)
            }
            binding.weakLayout.setOnClickListener {
                val intent = Intent(requireContext(),ViewListActivity::class.java).apply {
                    putExtra("List_ID", 2)
                }
                startActivity(intent)
            }
            binding.vulLayout.setOnClickListener {
                val intent = Intent(requireContext(),ViewListActivity::class.java).apply {
                    putExtra("List_ID", 3)
                }
                startActivity(intent)
            }
        }

        private fun observePasswords() {
            passwordViewModel.getAllPasswords(userId).observe(viewLifecycleOwner) { passwords ->
                lifecycleScope.launch {
                    val counts = getPassCount(passwords)
                    updateUI(counts)
                }
            }
        }

        private fun getPassCount(passwords: List<Password>): IntArray {
            val count = intArrayOf(0, 0, 0, 0) // count[3] will store reused count
            val passwordOccurrences = mutableMapOf<String, Int>()

            passwords.forEach { pass ->
                pass.password?.let { encryptedPassword ->
                    try {
                        val decryptedPassword = cryptographyManager.decryptData(encryptedPassword)

                        // Track occurrences of each decrypted password
                        val occurrences = passwordOccurrences.getOrDefault(decryptedPassword, 0) + 1
                        passwordOccurrences[decryptedPassword] = occurrences

                        // If the password has more than one occurrence, count it as reused
                        if (occurrences > 1) {
                            count[3]++
                        }

                        // Check password strength and increment appropriate counter
                        when (passwordManager.getPasswordStrength(decryptedPassword)) {
                            "Strong" -> count[0]++
                            "Weak" -> count[1]++
                            else -> count[2]++
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return count
        }


        private fun updateUI(counts: IntArray) {
    //        binding.strongText.text = counts[0].toString()

            binding.weakText.text = counts[1].toString()
            binding.vulText.text = counts[2].toString()
            binding.reusedText.text = counts[3].toString()
        }


    }