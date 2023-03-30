package com.example.ggestagram.navigation

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ggestagram.R
import com.example.ggestagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.lang.String.format
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //Initialize storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()




        //Open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type="image/*"
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode==Activity.RESULT_OK){
                photoUri = it.data?.data
                addphoto_image.setImageURI(photoUri)
            }
            else{
                finish()
            }
        }.launch(photoPickerIntent)


        add_photon_btn.setOnClickListener {
            contentUpload()
        }

    }

    private fun contentUpload() {

        //Make filename
        var timestamp = SimpleDateFormat("yyyyMMddmmss").format(Date())
        var imageFilename = "Image_" + timestamp + "_.png"
        var storageRef = storage?.reference?.child("images")?.child(imageFilename)




        storageRef?.putFile(photoUri!!)?.continueWithTask {
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener {
            var contentDTO = ContentDTO()

            contentDTO.imageUrl = it.toString()
            contentDTO.uid = auth?.currentUser?.uid
            contentDTO.explain = addphoto_edit_explain.text.toString()
            contentDTO.userId = auth?.currentUser?.email
            contentDTO.timeStamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)
            setResult(Activity.RESULT_OK)
            finish()
        }


    }


}


