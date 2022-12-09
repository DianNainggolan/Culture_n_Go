package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.PopupMenu
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

    //image uri(which we will pick)
    private var imageUri: Uri?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        // tombol back
        binding.btnBack.setOnClickListener{
            onBackPressed()
        }

        //pick imagefrom camera
        binding.tvProfile.setOnClickListener{
            showImageAttachMenu()
        }

        //tombol edit
        binding.btnEdit.setOnClickListener{

        }
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref .child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                //Get user info
                override fun onDataChange(snapshot: DataSnapshot){
                    val fotoprofile = "${snapshot.child("fotoprofile").value}"
                    val nama = "${snapshot.child("nama").value}"
                    val kontak = "${snapshot.child("kontak").value}"
                    val alamat = "${snapshot.child("alamat").value}"

                    //setdata
                    binding.etName.setText(nama)
                    binding.etContact.setText(kontak)
                    binding.etAlamat.setText(alamat)

                    //setImage
                    try{
                        Glide.with(this@EditProfile)
                            .load(fotoprofile)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.tvProfile)
                    }catch (e:Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }

    private fun showImageAttachMenu(){
        /*show popup menu with options camera*/

        //setup popup
        val popupMenu = PopupMenu(this, binding.tvProfile)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")
        popupMenu.show()

        //handle popup menu item click

        popupMenu.setOnMenuItemClickListener { item->
            //get id from the clicked item
            val id = item.itemId
            if(id==0){
                // camera click
                pickImageCamera()
            }else if(id==1){
                //gallery clicked
                pickImageGallery()
            }


            true

        }
    }

    private fun pickImageCamera(){

    }

    private fun pickImageGallery() {
        TODO("Not yet implemented")
    }
}