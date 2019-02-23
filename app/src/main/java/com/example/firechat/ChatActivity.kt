package com.example.firechat

import android.content.Intent
import android.net.Uri
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
import com.example.firechat.repositories.RoomRepository
import com.example.firechat.services.checkPermission
import com.example.firechat.services.requestPermissionForWrite
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton
import java.io.File
import java.util.*


class ChatActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var adapter: ChatAdapter = ChatAdapter()
    private lateinit var firestoreListener: ListenerRegistration
    private var messageRepository: MessageRepository = MessageRepository()
    private var roomRepository: RoomRepository = RoomRepository()
    private var filePath: Uri? = null
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
        actionBar?.setDisplayHomeAsUpEnabled(true);
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.title = "Chat Messages"

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

        firestoreListener = messageRepository.messageListener.whereEqualTo("id", id!!).orderBy("date").limit(50).addSnapshotListener(EventListener { documentSnapshots, e ->
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
                message.date = doc.getTimestamp("date")?.toDate()
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

        btn_send_message.setOnClickListener { sendMessage("text", "") }

        imageView_add_image.setOnClickListener {
            if(!checkPermission(this, 4)){
                requestPermissionForWrite(this)
            }
            else {
                if (checkPermission(this, 4)) {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    startActivityForResult(intent, REQUEST_IMAGE)
                }
            }
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
            android.R.id.home -> {
                finish()
            }
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
                    filePath = data.data
                    uploadImage()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        firestoreListener.remove()
    }

    private fun uploadImage() {

        // Create the file metadata
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        // Upload file and metadata to the path 'images/mountains.jpg'
        val ref = messageRepository.addImage.child("images/"+ UUID.randomUUID().toString())
        val uploadTask = ref.putFile(filePath!!, metadata)

        // Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            Log.w(TAG, "Upload is $progress% done")
        }.addOnPausedListener {
            Log.w(TAG, "Uploading is paused")
        }.addOnFailureListener { e ->
            Log.w(TAG, "Error uploading image", e)
        }.addOnSuccessListener {
            Log.w(TAG, "Success uploading image")
        }

        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                sendMessage("image", downloadUri.toString())
            } else {

            }
        }
    }

    private fun sendMessage(type: String, imageRoute: String) {
        val user = authInstance.currentUser
        val message = hashMapOf<String, Any>()
        val time = Timestamp.now()

        if (user != null) {
            if (type == "text") {

                if (edit_message.text.toString().trim() != "") {

                    message["id"] = id.toString()
                    message["senderName"] = user.displayName.toString()
                    message["text"] = edit_message.text.toString()
                    message["date"] = time
                    message["avatarUrl"] = user.photoUrl.toString()
                    message["photoUrl"] = ""

                    messageRepository.addMessage.document().set(message)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "")

                            val roomRef = roomRepository.updateRoom.document(id!!)
                            roomRef
                                .update("lastMessage", time)
                                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                                .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }

                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }

                    edit_message.setText("")
                } else {
                    alert("Can't send empty message", "Error") {
                        yesButton { }
                    }.show()
                }
            }
            else if (type == "image")
            {
                message["id"] = id.toString()
                message["senderName"] = user.displayName.toString()
                message["text"] = ""
                message["date"] = time
                message["avatarUrl"] = user.photoUrl.toString()
                message["photoUrl"] = imageRoute

                messageRepository.addMessage.document().set(message)
                    .addOnSuccessListener {
                        Log.d(TAG, "Success adding message")

                        val roomRef = roomRepository.updateRoom.document(id!!)
                        roomRef
                            .update("lastMessage", time)
                            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }

                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                    }
            }
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
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(loginIntent)
    }
}
