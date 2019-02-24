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
import com.example.firechat.models.Message.Companion.AVATARURL
import com.example.firechat.models.Message.Companion.DATE
import com.example.firechat.models.Message.Companion.ID
import com.example.firechat.models.Message.Companion.PHOTOURL
import com.example.firechat.models.Message.Companion.SENDERNAME
import com.example.firechat.models.Message.Companion.TEXT
import com.example.firechat.models.Room.Companion.NAME
import com.example.firechat.repositories.MessageRepository
import com.example.firechat.repositories.RoomRepository
import com.example.firechat.services.Constants
import com.example.firechat.services.Constants.Companion.FACEBOOK_LINK
import com.example.firechat.services.Constants.Companion.GOOGLE_LINK
import com.example.firechat.services.checkPermission
import com.example.firechat.services.requestPermissionForRead
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
import java.util.*


class ChatActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var adapter: ChatAdapter = ChatAdapter()
    private lateinit var firestoreListener: ListenerRegistration
    private var messageRepository: MessageRepository = MessageRepository()
    private var roomRepository: RoomRepository = RoomRepository()
    private var filePath: Uri? = null
    private var id: String? = ""
    private var name: String? = ""

    companion object {
        const val REQUEST_IMAGE = 1
        const val TAG = "ChatActivity"
        const val TEXT = "text"
        const val IMAGE = "image"
        const val CONTENT_TYPE = "image/jpeg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventsLogger.activateApp(application)
        setContentView(R.layout.activity_chat)

        //Receive bundle data from the ChatroomActvity where this activity is called from
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ID)) {
                id = bundle.getString(ID)
                name = bundle.getString(NAME)
            }
        }

        //Sets up the actionbar. This secures there is a visible back button to use on newer phones.
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true);
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.title = name

        //Sets up the recylcerview. Decides the item animations, the divider decoration and sets the adapter
        rv_messages.layoutManager = LinearLayoutManager(this)
        rv_messages.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        rv_messages.itemAnimator = DefaultItemAnimator()
        rv_messages.adapter = adapter

        //This listener fills out the recylcerview when the user enters the list of rooms.
        //The listener also makes sure to listen to updates in the data. This ensures the list is up to date all the time.
        firestoreListener = messageRepository.messageListener.whereEqualTo(ID, id!!).orderBy(DATE).limit(50).addSnapshotListener(EventListener { documentSnapshots, e ->
            if (e != null) {
                Log.e(TAG, this.getString(R.string.listen_failed), e)
                return@EventListener
            }

            val messageList = mutableListOf<Message>()

            for (doc in documentSnapshots!!) {
                val message = doc.toObject(Message::class.java)
                message.id = doc.id
                message.senderName = doc.getString(SENDERNAME)
                message.text = doc.getString(TEXT)
                message.date = doc.getTimestamp(DATE)?.toDate()
                message.photoUrl = doc.getString(PHOTOURL)
                message.avatarUrl = doc.getString(AVATARURL)
                messageList.add(message)
            }

            adapter.setMessages(messageList)
        })

        //This is build so we can sign out of google from the actionbar menu when needed.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(Constants.GOOGLE_TOKEN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //Calls the sendMessage method and pass it the type of message it is and a imageroute if it is an image message
        btn_send_message.setOnClickListener { sendMessage(TEXT, "") }

        //Check for persmissoin on whether or not we can use the phones gallery. If yes we start the intent to open it. If not we do nothing.
        imageView_add_image.setOnClickListener {
            if(checkPermission(this, 4)){
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_IMAGE)
            }
            else {
                requestPermissionForRead(this)
            }
        }
    }

    // Inflate the menu to use in the action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //Handles presses on the action bar menu items. Generates the back button on the actionbar and also gives the option to sign out
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

    //Handles the result after starting the Gallery intent where user can pick an image to upload
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

    //We unsubscribe from the firestorelistener here to makes sure there is no memory leak
    override fun onDestroy() {
        super.onDestroy()

        firestoreListener.remove()
    }

    //Uploads the image to firebase storage and gets a reference to use later when downloading the image from the storage
    private fun uploadImage() {

        // Create the file metadata
        val metadata = StorageMetadata.Builder()
            .setContentType(CONTENT_TYPE)
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
                sendMessage(IMAGE, downloadUri.toString())
            } else {

            }
        }
    }

    //This method is called when user either clicks on the send button or clicks on the add image imageview
    private fun sendMessage(type: String, imageRoute: String) {
        val user = authInstance.currentUser
        val message = hashMapOf<String, Any>()
        val time = Timestamp.now()

        if (user != null) {
            if (type == TEXT) {

                if (edit_message.text.toString().trim() != "") {

                    //Fills the message object with data before saving it to the cloud store
                    message[ID] = id.toString()
                    message[SENDERNAME] = user.displayName.toString()
                    message[TEXT] = edit_message.text.toString()
                    message[DATE] = time
                    message[AVATARURL] = user.photoUrl.toString()
                    message[PHOTOURL] = ""

                    //Adds text message to the cloud store
                    messageRepository.addMessage.document().set(message)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "")

                            //Updates the date of the rooms latest message
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
            else if (type == IMAGE)
            {
                //Fills the message object with data before saving it to the cloud store
                message[ID] = id.toString()
                message[SENDERNAME] = user.displayName.toString()
                message[TEXT] = ""
                message[DATE] = time
                message[AVATARURL] = user.photoUrl.toString()
                message[PHOTOURL] = imageRoute

                //Adds image message to the cloud store
                messageRepository.addMessage.document().set(message)
                    .addOnSuccessListener {
                        Log.d(TAG, "Success adding message")

                        //Updates the date of the rooms latest message
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

    //This method is called from onOptionsSelected to give the user a way to sign completely out of the application.
    private fun signOut(){
        val user = authInstance.currentUser
        if (user != null) {
            for (item in user.providerData){
                when (item.providerId) {
                    GOOGLE_LINK -> {
                        authInstance.signOut()
                        googleSignInClient.signOut()
                    }
                    FACEBOOK_LINK -> {
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
