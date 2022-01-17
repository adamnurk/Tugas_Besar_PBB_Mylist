package com.example.listku

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_edit.*
import listku.R

class EditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val key = intent.getStringExtra("key")
        val database = Firebase.database
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") val myRef = database.getReference("listku").child(
            key.toString()
        )

        myRef.addValueEventListener(object : ValueEventListener {
            // read
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val listku:Listku? = dataSnapshot.getValue(Listku::class.java)
                if (listku != null) {
                    nameEditText.text = Editable.Factory.getInstance().newEditable(listku.name)
                    dateEditText.text = Editable.Factory.getInstance().newEditable(listku.date)
                    descriptionEditText.text = Editable.Factory.getInstance().newEditable(listku.description)
                    urlEditText.text = Editable.Factory.getInstance().newEditable(listku.url)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        // update
        saveButton.setOnClickListener { v ->

            val name : String = nameEditText.text.toString()
            val date : String = dateEditText.text.toString()
            val description: String = descriptionEditText.text.toString()
            val url: String = urlEditText.text.toString()

            myRef.child("name").setValue(name)
            myRef.child("date").setValue(date)
            myRef.child("description").setValue(description)
            myRef.child("url").setValue(url)

            finish()
        }
    }

}