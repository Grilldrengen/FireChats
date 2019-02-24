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
import com.example.firechat.models.Room
import com.example.firechat.models.Room.Companion.DESCRIPTION
import com.example.firechat.models.Room.Companion.ID
import com.example.firechat.models.Room.Companion.LASTMESSAGE
import com.example.firechat.models.Room.Companion.NAME
import com.example.firechat.repositories.RoomRepository
import com.example.firechat.services.Constants.Companion.GOOGLE_TOKEN
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_chatroom.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.contentView
import org.jetbrains.anko.design.longSnackbar


class ChatroomActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var adapter: ChatroomAdapter = ChatroomAdapter()
    private lateinit var firestoreListener: ListenerRegistration
    private var roomRepository: RoomRepository = RoomRepository()

    companion object {
        const val TAG = "ChatroomActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventsLogger.activateApp(application)
        setContentView(R.layout.activity_chatroom)

        //Sets up the actionbar. This secures there is a visible back button to use on newer phones.
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true);
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar!!.title = this.getString(R.string.chat_rooms)

        //Sets up the recylcerview. Decides the item animations, the divider decoration and sets the adapter
        rv_chatroom_list.layoutManager = LinearLayoutManager(this)
        rv_chatroom_list.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        rv_chatroom_list.itemAnimator = DefaultItemAnimator()
        rv_chatroom_list.adapter = adapter

        //This listener fills out the recylcerview when the user enters the list of rooms.
        //The listener also makes sure to listen to updates in the data. This ensures the list is up to date all the time.
        firestoreListener = roomRepository.roomListener.addSnapshotListener(EventListener { documentSnapshots, e ->
            if (e != null) {
                Log.e(TAG, this.getString(R.string.listen_failed), e)
                return@EventListener
            }

            val roomList = mutableListOf<Room>()

            for (doc in documentSnapshots!!) {
                val room = doc.toObject(Room::class.java)
                room.id = doc.id
                room.name = doc.getString(NAME)
                room.description = doc.getString(DESCRIPTION)
                room.lastMessage = doc.getTimestamp(LASTMESSAGE)?.toDate()
                roomList.add(room)
            }

            roomList.sortDescending()
            adapter.setRooms(roomList)

        })

        //Calls the refresh method when user pulls the screen downwards
        sr_chatroom.setOnRefreshListener {
            refreshList()
        }

        //This is build so we can sign out of google from the actionbar menu when needed.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(GOOGLE_TOKEN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    //Eventbus is used to display a snackbar for the service since there is no context in it.
    //We subscribe here and listens for data.
    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    // Inflate the menu to use in the action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Handles presses on the action bar menu items. Generates the back button on the actionbar and also gives the option to sign out
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finishAffinity()
            }
            R.id.action_sign_out -> {
                signOut()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Since we in the login activity checks if the user is already signed in, in onStart, the onBackPressed would only recall this activty and simply do nothing.
    //Therefore finishAffinity() is user so the user can press onBackPressed to close the application without login out.
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    //We unsubscribe here from eventbus to makes sure there is no memory leak
    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    //We unsubscribe from the firestorelistener here to makes sure there is no memory leak
    override fun onDestroy() {
        super.onDestroy()
        firestoreListener.remove()
    }

    //This method is called to refresh the list of rooms
    private fun refreshList() {
        //TODO doesn't work properly, it doesn't update but reset the changes just made by the firestorelistener and should therefore not be used when a listener i implemented
        roomRepository.allRooms.addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val roomList = mutableListOf<Room>()

                for (doc in task.result!!) {
                    val room = doc.toObject(Room::class.java)
                    room.id = doc.id
                    room.name = doc.getString("name")
                    room.description = doc.getString("description")
                    room.lastMessage = doc.getTimestamp("lastMessage")?.toDate()
                    roomList.add(room)
                }

                roomList.sortDescending()
                adapter.setRooms(roomList)

                sr_chatroom.isRefreshing = false

            } else {
                Log.d(TAG, "Error getting documents: ", task.exception)
            }
        }
    }

    //This method is called from onOptionsSelected to give the user a way to sign completely out of the application.
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

    //This is where the eventbus receive the message sent from MyFirebaseNotificationService.
    //This is displayed in a Snackbar so users in the chat room list can see if new messages are received
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(messageEvent: String) {
        contentView!!.longSnackbar(messageEvent)
    }
}
