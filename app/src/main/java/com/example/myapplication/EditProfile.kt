package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityEditProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditProfile : AppCompatActivity() {

    //viewbinding
    private lateinit var binding: ActivityEditProfileBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref .child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                //Get user info
                override fun onDataChange(snapshot: DataSnapshot){
                    val fotoprofile = "$(snapshot.child("fotoprofile").value)"
                    val nama = "$(snapshot.child("nama").value)"
                    val kontak = "$(snapshot.child("kontak").value)"
                    val alamat = "$(snapshot.child("alamat").value)"

                    //setdata
                    binding.etName.setText(nama)
                    binding.etContact.setText(kontak)
                    binding.etAlamat.setText(alamat)

                    //setImage
                    
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }
}