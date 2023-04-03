    package com.example.ggestagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.ggestagram.databinding.ActivityFindIdBinding
import com.example.ggestagram.navigation.model.FindIdModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

    class FindIdActivity : AppCompatActivity() {

        lateinit var binding : ActivityFindIdBinding
        lateinit var firestore : FirebaseFirestore
        lateinit var auth : FirebaseAuth
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = DataBindingUtil.setContentView(this,R.layout.activity_find_id)
            firestore = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()
            binding.findIdBtn.setOnClickListener {
                readMyId()
            }
            binding.findPasswordBtn.setOnClickListener {
                var number = binding.edittextEmail.text.toString()
                auth.sendPasswordResetEmail(number)
            }
            binding.dismissBtn.setOnClickListener {
                startActivity(Intent(this,LoginActivity::class.java))
            }

        }
        fun readMyId(){
            var number = binding.edittextPhonenumber.text.toString()
            // 사용자가 입력한 전화번호를 검색하고 일치하는 phoneNumber 필드가 있는 문서가 포함된 findids 컬렉션을 Firestore에 쿼리합니다.
            // 일치하는 문서가 발견되면 'toObject' 메서드를 사용하여 'FindIdModel' 객체로 변환되고 전화번호와 연결된 이메일 ID를 보여주는 Toast 메시지가 표시됩니다.
            firestore.collection("findids").whereEqualTo("phoneNumber",number).get().addOnCompleteListener {
                    task ->
                if(task.isSuccessful){
                    var findIdModel = task.result?.documents?.first()!!.toObject(FindIdModel::class.java)
                    Toast.makeText(this,findIdModel!!.id, Toast.LENGTH_LONG).show()
                }
            }

        }
    }