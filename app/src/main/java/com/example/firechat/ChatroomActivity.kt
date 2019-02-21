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
import com.example.firechat.repositories.RoomRepository
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_chatroom.*

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

        val actionBar = supportActionBar
        actionBar!!.title = "Chat Rooms"

        rv_chatroom_list.layoutManager = LinearLayoutManager(this)
        rv_chatroom_list.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        rv_chatroom_list.itemAnimator = DefaultItemAnimator()
        rv_chatroom_list.adapter = adapter

        loadRoomList()

        firestoreListener = roomRepository.roomListener.addSnapshotListener(EventListener { documentSnapshots, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed!", e)
                return@EventListener
            }

            val roomList = mutableListOf<Room>()

            for (doc in documentSnapshots!!) {
                val room = doc.toObject(Room::class.java)
                room.id = doc.id
                room.name = doc.getString("name")
                room.description = doc.getString("description")
                roomList.add(room)
            }

            adapter.setRooms(roomList)

        })

        sr_chatroom.setOnRefreshListener {
            refreshList()
        }

        //Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1079804979782-biupcn69v8395q4kjvfd46vnqlk4qad3.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
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

    override fun onDestroy() {
        super.onDestroy()

        firestoreListener.remove()
    }

    private fun loadRoomList() {
        roomRepository.allRooms.addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val roomList = mutableListOf<Room>()

                for (doc in task.result!!) {
                    val room = doc.toObject(Room::class.java)
                    room.id = doc.id
                    room.name = doc.getString("name")
                    room.description = doc.getString("description")
                    roomList.add(room)
                }

                adapter.setRooms(roomList)

            } else {
                Log.d(TAG, "Error getting documents: ", task.exception)
            }
        }
    }

    private fun refreshList() {
        roomRepository.allRooms.addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val roomList = mutableListOf<Room>()

                for (doc in task.result!!) {
                    val room = doc.toObject(Room::class.java)
                    room.id = doc.id
                    room.name = doc.getString("name")
                    room.description = doc.getString("description")
                    roomList.add(room)
                }

                adapter.setRooms(roomList)

                sr_chatroom.isRefreshing = false

            } else {
                Log.d(TAG, "Error getting documents: ", task.exception)
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
        startActivity(loginIntent)
    }
}
