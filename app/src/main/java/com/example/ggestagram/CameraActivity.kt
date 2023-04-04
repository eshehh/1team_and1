package com.example.ggestagram

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import android.util.Log
import android.view.MotionEvent
import com.example.ggestagram.navigation.UserFragment

class CameraActivity : AppCompatActivity(), View.OnClickListener {
    val CAMERA = arrayOf(android.Manifest.permission.CAMERA)
    val STORAGE = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    // 저장소 권한
    val CAMERA_CODE = 98 // 카메라 권한 요청 코드
    val STORAGE_CODE = 99 // 저장소 권한 요청 코드

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // 카메라 호출
        camera.setOnClickListener(this)

        // 메인으로
        picture.setOnClickListener(this)



    }
    // 버튼 클릭 이벤트 처리
    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.camera -> CallCamera()
            R.id.picture -> GetAlbum()

        }
    }
    // 카메라 권한, 저장소 권한
    // 요청 권한
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CAMERA_CODE -> {
                for (grant in grantResults){
                    if(grant != PackageManager.PERMISSION_GRANTED){
                        // 카메라 권한이 승인되지 않은 경우 사용자에게 알림
                        Toast.makeText(this, "카메라 권한을 승인해 주세요", Toast.LENGTH_LONG).show()
                    }
                }
            }
            STORAGE_CODE -> {
                for(grant in grantResults){
                    if(grant != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "저장소 권한을 승인해 주세요", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // 다른 권한등도 확인이 가능하도록
    fun checkPermission(permissions: Array<out String>, type:Int):Boolean{
        // 디바이스가 안드로이드 6.0 Marshmallow 이상인지 확인합니다.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            for (permission in permissions){
                if(ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, permissions, type)
                    return false
                }
            }
        }
        return true
    }

    // 카메라 촬영 - 권한 처리
    fun CallCamera(){
        // 카메라 및 저장소 권한 확인
        if(checkPermission(CAMERA, CAMERA_CODE) && checkPermission(STORAGE, STORAGE_CODE)){
            // 카메라 호출 인텐트 실행
            val itt = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(itt, CAMERA_CODE)
        }
    }

    // 촬영된 사진을 저장하기 위해서 파일을 저장하는 메서드
    // 주어진 파일 이름, MIME 유형, 비트맵을 사용하여 파일을 저장하는 함수
    fun saveFile(fileName:String, mimeType:String, bitmap: Bitmap): Uri?{

        // fileName, mimeType, bitmap 생성하고 저장하기위해 ContentValues()
        // ContentProviders에 레코드를 삽입하거나 업데이트하기 위해 ContentResolver와 함께 사용됩니다.
        // 새로운 ContentValues 객체를 만듭니다.
        var CV = ContentValues()

        // MediaStore 에 파일명, mimeType 을 지정 , put() 메서드를 사용하여 CV 객체에 추가 ,MediaStore는 외부 저장소를 관리하는 데이터베이스
        CV.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        CV.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        // 안정성을 위해
        // 디바이스가 Android Q 이상인 경우, 이미지를 대기 중인 상태로 설정합니다.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            CV.put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        // 이미지를 MediaStore에 저장하고 해당 URI를 가져옵니다.
        // MediaStore 에 파일을 저장
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, CV)
        // URI가 null이 아닌 경우, 비트맵을 파일에 작성합니다.
        if(uri != null){
            // URI에 대한 파일 디스크립터를 엽니다 파일스크립터를 통해 파일을 읽거나 쓸 수 있게 되는데  모드를 "w"로 지정해 쓰기 위한 것
            var scriptor = contentResolver.openFileDescriptor(uri, "w")

            // 파일 디스크립터에 대한 FileOutputStream을 생성합니다.
            val fos = FileOutputStream(scriptor?.fileDescriptor)

            // 비트맵을 압축하고 파일에 씁니다.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            // FileOutputStream을 닫습니다.
            fos.close()

            // 디바이스가 Android Q 이상인 경우, 이미지를 대기 중이 아닌 것으로 표시합니다.
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                CV.clear()
                // IS_PENDING 을 초기화
                CV.put(MediaStore.Images.Media.IS_PENDING, 0)
                // 마지막으로 업데이트된 ContentValues 객체로 MediaStore를 업데이트합니다.
                contentResolver.update(uri, CV, null, null)
            }
        }
        // 저장된 이미지의 URI를 반환합니다.
        return uri
    }

    // 결과
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 캡처된 이미지를 보여줄 ImageView에 대한 참조 가져오기
        val imageView = findViewById<ImageView>(R.id.avatars)

        // 카메라 액티비티가 성공적으로 완료되었는지 확인하기
        if(resultCode == Activity.RESULT_OK){
            // 액티비티가 카메라 액티비티인지 확인하기
            //  이 코드는 onActivityResult() 함수에서 사용되며, 카메라 앱에서 결과가 성공적으로 반환되었는지 확인합니다.
            //  결과 코드가 Activity.RESULT_OK와 같으면, 즉 카메라 앱에서 촬영된 이미지가 있으면 아래의 코드 블록이 실행됩니다.
            when(requestCode){
                // 카메라 액티비티가 썸네일 이미지를 Bitmap으로 반환한 경우
                CAMERA_CODE -> {
                    // Intent 데이터가 "data" 키를 가진 null이 아닌 extras(값) 번들을 포함하는지 확인하기
                    if(data?.extras?.get("data") != null){
                        // 썸네일 이미지를 Bitmap 객체로 가져오기
                        val img = data?.extras?.get("data") as Bitmap
                        // 무작위로 생성된 파일 이름과 MIME 타입 "image/jpeg"으로 이미지를 파일에 저장하기
                        val uri = saveFile(RandomFileName(), "image/jpeg", img)
                        // 저장된 이미지 파일을 ImageView의 소스로 설정하기
                        imageView.setImageURI(uri)
                    }
                }
                // 없는 부분
                STORAGE_CODE -> {
                    val uri = data?.data
                    imageView.setImageURI(uri)
                }
            }
        }
    }

    // 파일명을 날짜 저장
    fun RandomFileName() : String{
        val fileName = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        return fileName
    }

    // 갤러리 취득
    fun GetAlbum(){
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }


}