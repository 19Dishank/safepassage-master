package com.dhruvbuildz.safepassageapp.Fetures

import android.app.Activity
import android.content.Intent
import android.content.Context
import android.view.WindowManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UtilitiesViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object Utils {
    const val REQUEST_CODE_PICK_DOCUMENT = 1001
    fun pickDocument(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        activity.startActivityForResult(
            Intent.createChooser(intent, "Select Document"),
            REQUEST_CODE_PICK_DOCUMENT
        )
    }

    fun showDiscardChangesDialog(
        context: Activity,
        title: String,
        message: String,
        buttonName: String,
        onPositiveClick: () -> Unit
    ) {
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonName) { _, _ -> onPositiveClick() }
            .setNegativeButton("Cancel", null)
            .show()
    }
    fun showPasswordTipsDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Password Tips")
            .setMessage(
                """
                To create a strong password, follow these guidelines:

                1. Use at least 12 characters.
                2. Include uppercase letters (A-Z).
                3. Include lowercase letters (a-z).
                4. Include numbers (0-9).
                5. Include symbols (e.g., @, #, $, %, &).

                Avoid using easily guessable information like your name or common words.
                """.trimIndent()
            )
            .setPositiveButton("Got it") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun showDiscardChangesDialog(
        context: Context, title: String,buttonName: String, message: String, onPositiveClick: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context).setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonName) { dialog, _ ->
                onPositiveClick()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    fun screenshotCheck(utilitiesViewModel: UtilitiesViewModel,owner: LifecycleOwner,activity:Activity){
        utilitiesViewModel.getUtilities().observe(owner, Observer {
            if (it != null) {
                if(!it.isScreenshot){
                    activity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                }
            }
        })
    }

}
