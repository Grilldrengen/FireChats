package com.example.firechat

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firechat.data.authInstance
import com.example.firechat.models.Message
import com.example.firechat.services.datetime
import com.example.firechat.services.inflate
import kotlinx.android.synthetic.main.item_message.view.*
import android.provider.MediaStore
import android.graphics.Bitmap
import android.net.Uri
import java.net.URI
import com.google.common.collect.TreeTraverser.using




class ChatAdapter() : RecyclerView.Adapter<ChatAdapter.Holder>() {

    private var messages = emptyList<Message>()

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val itemData = messages[position]
        holder.bind(itemData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflatedView = parent.inflate(R.layout.item_message, false)
        return Holder(inflatedView)
    }

    override fun getItemCount() = messages.size

    internal fun setMessages(messages: List<Message>) {
        this.messages = messages
        notifyDataSetChanged()
    }

    inner class Holder(private var view: View) : RecyclerView.ViewHolder(view) {

        private var holderData: Message? = null
        private val user = authInstance.currentUser


        fun bind(holderData: Message) {
            this.holderData = holderData
            view.tv_name.text = holderData.senderName
            view.tv_date.text = datetime(holderData.date)
            view.tv_message.text = holderData.text

            if (!holderData.photoUrl.isNullOrBlank())
            {
                Glide.with(itemView.context)
                    .load(holderData.photoUrl)
                    .into(view.imageView_added_image)
            }
            else {
                view.imageView_added_image.visibility = View.INVISIBLE
            }

            if (user != null) {

                val profile = user.providerData
                for (item in profile) {
                    if (item.providerId == "google.com") {
                        //Google
                        val googlePhotoUrl = holderData.avatarUrl
                        Glide.with(itemView.context).load(googlePhotoUrl)
                            .into(view.imageView_avatar)
                    } else if (item.providerId == "facebook.com") {
                        //Facebook
                        Glide.with(itemView.context).load(holderData.avatarUrl)
                            .into(view.imageView_avatar)
                    }
                }
            } else {
                view.imageView_avatar.setImageResource(android.R.color.transparent)
            }
        }
    }
}