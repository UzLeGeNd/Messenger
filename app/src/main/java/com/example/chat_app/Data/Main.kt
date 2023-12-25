package com.example.chat_app.Data

import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.input.TextFieldValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class Message(
    val from: String?,
    val to: String?,
    val msg: String?,
    val date: String?,
) {
    constructor() : this(null, null, null, null)
}

data class User(
    val fullname: String?,
    val username: String?,
    var password: String?,
) {
    constructor() : this(null, null, null)
}

class Main {

    companion object {
        private val users = FirebaseDatabase.getInstance().reference.child("users")
        private val messages = FirebaseDatabase.getInstance().reference.child("messages")

        fun saveUser(context: android.content.Context, user: String) {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("db", android.content.Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString("user", user)
            editor.apply()
        }

        fun getSavedUser(context: android.content.Context): String {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("db", android.content.Context.MODE_PRIVATE)
            return sharedPreferences.getString("user", "") ?: ""
        }


        fun createUser(user: User) {
            users.child(user.username!!).setValue(user)
        }

        fun checkUser(user: String, callback: (Boolean) -> Unit) {
            users.child(user).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        callback(false)
                    } else {
                        callback(true)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(false)
                }
            })
        }
        fun setPassword(context: android.content.Context, user: String, new_password: String) {
            users.child(user).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val changedUser = dataSnapshot.getValue(User::class.java)
                    if (changedUser != null && new_password!="") {
                        changedUser.password = new_password
                        Toast.makeText(context, "Your password is successfully changed", Toast.LENGTH_SHORT).show()

                    }
                    else{
                        Toast.makeText(context, "Password can't be changed", Toast.LENGTH_SHORT).show()
                    }
                    users.child(user).setValue(changedUser)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }


        @RequiresApi(Build.VERSION_CODES.O)
        fun sendMessage(from: String, to: String, msg: String) {
            if (msg!=""){
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                val formattedDateTime = currentDateTime.format(formatter)
                val data = Message(from, to, msg, formattedDateTime)
                messages.push().setValue(data)
            }
        }

        fun getMessages(chat: String, user: String, callback: (List<Message>) -> Unit) {
            messages.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val messages = mutableListOf<Message>()
                    val children = dataSnapshot.children
                    children.forEach {
                        val message = it.getValue(Message::class.java)
                        if (message != null) {
                            if ((message.from == chat && message.to == user) || (message.from == user && message.to == chat)) {
                                messages.add(message)
                            }
                        }
                    }
                    callback(messages)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(emptyList())
                }
            })
        }


        fun getUsers(callback: (List<String>) -> Unit) {
            users.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val users = mutableListOf<String>()
                    for (snapshot in dataSnapshot.children) {
                        users.add(snapshot.key.toString())
                    }
                    callback(users)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(emptyList())
                }
            })
        }




        fun getUser(user: TextFieldValue, password: TextFieldValue, callback: (String) -> Unit) {
            users.child(user.text).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getValue(User::class.java)!!.password == password.text) {
                        callback("Successful Login")
                    } else {
                        callback("Incorrect Username or Password")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback("Database Error")
                }
            })
        }


    }
}