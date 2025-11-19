package com.dhruvbuildz.safepassageapp.UI.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Category
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CategoryViewModel
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.UI.Home.MainActivity
import com.dhruvbuildz.safepassageapp.databinding.CategoryBtnLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class CategoryAdapter(
    val context: Context,
    private val onCategoryClick: (Category) -> Unit,
    private val categoryViewModel: CategoryViewModel
) :
    RecyclerView.Adapter<CategoryAdapter.categoryViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION
    private var recentlyDeletedCategory: Category? = null

    class categoryViewHolder(val itemBinding: CategoryBtnLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

    }

    private val differCallback = object : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.catId == newItem.catId
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): categoryViewHolder {
        return categoryViewHolder(
            CategoryBtnLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: categoryViewHolder, position: Int) {
        val currentCategory = differ.currentList[position]

        holder.itemBinding.catNameTxt.text = currentCategory.catName

        val isSelected = selectedPosition == position
        holder.itemView.isSelected = isSelected
        holder.itemBinding.catNameTxt.setTextAppearance(
            if (isSelected) R.style.SelectedBtnText else R.style.DeselectedBtnText
        )
        holder.itemView.setBackgroundResource(
            if (isSelected) R.drawable.setecled_button else R.drawable.deselected_button
        )

        holder.itemView.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = holder.adapterPosition


            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)

            onCategoryClick(currentCategory)
        }

        holder.itemView.setOnLongClickListener {
            showCategoryOptionsDialog(currentCategory, currentCategory.catName)
            true
        }
    }


    fun clearSelection() {
        val previousSelectedPosition = selectedPosition
        selectedPosition = RecyclerView.NO_POSITION
        notifyItemChanged(previousSelectedPosition)
    }

    private fun showCategoryOptionsDialog(category: Category, catName: String) {
        val options = arrayOf("Edit", "Delete")

        MaterialAlertDialogBuilder(context).apply {
            setTitle("Select Option")
            setItems(options) { dialog, which ->
                when (which) {
                    0 -> editCategoryDialog(category)  // Edit option
                    1 -> confirmDeleteCategoryDialog(category, catName)  // Delete option
                }
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    private fun confirmDeleteCategoryDialog(category: Category, catName: String) {
        MaterialAlertDialogBuilder(context).apply {
            setTitle("Delete Category")
            setMessage(
                "Are you sure you want to delete this category?"
                        + "\nThis action will delete tasks of " + catName + " and cannot be undone."

            )
            setPositiveButton("Delete") { _, _ ->
                recentlyDeletedCategory = category
                categoryViewModel.deleteCategory(category)
                showUndoSnackbar()
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    private fun editCategoryDialog(category: Category) {
        val editTextLy = LayoutInflater.from(context).inflate(R.layout.edittextbox, null)
        val txtBox = editTextLy.findViewById<TextInputEditText>(R.id.editTxt)
        txtBox.setText(category.catName)

        MaterialAlertDialogBuilder(context).apply {
            setTitle("Edit Category")
            setView(editTextLy)
            setPositiveButton("Save") { _, _ ->
                val updatedCategory = category.copy(catName = txtBox.text.toString())
                categoryViewModel.updateCategory(updatedCategory)
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    private fun showUndoSnackbar() {
        Snackbar.make(
            (context as MainActivity).findViewById(android.R.id.content),
            "Category deleted",
            Snackbar.LENGTH_LONG
        ).setAction("Undo") {
            recentlyDeletedCategory?.let { categoryViewModel.insertCategory(it) }
        }.show()
    }

}