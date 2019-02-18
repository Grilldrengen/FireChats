package com.example.firechat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.models.Room
import kotlinx.android.synthetic.main.item_room.view.*

class ChatroomAdapter(private val context: Context, private val rooms: MutableList<Room>): RecyclerView.Adapter<ChatroomAdapter.Holder>() {
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val itemData = rooms[position]
        holder.bind(itemData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflatedView = parent.inflate(R.layout.item_room, false)
        return Holder(inflatedView)
    }

    override fun getItemCount() = rooms.size

    inner class Holder(private var view: View): RecyclerView.ViewHolder(view) {

        private var holderData: Room? = null

        init {
            view.chevron_imageView.setOnClickListener { onImageClick() }
        }

        fun bind(holderData: Room) {
            this.holderData = holderData
            view.name_tv.text = holderData.name
            view.desc_tv.text = holderData.description
            view.chevron_imageView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_chevron_right_black_24dp))

        }

        private fun onImageClick() {
            //itemView.context.startActivity<ChatActivity>("id" to holderData?.id)
            Toast.makeText(context, "This is my Image message!",
                Toast.LENGTH_LONG).show();

        }
    }

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }
}