package com.example.firechat

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.firechat.data.authInstance
import com.example.firechat.models.Message
import com.example.firechat.services.datetime
import com.example.firechat.services.inflate
import kotlinx.android.synthetic.main.item_message.view.*

// Recyclerview used to display messages with either picture or text
class ChatAdapter : RecyclerView.Adapter<ChatAdapter.Holder>() {

    private var messages = emptyList<Message>()

    // Calls inner class Holder where detailed binding between data and views happens
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val itemData = messages[position]
        holder.bind(itemData)
    }

    // Pass the XML layout created to be inflated with data from list of messages
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflatedView = parent.inflate(R.layout.item_message, false)
        return Holder(inflatedView)
    }

    override fun getItemCount() = messages.size

    // Called whenever an update in data is registered so list is up to date
    // notifyDataSetChanged() is called to make sure the adapter updates the list with the new data
    internal fun setMessages(messages: List<Message>) {
        this.messages = messages
        notifyDataSetChanged()
    }

    // This is the inner class handling the binding between view and data
    inner class Holder(private var view: View) : RecyclerView.ViewHolder(view) {

        private var holderData: Message? = null
        private val user = authInstance.currentUser

        // This is where the binding happens
        fun bind(holderData: Message) {
            this.holderData = holderData
            view.tv_name.text = holderData.senderName
            view.tv_date.text = datetime(holderData.date)
            view.tv_message.text = holderData.text

            // Checks if photoUrl is null or blank. If not the photoUrl is used with Glide to donwload the image from firebase storage and placed in a imageview
            // Else the imageview is set to GONE which woulc meen the user have send at textmessage and not a image to the chat.
            if (!holderData.photoUrl.isNullOrBlank()) {
                val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).fitCenter()
                Glide.with(itemView.context)
                    .load(holderData.photoUrl)
                    .apply(requestOptions)
                    .into(view.imageView_added_image)
                view.imageView_added_image.visibility = View.VISIBLE
            } else {
                view.imageView_added_image.visibility = View.GONE
            }

            // Checks if authenticated user is null. If not we use avatarUrl to download the users social media picture and display it in the avatar picture imageview
            // Else we set it to transparent. We could also use a default image like facebook does.
            if (user != null) {
                Glide.with(itemView.context).load(holderData.avatarUrl)
                    .into(view.imageView_avatar)

            } else {
                view.imageView_avatar.setImageResource(android.R.color.transparent)
            }
        }
    }
}