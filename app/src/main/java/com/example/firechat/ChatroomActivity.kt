package com.example.firechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_chat.*

class ChatroomActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var adapter: ChatroomAdapter
    private lateinit var firestoreListener: ListenerRegistration
    private var roomRepository: RoomRepository = RoomRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        AppEventsLogger.activateApp(application)
        setContentView(R.layout.activity_chat)

        loadRoomList()

        firestoreListener = roomRepository.roomListener.first.also {
            adapter = ChatroomAdapter(applicationContext, roomRepository.roomListener.second)
            rv_chatroom_list.adapter = adapter
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
        rv_chatroom_list.layoutManager = LinearLayoutManager(this)
        rv_chatroom_list.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        rv_chatroom_list.itemAnimator = DefaultItemAnimator()
        adapter = ChatroomAdapter(applicationContext, roomRepository.allRooms)
        rv_chatroom_list.adapter = adapter
    }

    private fun signOut(){
        authInstance.signOut()
        LoginManager.getInstance().logOut()
        googleSignInClient.signOut()

        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }
}
