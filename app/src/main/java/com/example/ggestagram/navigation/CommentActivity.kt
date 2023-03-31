package com.example.ggestagram.navigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        contentUid = intent.getStringExtra("contentUid")

        comment_recyclerview.adapter = CommentRecylerViewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)


        comment_btn_send?.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_edit_message.text.toString()
            comment.timeStamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images")?.document(contentUid!!)?.collection("comments").document().set(comment)
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
    }

    inner class CommentRecylerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var comments : ArrayList<ContentDTO.Comment> = arrayListOf()
        init {
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                .collection("comments").orderBy("timeStamp")
                .addSnapshotListener { value, error ->
                    comments.clear()
                    if (value == null) return@addSnapshotListener

                    for (snapshot in value.documents!!) {
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java!!)!!)
                    }
                    notifyDataSetChanged()
                }
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

            FirebaseFirestore.getInstance().collection("profileImages").document(comments[position].uid!!)
                .get().addOnCompleteListener {
                    if(it.isSuccessful){
                        var url = it.result!!["image"]
                        Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(comment_imageview_profile!!)
                    }

                }
            view.comment_textview_comment.setOnClickListener {
                deleteCommentData(comments[position].uid!!, comments[position].comment!!)
            }
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
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                FirebaseFirestore.getInstance()
                                    .collection("images")
                                    .document(contentUid!!)
                                    .collection("comments")
                                    .document(document.id)
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

        override fun getItemCount(): Int {
            return comments.size
        }

    }


}