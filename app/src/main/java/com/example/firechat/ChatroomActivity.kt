package com.example.firechat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.firechat.data.RoomDao
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
    private lateinit var adapter: ChatroomAdapter
    private lateinit var firestoreListener: ListenerRegistration
    private var roomRepository: RoomRepository = RoomRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventsLogger.activateApp(application)
        setContentView(R.layout.activity_chatroom)

        adapter = ChatroomAdapter(applicationContext)

        loadRoomList()

        firestoreListener = roomRepository.roomListener.addSnapshotListener(EventListener { documentSnapshots, e ->
            if (e != null) {
                Log.e(RoomDao.TAG, "Listen failed!", e)
                return@EventListener
            }

            val roomList = mutableListOf<Room>()

            for (doc in documentSnapshots!!) {
                val room = doc.toObject(Room::class.java)
                room.id = doc.id
                room.name = doc.getString("Name")
                room.description = doc.getString("Description")
                roomList.add(room)
            }

            adapter.setRooms(roomList)
            rv_chatroom_list.adapter = adapter
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

        sign_out_btn.setOnClickListener {
            signOut()
        }
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
                    room.name = doc.getString("Name")
                    room.description = doc.getString("Description")
                    roomList.add(room)
                }

                rv_chatroom_list.layoutManager = LinearLayoutManager(this)
                rv_chatroom_list.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
                rv_chatroom_list.itemAnimator = DefaultItemAnimator()
                adapter.setRooms(roomList)
                rv_chatroom_list.adapter = adapter

            } else {
                Log.d(RoomDao.TAG, "Error getting documents: ", task.exception)
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
                    room.name = doc.getString("Name")
                    room.description = doc.getString("Description")
                    roomList.add(room)
                }

                adapter.setRooms(roomList)
                rv_chatroom_list.adapter = adapter

                sr_chatroom.isRefreshing = false

            } else {
                Log.d(RoomDao.TAG, "Error getting documents: ", task.exception)
            }
        }
    }

    private fun signOut(){
        authInstance.signOut()

        LoginManager.getInstance().logOut()
        googleSignInClient.signOut()

        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }
}
