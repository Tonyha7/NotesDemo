package com.th7.notesdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.th7.notesdemo.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    private val NOTE_REQUEST = 1
    private val gson = Gson()
    private var allNotes: List<Note> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchNotes()

        binding.addNoteButton.setOnClickListener {
            val intent = Intent(this, NoteEditActivity::class.java)
            startActivityForResult(intent, NOTE_REQUEST)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterNotes(newText)
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(this, mutableListOf(),
            onNoteLongClick = { note ->
                showNoteOptions(note)
            })
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = noteAdapter
        }
    }

    private fun fetchNotes() {
        val api = NoteApi.create()
        api.getNotes().enqueue(object : Callback<ApiResponse<List<Note>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Note>>>,
                response: Response<ApiResponse<List<Note>>>
            ) {
                if (response.isSuccessful) {
                    val notes = response.body()?.data
                    notes?.let {
                        allNotes = it
                        updateNoteList(it)
                    }
                } else {
                    Log.e("MainActivity", "Failed to fetch notes: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Note>>>, t: Throwable) {
                Log.e("MainActivity", "Error fetching notes", t)
            }
        })
    }

    private fun filterNotes(query: String?) {
        val filteredNotes = if (query.isNullOrEmpty()) {
            allNotes
        } else {
            allNotes.filter { note ->
                note.title.contains(query, ignoreCase = true)
            }
        }
        updateNoteList(filteredNotes)
    }

    private fun updateNoteList(notes: List<Note>) {
        noteAdapter.updateNotes(notes)
    }

    private fun showNoteOptions(note: Note) {
        val options = arrayOf("编辑", "删除")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("选择操作")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editNote(note)
                    1 -> deleteNote(note)
                }
            }
            .show()
    }

    private fun editNote(note: Note) {
        val intent = Intent(this, NoteEditActivity::class.java)
        val noteJson = gson.toJson(note)
        intent.putExtra("note", noteJson)
        startActivityForResult(intent, NOTE_REQUEST)
    }

    private fun deleteNote(note: Note) {
        val api = NoteApi.create()
        api.deleteNote(note.id).enqueue(object : Callback<ApiResponse<Boolean>> {
            override fun onResponse(
                call: Call<ApiResponse<Boolean>>,
                response: Response<ApiResponse<Boolean>>
            ) {
                if (response.isSuccessful) {
                    if (response.body()?.data == true) {
                        Toast.makeText(this@MainActivity, "删除成功", Toast.LENGTH_SHORT).show()
                        fetchNotes()
                    } else {
                        Toast.makeText(this@MainActivity, "删除失败", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "删除失败", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "Failed to delete note: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<Boolean>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "删除失败", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Error deleting note", t)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NOTE_REQUEST && resultCode == Activity.RESULT_OK) {
            fetchNotes()
        }
    }
}
