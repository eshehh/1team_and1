package com.example.ggestagram.navigation

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ggestagram.CameraActivity
import com.example.ggestagram.LoginActivity
import com.example.ggestagram.MainActivity
import com.example.ggestagram.R
import com.example.ggestagram.navigation.model.AlarmDTO
import com.example.ggestagram.navigation.model.ContentDTO
import com.example.ggestagram.navigation.model.FollowerDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var fragmentView:View? = null
    var firestore:FirebaseFirestore? = null
    var uid:String? = null
    var auth: FirebaseAuth? = null
    var currentUserUid : String? = null

    val content = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        if (it.resultCode == Activity.RESULT_OK) {
            var imageUri = it.data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages")
                .child(uid!!)
            storageRef.putFile(imageUri!!).continueWithTask {
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener {
                var map = HashMap<String, Any>()
                map["image"] = it.toString()
                FirebaseFirestore.getInstance().collection("profileImages")
                    .document(uid!!).set(map)


            }


        }
    }

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
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        // 넘어온 Uid 받아오기
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.uid
        //자신의 정보일 경우
        if(uid == currentUserUid){
            fragmentView?.userid?.text = auth?.currentUser?.email
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                activity?.finish()
                var intent = Intent(activity,LoginActivity::class.java)
                startActivity(intent)
                auth?.signOut()
            }

        }
        //다른 사람의 정보일 경우
        else{
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
            var mainactivity = (activity as MainActivity)
            mainactivity?.toolbar_tv_userid?.text = arguments?.getString("userId")
            mainactivity?.toolbar_btn_back?.setOnClickListener {
                mainactivity.bottom_navigation.selectedItemId = R.id.action_home
            } // 디테일 화면으로 돌아가기

            mainactivity?.toolbar_title_image?.visibility = View.GONE
            mainactivity?.toolbar_tv_userid?.visibility = View.VISIBLE
            mainactivity?.toolbar_btn_back?.visibility = View.VISIBLE
            fragmentView?.camera?.visibility = View.GONE
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                requestFollow()
            }

        }


        // 어댑터 연결
        fragmentView?.account_recylerview?.adapter = UserFragmentRecylerView()
        fragmentView?.account_recylerview?.layoutManager = GridLayoutManager(activity,3)

        // 프로필 사진 클릭
        fragmentView?.asccount_iv_profile?.setOnClickListener {

            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            content.launch(photoPickerIntent)


        }

        fragmentView?.camera?.setOnClickListener {
            activity?.finish()
            var intent = Intent(activity, CameraActivity::class.java)
            startActivity(intent)
        }

        getProfileImage()
        getFollowandFollowing()
        return fragmentView
    }
    fun getFollowandFollowing(){
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { value, error ->
            if( value == null){
                return@addSnapshotListener
            }
            var followDTO = value.toObject(FollowerDTO::class.java)
            if(followDTO?.followingCount!=null){
                fragmentView?.account_tv_following_counter?.text = followDTO?.followingCount?.toString()
            }
            if(followDTO?.followerCount!=null){
                fragmentView?.account_tv_follower_counter?.text = followDTO?.followerCount?.toString()
                if(FirebaseAuth.getInstance().uid == uid){
                    //나의 페이지 일경우
                    return@addSnapshotListener
                }
                if(followDTO?.followers.containsKey(currentUserUid!!)){
                    fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow_cancel)
                }
                else{
                    fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)

                }
            }

        }
    }

//    override fun onStop() {
//        super.onStop()
//        ex) var = followListenerRegistration: ListenerRegistration? = null
//        followListenerRegistration?.remove
//

    // 팔로워 알림
    fun followerAlarm(destinationUid : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser?.email
        alarmDTO.uid = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        // 팔로워 푸시 이벤트
//        var message = auth?.currentUser?.email + " "+getString(R.string.alarm_follow)
//        FcmPush.instance.sendMessage(destinationUid,"Stagram",message)
    }


    // 팔로우
    fun requestFollow(){
        // my follower
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction {
            var followDTO =  it.get(tsDocFollowing!!).toObject(FollowerDTO::class.java)

            if (followDTO == null){
                // 팔로우 정보가 없을 때 생성
                followDTO = FollowerDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followers[uid!!] = true
            }

            else {
                if (followDTO.following.containsKey(uid)) {
                    followDTO?.followingCount = followDTO?.followingCount - 1
                    followDTO?.followers?.remove(uid)
                } else {
                    followDTO?.followingCount = followDTO?.followingCount + 1
                    followDTO?.followers[uid!!] = true
                }
            }
            it.set(tsDocFollowing,followDTO)
            return@runTransaction
        }

        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction {
            var followDTO = it.get(tsDocFollower!!).toObject(FollowerDTO::class.java)
            if(followDTO == null){
                followDTO = FollowerDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)


            }


            else {
                if (followDTO!!.followers.containsKey(currentUserUid)) {
                    followDTO!!.followerCount = followDTO!!.followerCount - 1
                    followDTO!!.followers.remove(currentUserUid!!)

                } else {
                    followDTO!!.followerCount = followDTO!!.followerCount + 1
                    followDTO!!.followers[currentUserUid!!] = true
                    followerAlarm(uid!!)
                }
            }
            it.set(tsDocFollower,followDTO!!)
            return@runTransaction


        }

        //


    }


    inner class UserFragmentRecylerView : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()

        init{
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener { value, error ->

                if(value == null) {
                    return@addSnapshotListener
                }
                else{
                    for(snapshot in value.documents){
                        var data = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(data!!)
                    }
                    fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()

                    notifyDataSetChanged()
                }

            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            //화면 폭의 1/3
            var width = resources.displayMetrics.widthPixels/3
            var imageView = ImageView(parent.context)

            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width,width)

            return CustomViewHolder(imageView)

        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageView)

        }

        override fun getItemCount(): Int {

            return contentDTOs.size

        }


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        val PICK_PROFILE_FROM_ALBUM = 10


        fun newInstance(param1: String, param2: String) =
            UserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)

                }
            }
    }
    fun getProfileImage(){
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener
            if (value.data != null) {
                var url = value?.data!!["image"]
                Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.asccount_iv_profile!!)

            }
        }
    }


}
