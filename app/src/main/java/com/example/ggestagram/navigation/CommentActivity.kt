package com.example.ggestagram.navigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ggestagram.R
import com.example.ggestagram.navigation.model.AlarmDTO
import com.example.ggestagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.item_detail.view.*


class CommentActivity : AppCompatActivity() {
    var contentUid : String? = null
    var destinationUid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        contentUid = intent.getStringExtra("contentUid")
        destinationUid = intent.getStringExtra("destinationUid")

        comment_recyclerview.adapter = CommentRecylerViewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)


        comment_btn_send?.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_edit_message.text.toString()
            comment.timeStamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images")?.document(contentUid!!)?.collection("comments").document().set(comment)
//            commentAlarm(destinationUid!!,comment_edit_message.text.toString())
            comment_edit_message.setText("")
        }
    }

    // 댓글 알림
    fun commentAlarm(destinationUid : String, message : String) {
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.kind = 1
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.timestamp = System.currentTimeMillis()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        // 댓글 푸시 이벤트
//        var msg = FirebaseAuth.getInstance()?.currentUser?.email + " "+ getString(R.string.alarm_comment)+ " of " + message
//        FcmPush.instance.sendMessage(destinationUid,"Stagram",msg)
    }

    inner class CommentRecylerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var comments : ArrayList<ContentDTO.Comment> = arrayListOf()
        init {
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                .collection("comments").orderBy("timeStamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException  ->
                    comments.clear()
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot.documents!!) {
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java!!)!!)
                        // 댓글이 추가될 때마다 알람 추가
//                        commentAlarm(snapshot.get("uid") as String, snapshot.get("comment") as String)
                    }
                    notifyDataSetChanged()
                }

        }

        private fun editCommentData(uid: String, comment: String) {

            val alertDialogBuilder = AlertDialog.Builder(this@CommentActivity)
            val editCommentEditText = EditText(this@CommentActivity)
            editCommentEditText.setText(comment)
            alertDialogBuilder.setView(editCommentEditText)
                .setMessage("댓글을 수정하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("수정") { dialog, id ->
                    val editedComment = editCommentEditText.text.toString()
                    FirebaseFirestore.getInstance()
                        .collection("images")
                        .document(contentUid!!)
                        .collection("comments")
                        .whereEqualTo("uid", uid)
                        .whereEqualTo("comment", comment)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.size() > 0) {
                                val documentId = documents.documents[0].id
                                FirebaseFirestore.getInstance()
                                    .collection("images")
                                    .document(contentUid!!)
                                    .collection("comments")
                                    .document(documentId)
                                    .update("comment", editedComment)
                                    .addOnSuccessListener {
                                        // 댓글 수정 성공 처리
                                    }
                                    .addOnFailureListener { e ->
                                        // 댓글 수정 실패 처리
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            // 댓글 수정 실패 처리
                        }
                }
                .setNegativeButton("취소") { dialog, id ->
                    dialog.cancel()
                }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        private fun deleteCommentData(uid: String, comment: String) {
            val alertDialogBuilder = AlertDialog.Builder(this@CommentActivity)
            alertDialogBuilder.setMessage("댓글을 삭제하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("삭제") { dialog, id ->
                    FirebaseFirestore.getInstance()
                        .collection("images")
                        .document(contentUid!!)
                        .collection("comments")
                        .whereEqualTo("uid", uid)
                        .whereEqualTo("comment", comment)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.size() > 0) {
                                val documentId = documents.documents[0].id
                                FirebaseFirestore.getInstance()
                                    .collection("images")
                                    .document(contentUid!!)
                                    .collection("comments")
                                    .document(documentId)
                                    .delete()
                                    .addOnSuccessListener {
                                        // 댓글 삭제 성공 처리
                                    }
                                    .addOnFailureListener { e ->
                                        // 댓글 삭제 실패 처리
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            // 댓글 삭제 실패 처리
                        }
                }
                .setNegativeButton("취소") { dialog, id ->
                    dialog.cancel()
                }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
            return CustomViewHolder(view)
        }
        private inner class CustomViewHolder(view : View?) : RecyclerView.ViewHolder(view!!)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = (holder as CustomViewHolder).itemView
            view.comment_textview_comment.text = comments[position].comment
            view.comment_textview_profile.text = comments[position].userId

            FirebaseFirestore.getInstance().collection("profileImages").document(comments[position].uid!!).get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    var url = task.result!!["image"]
                    if (url != null) {
                        Glide.with(view.context).load(url).apply(RequestOptions().circleCrop())
                            .into(view.comment_imageview_profile)
                    }
                }
            }

            view.comment_textview_comment.setOnClickListener {
                deleteCommentData(comments[position].uid!!, comments[position].comment!!)
            }
            view.comment_textview_comment.setOnLongClickListener {
                editCommentData(comments[position].uid!!, comments[position].comment!!)
                true
            }
        }



        override fun getItemCount(): Int {
            return comments.size
        }

    }


}