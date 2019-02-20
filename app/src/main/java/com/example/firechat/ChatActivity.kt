package com.example.firechat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firechat.data.authInstance
import com.example.firechat.models.Message
import com.example.firechat.repositories.MessageRepository
import com.facebook.appevents.AppEventsLogger
import com.example.firechat.services.Datetime
import com.example.firechat.services.checkPermission
import com.example.firechat.services.requestPermissionForWrite
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var adapter: ChatAdapter = ChatAdapter()
    private lateinit var firestoreListener: ListenerRegistration
    private var messageRepository: MessageRepository = MessageRepository()
    private var id: String? = ""

    companion object {
        const val REQUEST_IMAGE = 1
        const val TAG = "ChatActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventsLogger.activateApp(application)
        setContentView(R.layout.activity_chat)

        val actionBar = supportActionBar
        actionBar!!.title = "Chat Messages"

        rv_messages.layoutManager = LinearLayoutManager(this)
        rv_messages.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        rv_messages.itemAnimator = DefaultItemAnimator()
        rv_messages.adapter = adapter

        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey("id")) {
                id = bundle.getString("id")
            }
        }

        loadMessageList()

        firestoreListener = messageRepository.messageListener(id!!).addSnapshotListener(EventListener { documentSnapshots, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed!", e)
                return@EventListener
            }

            val messageList = mutableListOf<Message>()

            for (doc in documentSnapshots!!) {
                val message = doc.toObject(Message::class.java)
                message.id = doc.id
                message.senderName = doc.getString("senderName")
                message.text = doc.getString("text")
                message.date = doc.getDate("date")

                //TODO fill firestor with photoUrl so loadMessageList can show meassages with pictures
                message.photoUrl = doc.getString("photoUrl")
                message.avatarUrl = doc.getString("avatarUrl")
                messageList.add(message)
            }

            adapter.setMessages(messageList)
        })

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1079804979782-biupcn69v8395q4kjvfd46vnqlk4qad3.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btn_send_message.setOnClickListener { sendMessage() }

        imageView_add_image.setOnClickListener {
            if(!checkPermission(applicationContext, 4)){
                requestPermissionForWrite(applicationContext)
            }
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_sign_out -> {
                signOut()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    val uri = data.data
                    //TODO save image in firestore
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        firestoreListener.remove()
    }

    private fun loadMessageList() {
        messageRepository.allMessages(id!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val messageList = mutableListOf<Message>()

                for (doc in task.result!!) {
                    val message = doc.toObject(Message::class.java)
                    message.id = doc.id
                    message.senderName = doc.getString("senderName")
                    message.text = doc.getString("text")
                    message.date = doc.getDate("date")

                    //TODO fill firestore with photoUrl so loadMessageList can show meassages with pictures
                    message.photoUrl = doc.getString("photoUrl")
                    message.avatarUrl = doc.getString("avatarUrl")
                    messageList.add(message)
                }

                adapter.setMessages(messageList)

            } else {
                Log.d(TAG, "Error getting documents: ", task.exception)
            }
        }
    }

    private fun sendMessage() {
        val user = authInstance.currentUser
        val message = HashMap<String, Any>()

        if (user != null) {
            message["id"] = id.toString()
            message["senderName"] = user.displayName.toString()
            message["text"] = edit_message.text
            message["date"] = Datetime()
            message["avatarUrl"] = user.photoUrl.toString()
            message["photoUrl"] = ""
        }

        //TODO få den til at tilføje en message
        messageRepository.addMessage(message)
            .addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.id)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun signOut(){
        val user = authInstance.currentUser
        if (user != null) {
            for (item in user.providerData){
                when (item.providerId) {
                    "google.com" -> {
                        authInstance.signOut()
                        googleSignInClient.signOut()
                    }
                    "facebook.com" -> {
                        authInstance.signOut()
                        LoginManager.getInstance().logOut()
                    }
                }
            }
        }
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }
}
