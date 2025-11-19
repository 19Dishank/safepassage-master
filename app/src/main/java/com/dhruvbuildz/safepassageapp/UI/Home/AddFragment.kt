package com.dhruvbuildz.safepassageapp.UI.Home

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.findNavController
import com.dhruvbuildz.safepassageapp.UI.AddDocumentActivity
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.UI.CreateCardActivity
import com.dhruvbuildz.safepassageapp.UI.CreateLoginActivity
import com.dhruvbuildz.safepassageapp.UI.PasswordGeneratorActivity
import com.dhruvbuildz.safepassageapp.databinding.FragmentAddBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddFragment : BottomSheetDialogFragment() {

    private val binding by lazy {
        FragmentAddBinding.inflate(layoutInflater)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding.loginBtn.setOnClickListener {
            val intent = Intent(context, CreateLoginActivity::class.java)
            startActivityForResult(intent, 1002)
        }

        binding.creditCardBtn.setOnClickListener {
            val intent = Intent(context, CreateCardActivity::class.java)
            startActivityForResult(intent, 1003)
        }

        binding.passwordBtn.setOnClickListener {
            startActivity(Intent(context, PasswordGeneratorActivity::class.java))
        }
        binding.documentBtn.setOnClickListener {
            val intent = Intent(context, AddDocumentActivity::class.java)
            startActivityForResult(intent, 1001)
        }

        return binding.root
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        findNavController().navigateUp()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        findNavController().navigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == android.app.Activity.RESULT_OK) {
            when (requestCode) {
                1001, 1002, 1003 -> {
                    android.util.Log.d("AddFragment", "Activity result received for requestCode: $requestCode")
                    // Refresh the main activity data for documents, passwords, and cards
                    (activity as? MainActivity)?.refreshData()
                    
                    // Force refresh the HomeFragment
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        (activity as? MainActivity)?.let { mainActivity ->
                            val homeFragment = mainActivity.supportFragmentManager
                                .findFragmentById(R.id.fragmentContainerView)
                                ?.childFragmentManager
                                ?.fragments
                                ?.firstOrNull { it is HomeFragment } as? HomeFragment
                            
                            homeFragment?.updateUnifiedList()
                        }
                    }, 500)
                }
            }
        }
    }

    override fun onDestroyView() {
        findNavController().navigateUp()
        super.onDestroyView()
    }
}