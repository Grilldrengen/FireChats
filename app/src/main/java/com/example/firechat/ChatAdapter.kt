package com.example.firechat

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firechat.data.authInstance
import com.example.firechat.models.Message
import com.example.firechat.services.inflate
import com.google.firebase.auth.FacebookAuthProvider
import kotlinx.android.synthetic.main.item_message.view.*


class ChatAdapter(): RecyclerView.Adapter<ChatAdapter.Holder>() {

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

    inner class Holder(private var view: View): RecyclerView.ViewHolder(view) {

        private var holderData: Message? = null
        private val user = authInstance.currentUser
        private var facebookUserId = ""
        private var facebookPhotoUrl = "https://graph.facebook.com/$facebookUserId/picture?type=large"


        fun bind(holderData: Message) {
            this.holderData = holderData
            view.tv_name.text = holderData.senderName
            view.tv_date.text = holderData.date
            view.tv_message.text = holderData.text

            //TODO User should be able to add random picture from phone
            //view.imageView_added_image.setImageResource(android.R.color.transparent)
            view.imageView_added_image.setVisibility(View.INVISIBLE);

            if (user != null) {

                val profile = user.providerData

                for (item in profile) {

                    //Google
                    if (item.providerId == "google.com") {
                        val googlePhotoUrl = user.photoUrl
                        Glide.with(itemView.context).load(googlePhotoUrl)
                            .into(view.imageView_avatar)
                    }
                    else if (item.providerId == "facebook.com") {
                        //Facebook
                        for (items in profile) {
                            if (FacebookAuthProvider.PROVIDER_ID.equals(item.getProviderId())) {
                                facebookUserId = item.getUid()
                            }
                        }
                        Glide.with(itemView.context).load(facebookPhotoUrl)
                            .into(view.imageView_avatar)
                    }
                }
            }
            else {
                view.imageView_avatar.setImageResource(android.R.color.transparent)
            }
        }
    }
}