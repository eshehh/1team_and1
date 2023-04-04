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
import com.example.ggestagram.navigation.model.AlarmDTO
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

    // onCreateView' 함수는 프래그먼트의 레이아웃을 확장하고 RecyclerView를 초기화하는 역할을 합니다. LayoutInflater를 사용하여 R.layout.fragment_detail_view 리소스 파일에서 레이아웃을 확장하고,
    // RecyclerView 어댑터를 DetailViewRecylerViewAdapter 내부 클래스의 인스턴스로 설정하고, 레이아웃 관리자를 LinearLayoutManager의 새 인스턴스로 설정합니다. .

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

    // Firestore 데이터베이스의 데이터로 RecyclerView를 채우는 역할을 합니다.
    // 이는 RecyclerView.Adapter 클래스를 확장하고 해당 메서드에 대한 구현을 제공합니다.
    // 또한 ContentDTO 개체와 해당 UID를 보관할 두 개의 배열을 선언합니다.
    inner class DetailViewRecylerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        //변경 사항이 감지되면 contentDTOs 및 contentUidList 배열이 지워지고 데이터베이스에서 새 데이터가 검색되어 이러한 배열에 추가됩니다.
        // 'notifyDataSetChanged' 함수가 호출되어 어댑터에 변경 사항을 알리고 RecyclerView 새로 고침을 트리거합니다.
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

        // 개별 상세 뷰(틀)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)

            return CustomViewHoler(view)
        }

        inner class CustomViewHoler(view: View?) : RecyclerView.ViewHolder(view!!) {

        }

        // 개별 상세 뷰를 뿌리는 데이터를 가지고 있음
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
            //  Firestore에서 특정 사용자 ID('uid')와 연결된 프로필 이미지를 검색하고 Glide를 사용하여 ImageView에 표시
            firestore?.collection("profileImages")
                    // Firestore의 profileImages 컬렉션에 액세스
                ?.document(contentDTOs[position].uid!!)
                    // contentDTOs 목록의 지정된 position에서 사용자 ID(uid)와 연결된 문서를 검색
                ?.get()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        var url = task.result?.get("image")

                        if (url != null) {
                            // url이 널이 아니면
                            Glide.with(holder.itemView.context).load(url)
                                .apply(RequestOptions().circleCrop())
                                    // 이미지에 원형 자르기를 적용
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

            // 이 코드를 통해 사용자는 클릭한 콘텐츠를 만든 사용자의 프로필로 이동할 수 있습니다.
           viewholder.profile_textview.setOnClickListener {
                var userFragment = UserFragment()
                var bundle = Bundle()
               // destinationUid는 프로필을 보고 있는 사용자이고 userId는 보고 있는 콘텐츠를 게시한 사용자
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
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                it.set(tsDoc, contentDTO)
            }


        }
    }

    // 좋아요 알림
    fun favoriteAlarm(destinationUid : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.kind = 0
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        // 좋아요 푸시 이벤트
//        var message = FirebaseAuth.getInstance()?.currentUser?.email + " "+ getString(R.string.alarm_favorite)
//        FcmPush.instance.sendMessage(destinationUid,"Stagram",message)
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


