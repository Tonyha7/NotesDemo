package com.th7.notesdemo

import android.content.Context
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.th7.notesdemo.databinding.ItemNoteBinding
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

class NoteAdapter(
    private val context: Context,
    private var notes: MutableList<Note>,
    private val onNoteLongClick: (Note) -> Unit
) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.binding.titleTextView.text = note.title
        //holder.binding.contentTextView.text = note.content
        val parser = Parser.builder().build()
        val document: Node = parser.parse(note.content)
        val renderer = HtmlRenderer.builder().build()
        val html: String = renderer.render(document)
        val spanned: Spanned =
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        holder.binding.contentTextView.text = spanned

        holder.binding.authorTextView.text = note.author

        if (!note.image.isNullOrEmpty()) {
            Glide.with(context)
                .load(note.image)
                .into(holder.binding.imageView)
        } else {
            holder.binding.imageView.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.itemView.setOnLongClickListener {
            onNoteLongClick(note)
            true
        }
    }

    override fun getItemCount(): Int = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }
}
