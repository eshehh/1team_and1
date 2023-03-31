package com.example.ggestagram.navigation

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ggestagram.DoubleClickListener
import com.example.ggestagram.MainActivity
import com.example.ggestagram.R
import com.example.ggestagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_photo.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_detail_view.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailViewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    var uid :String? = null
    private var param1: String? = null
    private var param2: String? = null
    var firestore: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view =
            LayoutInflater.from(activity).inflate(R.layout.fragment_detail_view, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailView_recylerview.adapter = DetailViewRecylerViewAdapter()
        val manager = LinearLayoutManager(activity)
        manager.reverseLayout = true
        manager.stackFromEnd = true
        view.detailView_recylerview.layoutManager = manager


        return view
    }

    inner class DetailViewRecylerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {


            firestore?.collection("images")?.orderBy("timeStamp")
                ?.addSnapshotListener { value, error ->

                    contentDTOs.clear()
                    contentUidList.clear()

                    for (snapshot in value!!.documents) {
                        var data = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(data!!)
                        contentUidList.add(snapshot.id)

                    }
                    //새로고침
                    notifyDataSetChanged()

                }


        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)

            return CustomViewHoler(view)
        }

        inner class CustomViewHoler(view: View?) : RecyclerView.ViewHolder(view!!) {

        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position:Int) {

            var viewholder = (holder as CustomViewHoler).itemView
            //user명
            viewholder.profile_textview.text = contentDTOs[position]!!.userId
            //사진
            Glide.with(holder.itemView.context).load(contentDTOs[position]!!.imageUrl)
                .into(viewholder.imageview_content)
            //설명
            viewholder.explain_textview.text = contentDTOs[position]!!.explain
            //좋아요
            viewholder.favoritecounter_textview.text =
                "좋아요 " + contentDTOs[position]!!.favoriteCount + "개"
            //프로필 사진
//            val into = Glide.with(holder.itemView.context).load(contentDTOs[position]!!.imageUrl)
//                .into(viewholder.profile_image)
            //프로필 사진 수정
            firestore?.collection("profileImages")
                ?.document(contentDTOs[position].uid!!)
                ?.get()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        var url = task.result?.get("image")

                        if (url != null) {
                            Glide.with(holder.itemView.context).load(url)
                                .apply(RequestOptions().circleCrop())
                                .into(viewholder.profile_image)
                        }

                    }
                }
            // 좋아요 버튼에 이벤트 추가 수정
            viewholder.favorite_imageview.setOnClickListener {
                favoirteEvent(holder.adapterPosition)
            }
            // 이미지에 더블클릭하면 좋아요 이벤트 수정
            viewholder.imageview_content.setOnClickListener(object : DoubleClickListener(){
                override fun onDoubleClick(v: View) {
                    favoirteEvent(holder.adapterPosition)
                }
            }
        )




            if(contentDTOs!![position].favorites.containsKey(uid)){
                viewholder.favorite_imageview.setImageResource(R.drawable.ic_favorite)
            }
            else{
                viewholder.favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }

            // 이것도 위에 것도 동일하게 만들어줘도 됐을듯함(viewholder.profile_image.setOnClickListener {
            //                메서드 이름 -> profilemove(position)
            //            })
           viewholder.profile_textview.setOnClickListener {
                var userFragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid",contentDTOs[position].uid)
                bundle.putString("userId",contentDTOs[position].userId)
                userFragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,userFragment)?.commit()
//                var mainActivity = activity as MainActivity
//                mainActivity?.bottom_navigation.selectedItemId = R.id.action_account

            }
            viewholder.comment_imageview.setOnClickListener {
                var intent = Intent(view?.context,CommentActivity::class.java)
                intent.putExtra("contentUid",contentUidList[position])
                startActivity(intent)



            }

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }



        fun favoirteEvent(position: Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction {
                var contentDTO = it.get(tsDoc!!).toObject(ContentDTO::class.java)

                if (contentDTO!!.favorites.containsKey(uid)) {
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)

                } else {
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO?.favorites[uid!!] = true

                }
                it.set(tsDoc, contentDTO)
            }


        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetailViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetailViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}


