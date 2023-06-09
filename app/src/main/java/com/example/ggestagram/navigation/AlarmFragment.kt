package com.example.ggestagram.navigation

import android.os.Bundle
import android.text.Layout
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ggestagram.R
import com.example.ggestagram.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm,container,false)
        view.alarmfragment_RecyclerView.adapter = AlarmRecyclerViewAdapter()
        view.alarmfragment_RecyclerView.layoutManager = LinearLayoutManager(activity)

        return view
    }

    inner class AlarmRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var alarmDTOList : ArrayList<AlarmDTO> = arrayListOf()

        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            // 자기 알림만
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid",uid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                alarmDTOList.clear()
                if(querySnapshot == null) return@addSnapshotListener

                for(snapshot in querySnapshot.documents){
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }

                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView

            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid!!).get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    var url = task.result!!["image"]
                    if (url != null) {
                        Glide.with(view.context).load(url).apply(RequestOptions().circleCrop())
                            .into(view.comment_imageview_profile)
                    }
                }
            }

            when(alarmDTOList[position].kind){
                0 -> {
                    val str = alarmDTOList[position].userId + " " + getString(R.string.alarm_favorite)
                    view.comment_textview_profile.text = str
                }
                1 -> {
                    val str = alarmDTOList[position].userId + " " + getString(R.string.alarm_comment)
                    // + " of " + alarmDTOList[position].message
                    view.comment_textview_profile.text = str
                }
                2 -> {
                    val str = alarmDTOList[position].userId + " " + getString(R.string.alarm_follow)
                    view.comment_textview_profile.text = str
                }
            }
            view.comment_textview_profile.visibility = View.VISIBLE
            view.comment_textview_comment.visibility = View.INVISIBLE
        }
    }
}