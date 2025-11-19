package com.dhruvbuildz.safepassageapp.UI.Adapter

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Document
import com.dhruvbuildz.safepassageapp.Database.Room.Model.DocumentAIStatus
import com.dhruvbuildz.safepassageapp.Fetures.DocumentManager
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.databinding.DialogAiDetailsBinding
import com.dhruvbuildz.safepassageapp.databinding.DocumentItemLayoutBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

private val DISPLAY_DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())

class DocumentAdapter(
    private val onDocumentClick: (Document) -> Unit,
    private val onDocumentDelete: (Document) -> Unit
) : ListAdapter<Document, DocumentAdapter.DocumentViewHolder>(DocumentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val binding = DocumentItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DocumentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DocumentViewHolder(
        private val binding: DocumentItemLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(document: Document) {
            binding.apply {
                documentTitle.text = document.title
                documentFileName.text = document.fileName
                documentFileSize.text = DocumentManager.formatFileSize(document.fileSize)
                documentCreatedAt.text = document.createdAt
                
                // Set file type icon based on MIME type
                val fileExtension = DocumentManager.getFileExtension(document.fileName)
                when (fileExtension.lowercase()) {
                    "pdf" -> documentFileIcon.setImageResource(android.R.drawable.ic_menu_view)
                    "doc", "docx" -> documentFileIcon.setImageResource(android.R.drawable.ic_menu_edit)
                    "txt" -> documentFileIcon.setImageResource(android.R.drawable.ic_menu_edit)
                    "jpg", "jpeg", "png", "gif" -> documentFileIcon.setImageResource(android.R.drawable.ic_menu_gallery)
                    else -> documentFileIcon.setImageResource(android.R.drawable.ic_menu_agenda)
                }

                updateAiViews(document)

                root.setOnClickListener {
                    onDocumentClick(document)
                }

                deleteButton.setOnClickListener {
                    onDocumentDelete(document)
                }
            }
        }

        private fun DocumentItemLayoutBinding.updateAiViews(document: Document) {
            val statusView = documentAiStatus
            val infoButton = documentAiInfoButton

            statusView.visibility = View.GONE
            infoButton.visibility = View.GONE
            infoButton.setOnClickListener(null)

            when (document.aiStatus) {
                DocumentAIStatus.SUCCESS -> {
                    statusView.visibility = View.VISIBLE
                    statusView.text = buildExpiryLabel(document)

                    if (hasAiDetails(document)) {
                        val context = root.context
                        infoButton.visibility = View.VISIBLE
                        infoButton.text = context.getString(R.string.view_ai_details)
                        infoButton.setOnClickListener {
                            showAiDetailsDialog(
                                context = context,
                                document = document,
                                status = DocumentAIStatus.SUCCESS,
                                overrideSummary = null
                            )
                        }
                    }
                }

                DocumentAIStatus.PENDING -> {
                    statusView.visibility = View.VISIBLE
                    statusView.text = "AI analyzing document…"
                }

                DocumentAIStatus.UNSUPPORTED -> {
                    statusView.visibility = View.VISIBLE
                    statusView.text =
                        document.aiFailureReason ?: "AI unsupported for this file type"
                    document.aiFailureReason?.let { reason ->
                        val context = root.context
                        infoButton.visibility = View.VISIBLE
                        infoButton.text = context.getString(R.string.view_ai_details)
                        infoButton.setOnClickListener {
                            showAiDetailsDialog(
                                context = context,
                                document = document,
                                status = DocumentAIStatus.UNSUPPORTED,
                                overrideSummary = reason
                            )
                        }
                    }
                }

                DocumentAIStatus.FAILED -> {
                    statusView.visibility = View.VISIBLE
                    statusView.text = "AI failed: ${document.aiFailureReason ?: "Unable to read"}"
                    document.aiFailureReason?.let { reason ->
                        val context = root.context
                        infoButton.visibility = View.VISIBLE
                        infoButton.text = context.getString(R.string.view_ai_details)
                        infoButton.setOnClickListener {
                            showAiDetailsDialog(
                                context = context,
                                document = document,
                                status = DocumentAIStatus.FAILED,
                                overrideSummary = reason
                            )
                        }
                    }
                }

                DocumentAIStatus.NOT_REQUESTED, null -> {
                    statusView.visibility = View.GONE
                }

                else -> {
                    statusView.visibility = View.GONE
                }
            }
        }

        private fun buildExpiryLabel(document: Document): String {
            val expiryRaw = document.aiExpiryDate
            if (expiryRaw.isNullOrBlank()) {
                val typeLabel = document.aiDocumentType?.replace('_', ' ')
                return if (!typeLabel.isNullOrBlank()) {
                    "Document type: ${typeLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"
                } else {
                    "AI analysis complete"
                }
            }
            val parsedDate = parseIsoDate(expiryRaw) ?: return "Expires on $expiryRaw"
            val today = LocalDate.now()
            val days = ChronoUnit.DAYS.between(today, parsedDate)
            val formattedDate = parsedDate.format(DISPLAY_DATE_FORMAT)

            return when {
                days < 0 -> "Expired ${-days} days ago ($formattedDate)"
                days == 0L -> "Expires today ($formattedDate)"
                days <= 30 -> "Expires in $days days ($formattedDate)"
                else -> "Expires on $formattedDate"
            }
        }

        private fun parseIsoDate(value: String): LocalDate? {
            return try {
                LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (_: DateTimeParseException) {
                null
            }
        }

        private fun hasAiDetails(document: Document): Boolean {
            return !document.aiSummary.isNullOrBlank() ||
                !document.aiDocumentType.isNullOrBlank() ||
                !document.aiExpiryDate.isNullOrBlank() ||
                document.aiConfidence != null
        }

        private fun showAiDetailsDialog(
            context: Context,
            document: Document,
            status: String,
            overrideSummary: String?
        ) {
            val binding = DialogAiDetailsBinding.inflate(LayoutInflater.from(context))

            val dialog = Dialog(context)
            dialog.setContentView(binding.root)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            binding.titleText.text = context.getString(R.string.ai_details)

            val (statusTextRes, statusColorRes) = when (status) {
                DocumentAIStatus.SUCCESS -> R.string.ai_status_success to R.color.green
                DocumentAIStatus.UNSUPPORTED -> R.string.ai_status_unsupported to R.color.yellow
                DocumentAIStatus.FAILED -> R.string.ai_status_failed to R.color.red
                else -> R.string.ai_status_success to R.color.teal
            }
            binding.statusChip.text = context.getString(statusTextRes)
            ViewCompat.setBackgroundTintList(
                binding.statusChip,
                ColorStateList.valueOf(ContextCompat.getColor(context, statusColorRes))
            )

            // Type Row
            val type = document.aiDocumentType?.takeIf { it.isNotBlank() }?.let { formatType(it) }
            binding.typeRow.isVisible = !type.isNullOrBlank()
            if (binding.typeRow.isVisible) {
                binding.typeChip.text = type
            }

            // Expiry Row
            val expiry = document.aiExpiryDate?.takeIf { it.isNotBlank() }?.let { formatDisplayDate(it) }
            binding.expiryRow.isVisible = !expiry.isNullOrBlank()
            if (binding.expiryRow.isVisible) {
                binding.expiryChip.text = expiry
            }

            // Summary Row
            val summaryText = overrideSummary?.trim()
                ?: document.aiSummary?.trim()
            val hasSummary = !summaryText.isNullOrBlank()
            binding.summaryRow.isVisible = hasSummary
            if (binding.summaryRow.isVisible) {
                binding.summaryText.text = summaryText ?: context.getString(R.string.ai_details_unavailable)
            }

            binding.closeButton.setOnClickListener { dialog.dismiss() }

            dialog.show()
        }

        private fun formatDisplayDate(raw: String): String {
            val parsed = parseIsoDate(raw) ?: return raw
            return parsed.format(DISPLAY_DATE_FORMAT)
        }

        private fun formatType(raw: String): String {
            return raw.replace('_', ' ').replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
    }

    private class DocumentDiffCallback : DiffUtil.ItemCallback<Document>() {
        override fun areItemsTheSame(oldItem: Document, newItem: Document): Boolean {
            return oldItem.documentId == newItem.documentId
        }

        override fun areContentsTheSame(oldItem: Document, newItem: Document): Boolean {
            return oldItem == newItem
        }
    }
}
