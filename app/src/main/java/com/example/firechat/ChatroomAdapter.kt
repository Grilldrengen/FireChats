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

// Recyclerview used to display messages with either picture or text
class ChatroomAdapter: RecyclerView.Adapter<ChatroomAdapter.Holder>() {

    private var rooms = emptyList<Room>()

    // Calls inner class Holder where detailed binding between data and views happens
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val itemData = rooms[position]
        holder.bind(itemData)
    }

    // Pass the XML layout created to be inflated with data from list of messages
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflatedView = parent.inflate(R.layout.item_room, false)
        return Holder(inflatedView)
    }

    override fun getItemCount() = rooms.size

    // Called whenever an update in data is registered so list is up to date
    // notifyDataSetChanged() is called to make sure the adapter updates the list with the new data
    internal fun setRooms(rooms: List<Room>) {
        this.rooms = rooms
        notifyDataSetChanged()
    }

    // This is the inner class handling the binding between view and data
    inner class Holder(private var view: View): RecyclerView.ViewHolder(view) {

        private var holderData: Room? = null

        //Creates onClickEvent for the image so user can open the chat rooms
        init {
            view.chevron_imageView.setOnClickListener { onImageClick() }
        }

        // This is where the binding happens
        fun bind(holderData: Room) {
            this.holderData = holderData
            view.name_tv.text = holderData.name
            view.desc_tv.text = holderData.description
            view.chevron_imageView.setImageDrawable(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_chevron_right_black_24dp))

        }

        //Opens the ChatActivity with needed parameters from selected item
        private fun onImageClick() {
            itemView.context.startActivity<ChatActivity>("id" to holderData?.id, "name" to holderData?.name)
        }
    }
}