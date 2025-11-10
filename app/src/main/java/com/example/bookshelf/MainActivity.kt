package com.example.bookshelf

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import androidx.appcompat.widget.SearchView

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: BookAdapter
    private val books = mutableListOf<BookEntity>() // forráslista (aktuális állapot)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Rendszersávok miatti padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, ins ->
            val bars = ins.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            ins
        }

        val db = AppDatabase.get(this)
        val dao = db.bookDao()

        // RecyclerView alapok
        recycler = findViewById(R.id.recycler_View)
        recycler.layoutManager = LinearLayoutManager(this)

        // Adapter példányosítás – EDIT / READ-TOGGLE / DELETE callbackekkel
        adapter = BookAdapter(
            books,
            onEdit = { book, idxInAdapter ->
                // Szerkesztés dialógus
                DialogBook(
                    DialogBook.Mode.EDIT(book),
                    onUpdated = { updated ->
                        // Forráslista frissítése
                        val posInSource = books.indexOfFirst { it.id == updated.id }
                        if (posInSource != -1) books[posInSource] = updated
                        // Adapterben az adott pozíció frissítése
                        adapter.replaceAt(idxInAdapter, updated)
                        Snackbar.make(recycler, "Könyv frissítve", Snackbar.LENGTH_SHORT).show()
                    }
                ).show(supportFragmentManager, "editBook")
            },
            onToggleRead = { book, idx ->
                // Olvasottság kapcsolása
                val updated = book.copy(isRead = !book.isRead)
                lifecycleScope.launch {
                    dao.update(updated)
                    val posInSource = books.indexOfFirst { it.id == updated.id }
                    if (posInSource != -1) books[posInSource] = updated
                    adapter.replaceAt(idx, updated)
                }
            },
            onDelete = { book, idx ->
                // <<< TÖRLÉS GOMB KEZELÉSE
                lifecycleScope.launch {
                    // 1) Törlés DB-ből
                    dao.delete(book)
                    // 2) Forráslista frissítése
                    val posInSource = books.indexOfFirst { it.id == book.id }
                    if (posInSource != -1) {
                        books.removeAt(posInSource)
                    }
                    // 3) Adapter frissítése (teljes lista újraadása vagy removeAt, ha van)
                    adapter.updateList(books)
                    Snackbar.make(recycler, "Könyv törölve", Snackbar.LENGTH_SHORT).show()
                }
            }
        )
        recycler.adapter = adapter

        // Kezdeti betöltés DB-ből
        lifecycleScope.launch {
            val all = dao.getAll()
            books.clear()
            books.addAll(all)
            adapter.updateList(books)
        }

        // FAB – CREATE (új könyv(ek) felvétele)
        findViewById<FloatingActionButton>(R.id.fab_Add).setOnClickListener {
            DialogBook(
                DialogBook.Mode.CREATE,
                onCreated = { created ->
                    books.addAll(created)
                    adapter.updateList(books)
                    recycler.scrollToPosition(books.lastIndex)
                    Snackbar.make(
                        recycler,
                        "${created.size} könyv hozzáadva",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            ).show(supportFragmentManager, "createBook")
        }
    }
}



