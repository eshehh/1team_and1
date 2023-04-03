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

        // 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()




        //Open the album
        // 앨범을 열기 위한 인텐트 생성
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        // 이미지 선택을 위해 Intent.ACTION_PICK
        photoPickerIntent.type="image/*"
        // 이미지를 선택하면 앨범 액티비티 결과를 처리하기 위해 registerForActivityResult 사용
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode==Activity.RESULT_OK){
                // 선택한 이미지의 Uri를 저장하고 ImageView에 표시
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

        // 파일 이름 생성
        var timestamp = SimpleDateFormat("yyyyMMddmmss").format(Date())
        var imageFilename = "Image_" + timestamp + "_.png"
        var storageRef = storage?.reference?.child("images")?.child(imageFilename)



        // Firebase Storage에 이미지 업로드
        storageRef?.putFile(photoUri!!)?.continueWithTask {
            // continueWithTask 여러 비동기 작업을 함께 연결할 수 있는 연속 메서드
            // 이 경우 이미지 파일을 업로드하는 작업을 업로드된 파일의 다운로드 URL을 가져오는 작업에 연결하는 데 사용
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener {
            var contentDTO = ContentDTO()

            // 업로드된 이미지 URL을 ContentDTO 모델 객체에 설정
            contentDTO.imageUrl = it.toString()
            contentDTO.uid = auth?.currentUser?.uid
            contentDTO.explain = addphoto_edit_explain.text.toString()
            contentDTO.userId = auth?.currentUser?.email
            contentDTO.timeStamp = System.currentTimeMillis()

            // Firestore에 ContentDTO 객체 저장
            firestore?.collection("images")?.document()?.set(contentDTO)
            setResult(Activity.RESULT_OK)
            finish()
        }


    }


}


