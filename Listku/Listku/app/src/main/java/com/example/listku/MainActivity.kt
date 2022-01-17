package com.example.listku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.listku_content.view.*
import listku.R

class MainActivity : AppCompatActivity() {

    private val database = Firebase.database
    private lateinit var messagesListener: ValueEventListener
    private val listListku:MutableList<Listku> = ArrayList()
    val myRef = database.getReference("listku")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        newFloatingActionButton.setOnClickListener { v ->
            val intent = Intent(this, AddActivity::class.java)
            v.context.startActivity(intent)
        }

        listListku.clear()
        setupRecyclerView(listkuRecyclerView)

    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {

        messagesListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listListku.clear()
                dataSnapshot.children.forEach { child ->
                    val listku: Listku? =
                            Listku(child.child("name").getValue<String>(),
                                    child.child("date").getValue<String>(),
                                    child.child("description").getValue<String>(),
                                    child.child("url").getValue<String>(),
                                    child.key)
                    listku?.let { listListku.add(it) }
                }
                recyclerView.adapter = ListkuViewAdapter(listListku)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }
        }
        myRef.addValueEventListener(messagesListener)

        deleteSwipe(recyclerView)
    }

    class ListkuViewAdapter(private val values: List<Listku>) :
        RecyclerView.Adapter<ListkuViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.listku_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val listku = values[position]
            holder.mNameTextView.text = listku.name
            holder.mDateTextView.text = listku.date
            holder.mPosterImgeView?.let {
                Glide.with(holder.itemView.context)
                    .load(listku.url)
                    .into(it)
            }

            // klik sekali untuk buka menu detail
            holder.itemView.setOnClickListener { v ->
                val intent = Intent(v.context, ListkuDetail::class.java).apply {
                    putExtra("key", listku.key)
                }
                v.context.startActivity(intent)
            }

            // tekan lama untuk buka menu edit
            holder.itemView.setOnLongClickListener{ v ->
                val intent = Intent(v.context, EditActivity::class.java).apply {
                    putExtra("key", listku.key)
                }
                v.context.startActivity(intent)
                true
            }

        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val mNameTextView: TextView = view.nameTextView
            val mDateTextView: TextView = view.dateTextView
            val mPosterImgeView: ImageView? = view.posterImgeView
        }
    }

    // hapus data jika swipe ke kanan atau kiri
    private fun deleteSwipe(recyclerView: RecyclerView){
        val touchHelperCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                listListku.get(viewHolder.adapterPosition).key?.let { myRef.child(it).setValue(null) }
                listListku.removeAt(viewHolder.adapterPosition)
                recyclerView.adapter?.notifyItemRemoved(viewHolder.adapterPosition)
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

}

