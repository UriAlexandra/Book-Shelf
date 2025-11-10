package com.example.bookshelf

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DialogBook(
    private val mode: Mode,
    private val onCreated: (List<BookEntity>) -> Unit = {},
    private val onUpdated: (BookEntity) -> Unit = {}
) : DialogFragment() {

    sealed class Mode {
        object CREATE : Mode()
        data class EDIT(val book: BookEntity) : Mode()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = requireActivity().layoutInflater.inflate(R.layout.dialog_book, null)

        val etTitle = v.findViewById<EditText>(R.id.et_Title)
        val etAuthor = v.findViewById<EditText>(R.id.et_Author)
        val spGenre = v.findViewById<Spinner>(R.id.spinner_Genre)
        val etYear = v.findViewById<EditText>(R.id.et_Year)

        val genres = listOf("Regény", "Sci-fi", "Fantasy", "Történelem", "Ismeretterjesztő")
        spGenre.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genres)

        when (mode) {
            is Mode.CREATE -> {
                // semmi extra – nincs mennyiség
            }
            is Mode.EDIT -> {
                etTitle.setText(mode.book.title)
                etAuthor.setText(mode.book.author)
                val idx = genres.indexOfFirst { it.equals(mode.book.genre, ignoreCase = true) }
                spGenre.setSelection(if (idx >= 0) idx else 0)
                etYear.setText(mode.book.year.toString())
            }
        }

        val dialogTitle = if (mode is Mode.CREATE) "Könyv hozzáadása" else "Könyv szerkesztése"
        val positive = if (mode is Mode.CREATE) "Mentés" else "Frissítés"

        return AlertDialog.Builder(requireContext())
            .setTitle(dialogTitle)
            .setView(v)
            .setPositiveButton(positive) { _, _ ->
                val titleText = etTitle.text.toString().trim()
                val authorText = etAuthor.text.toString().trim()
                val genreText = spGenre.selectedItem?.toString()?.trim().orEmpty()
                val yearVal = etYear.text.toString().toIntOrNull() ?: 0

                if (titleText.isEmpty() || authorText.isEmpty() || genreText.isEmpty() || yearVal <= 0) {
                    return@setPositiveButton
                }

                val appCtx = requireContext().applicationContext

                when (mode) {
                    is Mode.CREATE -> {
                        // Csak 1 példányt szúrunk be
                        val newBook = BookEntity(
                            title = titleText,
                            author = authorText,
                            genre = genreText,
                            year = yearVal
                        )

                        lifecycleScope.launch {
                            val createdOne: BookEntity = withContext(Dispatchers.IO) {
                                val dao = AppDatabase.get(appCtx).bookDao()

                                // Ha ragaszkodsz a meglévő DAO-hoz (insertAll):
                                val ids = dao.insertAll(listOf(newBook))
                                newBook.copy(id = ids.first().toInt())

                                // Ha inkább külön @Insert suspend fun insert(book: BookEntity): Long lenne a DAO-ban,
                                // akkor ezt lehetne használni helyette:
                                // val id = dao.insert(newBook)
                                // newBook.copy(id = id.toInt())
                            }
                            if (!isAdded) return@launch
                            onCreated(listOf(createdOne))
                        }
                    }

                    is Mode.EDIT -> {
                        val updated = mode.book.copy(
                            title = titleText,
                            author = authorText,
                            genre = genreText,
                            year = yearVal
                        )

                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                AppDatabase.get(appCtx).bookDao().update(updated)
                            }
                            if (!isAdded) return@launch
                            onUpdated(updated)
                        }
                    }
                }
            }
            .setNegativeButton("Mégse", null)
            .create()
    }
}
