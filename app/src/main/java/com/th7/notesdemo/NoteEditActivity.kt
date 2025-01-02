package com.th7.notesdemo

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.th7.notesdemo.databinding.ActivityNoteEditBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditBinding
    private var note: Note? = null
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val noteJson = intent.getStringExtra("note")
        note = if (noteJson != null) {
            gson.fromJson(noteJson, Note::class.java)
        } else {
            null
        }

        note?.let {
            binding.titleEditText.setText(it.title)
            binding.contentEditText.setText(it.content)
            binding.authorEditText.setText(it.author)
            binding.imageEditText.setText(it.image)
        }

        binding.saveButton.setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote() {
        val title = binding.titleEditText.text.toString()
        val content = binding.contentEditText.text.toString()
        val author = binding.authorEditText.text.toString()
        val image = binding.imageEditText.text.toString()

        if (title.isEmpty() || content.isEmpty() || author.isEmpty()) {
            Toast.makeText(this, "标题、内容和作者不能为空", Toast.LENGTH_SHORT).show()
            return
        }

        val api = NoteApi.create()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = sdf.format(Date())

        val newNote = Note(
            id = note?.id ?: 0,
            title = title,
            content = content,
            author = author,
            image = if (image.isNotEmpty()) image else null,
            createdAt = note?.createdAt ?: currentDate,
            updateAt = currentDate,
            deleted = 0
        )

        val call = if (note == null) {
            api.addNote(newNote)
        } else {
            api.updateNote(newNote)
        }

        call.enqueue(object : Callback<ApiResponse<Boolean>> {
            override fun onResponse(
                call: Call<ApiResponse<Boolean>>,
                response: Response<ApiResponse<Boolean>>
            ) {
                if (response.isSuccessful) {
                    if (response.body()?.data == true) {
                        Toast.makeText(this@NoteEditActivity, "保存成功", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@NoteEditActivity, "保存失败", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@NoteEditActivity, "保存失败", Toast.LENGTH_SHORT).show()
                    Log.e("NoteEditActivity", "Failed to save note: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<Boolean>>, t: Throwable) {
                Toast.makeText(this@NoteEditActivity, "保存失败", Toast.LENGTH_SHORT).show()
                Log.e("NoteEditActivity", "Error saving note", t)
            }
        })
    }
}
