package com.example.firechat

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.models.Room
import com.example.firechat.services.inflate
import kotlinx.android.synthetic.main.item_room.view.*
import org.jetbrains.anko.startActivity

class ChatroomAdapter(): RecyclerView.Adapter<ChatroomAdapter.Holder>() {

    private var rooms = emptyList<Room>()

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val itemData = rooms[position]
        holder.bind(itemData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflatedView = parent.inflate(R.layout.item_room, false)
        return Holder(inflatedView)
    }

    override fun getItemCount() = rooms.size

    internal fun setRooms(rooms: List<Room>) {
        this.rooms = rooms
        notifyDataSetChanged()
    }

    inner class Holder(private var view: View): RecyclerView.ViewHolder(view) {

        private var holderData: Room? = null

        init {
            view.chevron_imageView.setOnClickListener { onImageClick() }
        }

        fun bind(holderData: Room) {
            this.holderData = holderData
            view.name_tv.text = holderData.name
            view.desc_tv.text = holderData.description
            view.chevron_imageView.setImageDrawable(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_chevron_right_black_24dp))

        }

        private fun onImageClick() {
            itemView.context.startActivity<ChatActivity>("id" to holderData?.id, "name" to holderData?.name)
        }
    }
}