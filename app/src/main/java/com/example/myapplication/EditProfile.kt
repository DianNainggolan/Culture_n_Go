package com.example.myapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityEditProfileBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class EditProfile : AppCompatActivity() {

    //viewbinding
    private lateinit var binding: ActivityEditProfileBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //image uri(which we will pick)
    private var imageUri: Uri?= null

    //progress dialog
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setup progres dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon menunggu")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebase auth
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
            validateData()
        }
    }

    private var name =""
    private fun validateData() {
        //get data
        name = binding.etName.text.toString().trim()

        //validate data
        if(name.isEmpty()){
            //kosong
            Toast.makeText(this, "Masukkan nama", Toast.LENGTH_SHORT).show()
        }else{
            //name is entered
            if(imageUri == null){
                //need to update without image
                updateProfile("")
            }else{
                //need to update with image
                uploadImage()
            }

        }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Mengupload gambar")
        progressDialog.show()

        //image phat and name, use uid to repale previous
        val filePathAndName = "ProfileImage/"+firebaseAuth.uid

        //storage reference
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener {taskSnapShot->
                // image uploaded, get url of uploaded image

                val uriTask: Task<Uri> = taskSnapShot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                updateProfile(uploadedImageUrl)
            }
            .addOnFailureListener{ e ->
                progressDialog.dismiss()
                Toast.makeText(this,"Gagal memasukkan gambar dikarenakan ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Mengupdate profil...")

        //setup info to update to db
        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["nama"] = "$name"
        if(imageUri != null){
            hashmap["fotoprofil"] = uploadedImageUrl
        }

        //update to db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                //profile updated
                progressDialog.dismiss()
                Toast.makeText(this,"Berhasil mengubah profil",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{ e ->
                progressDialog.dismiss()
                Toast.makeText(this,"Gagal mengupdate profil dikarenakan ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref .child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                //Get user info
                override fun onDataChange(snapshot: DataSnapshot){
                    val fotoprofil = "${snapshot.child("fotoprofil").value}"
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
                            .load(fotoprofil)
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
        //intent to pick image from camera
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
        //intent to pick image from gallery
        val intent= Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    //used to handle result of camera intent(new way in replacement of startactivityforresutlt)
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->
            //get url of image
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                //imageUri = data!!.data no need we already have image in imageUri in camera case


                //set to imageview
                binding.tvProfile.setImageURI(imageUri)
            }else{
                //cancelled
                Toast.makeText(this,"Membatalkan", Toast.LENGTH_SHORT).show()
            }
        }
    )

    //used to handle result of gallery intent(new way in replacement of startactivityforresutlt)
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{ result ->
            //get url of image
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                //set to imageview
                binding.tvProfile.setImageURI(imageUri)
            }else{
                //cancelled
                Toast.makeText(this,"Membatalkan", Toast.LENGTH_SHORT).show()
            }
        }
    )

}