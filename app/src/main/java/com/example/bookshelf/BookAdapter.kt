package com.example.bookshelf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Egyszerű adapter: lista + edit / read-toggle / delete callback
class BookAdapter(
    private var books: List<BookEntity>,
    private val onEdit: (BookEntity, Int) -> Unit,
    private val onToggleRead: (BookEntity, Int) -> Unit,
    private val onDelete: (BookEntity, Int) -> Unit          // <<< ÚJ: törlés callback
) : RecyclerView.Adapter<BookAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.findViewById(R.id.tv_Title)
        val tvAuthor: TextView = v.findViewById(R.id.tv_Author)
        val tvMeta: TextView = v.findViewById(R.id.tv_Meta)       // műfaj + év
        val ivStatus: ImageView = v.findViewById(R.id.iv_Status)  // olvasott ikon
        val btnEdit: ImageButton = v.findViewById(R.id.btn_Edit)  // ceruza
        val btnDelete: ImageButton = v.findViewById(R.id.btn_Delete) // <<< ÚJ: törlés gomb
    }

    // Teljes lista cseréje (pl. DB újra-lekérdezés után)
    fun updateList(newList: List<BookEntity>) {
        books = newList
        notifyDataSetChanged()
    }

    // Egy elem frissítése adott indexen
    fun replaceAt(index: Int, updated: BookEntity) {
        if (index in books.indices) {
            val m = books.toMutableList()
            m[index] = updated
            books = m
            notifyItemChanged(index)
        }
    }

    // <<< Opcionális: helyben törlés segédfüggvény (ha nem kívül intézed)
    fun removeAt(index: Int) {
        if (index in books.indices) {
            val m = books.toMutableList()
            m.removeAt(index)
            books = m
            notifyItemRemoved(index)
            // Ha szeretnél pozíciófrissítést a RecyclerView-nak:
            notifyItemRangeChanged(index, books.size - index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val b = books[position]

        // --- Bind UI ---
        holder.tvTitle.text = b.title
        holder.tvAuthor.text = b.author
        holder.tvMeta.text = "${b.genre} • ${b.year}"

        holder.ivStatus.setImageResource(
            if (b.isRead) android.R.drawable.checkbox_on_background
            else android.R.drawable.checkbox_off_background
        )

        // --- Clickek ---
        holder.btnEdit.setOnClickListener {
            val idx = holder.bindingAdapterPosition
            if (idx != RecyclerView.NO_POSITION) onEdit(b, idx)
        }

        holder.ivStatus.setOnClickListener {
            val idx = holder.bindingAdapterPosition
            if (idx != RecyclerView.NO_POSITION) onToggleRead(b, idx)
        }

        // <<< LÉNYEG: TÖRLÉS GOMB
        holder.btnDelete.setOnClickListener {
            val idx = holder.bindingAdapterPosition
            if (idx != RecyclerView.NO_POSITION) onDelete(b, idx)
        }
    }

    override fun getItemCount() = books.size
}
