package com.example.ggestagram

import android.app.Activity
import android.app.Instrumentation
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.facebook.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception
import com.facebook.appevents.AppEventsLogger;
import com.facebook.appevents.codeless.internal.ViewHierarchy.setOnClickListener
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.common.util.ClientLibraryUtils.getPackageInfo
import com.google.firebase.auth.FacebookAuthProvider
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity(), View.OnClickListener{

    // auth 변수를 설정해주며  Firebase 인증 객체 허용
    var auth : FirebaseAuth? = null
    // googleSignInClient 변수 Google 로그인 클라이언트 허용
    var googleSignInClient : GoogleSignInClient? = null
    // callbackManager 변수 Facebook 로그인 허용
    var callbackManager : CallbackManager? = null

    // GOOGLE_LOGIN_CODE 변수는 Google 로그인 요청 코드를 나타내는데 요청을 받을시
    private val GOOGLE_LOGIN_CODE = -1
    // 구글 로그인 코드 변수를 초기화 해준다

    override fun onCreate(savedInstanceState: Bundle?) {
        //  먼저, onCreate() 메서드를 오버라이드하고 함수를 정의해준다.
        // savedInstanceState의 타입을 Bundle로 지정후 액티비티가 재생성될 떄 사용되는 데이터를 포함시킨다.
        super.onCreate(savedInstanceState)
        //  super.onCreate(savedInstanceState)를 호출하여 부모 클래스의 onCreate()를 실행한후
        setContentView(R.layout.activity_login)
        // setContentView(R.layout.activity_login)을 호출하여 레이아웃 파일을 지정한다.
        auth = FirebaseAuth.getInstance()
        // 파이어베이스 겟 인스턴스를 호출하여 변수에 파이어베이스 인증 인스턴스를 할당시킨다.


        // 버튼 클릭 이벤트를 처리하기 위해 클릭시 상위의 로그인 엑티비티를 호출한다
        login_btn1.setOnClickListener(this)
        login_btn11.setOnClickListener(this)
        login_btn2.setOnClickListener(this)
        login_btn3.setOnClickListener(this)



        // Google 로그인을 구현하기 위한 GoogleSignInOptions 객체를 생성하는 빌더 클래스로
        // GoogleSignInOptions.Builder() 메소드를 호출하여 GoogleSignInOptions 객체 생성
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // requestIdToken() 서버클라이언트 주소로 호출한다
            .requestIdToken("564761092046-mcu3f1lgrboiuhijs8ikrtr6kkrtfsvg.apps.googleusercontent.com")
            //requestEmail()를 호출
            .requestEmail()
            .build()


        // GoogleSignIn.getClient(this, gso) 호출 클라이언트 변수에 로그인 인스턴르를 할당함
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        // 페이스북 로그인 기능을 사용하기 위해 콜백메니저에 팩트리크리에이티드 인스턴스를 할단한다
        callbackManager = CallbackManager.Factory.create()
//        printHashKey()

    }


    override fun onClick(p0: View?) {
        // 인터페이스안에서 클릭이벤트 발생할때 실행되게
        // 상위에 부모클레스에서 이미 구현된 클릭이벤트를 호출한다
        when(p0?.id){
            // 매개변수 p0객체의 id값을 확인한 후
            R.id.login_btn1 -> signinAndSignup()
            R.id.login_btn2 -> facebookLogin()
            R.id.login_btn3 -> googleLogin()
            R.id.login_btn11 -> findIdPasswordButton()
            //버튼 클릭시 로그인,페이스북,구글 로그인 각 함수를 호출한다
        }
    }

//    fun printHashKey() {
//        try {
//            val info = packageManager.getPackageInfo(packageName,PackageManager.GET_SIGNATURES)
//            for (signature in info.signatures) {
//                val md = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray());
//                val hashKey = String(Base64.encode(md.digest(),0))
//                Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
//            }
//        } catch (e: NoSuchAlgorithmException) {
//            Log.e(TAG, "printHashKey()", e);
//        } catch (e: Exception) {
//            Log.e(TAG, "printHashKey()", e);
//        }
//    }



    fun googleLogin(){
        // 구글 로그인 기능을 수행하며
        var signinIntent = googleSignInClient!!.signInIntent
        // 구글 로그인 API 호출하기 위해 객체의 프로퍼티를 호출한후 인텐트를 반환한다.
        loginLauncher.launch(signinIntent)
        //로그인 런처에 할당된 객체를 사용하여 로그인 창을 연다.
        //signinIntent 인텐트는 메서드에 전달된후 로그인을 처리한다
    }
    private val loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        //상위의 loginLauncher 변수 ActivityResult 객체를 저장하는 변수
        // registerForActivityResult() 함수를 사용하여 이 변수에 ActivityResult 객체를 할당한다.
        Log.e("REQSULT CODE Start= ",it.resultCode.toString())
        // 로그창에서 에러 로그를 확인 할수 있으며 "REQSULT CODE Start=" 문자열로 시작하는 로그태그 결과를 호출 한다.
        if(it.resultCode == Activity.RESULT_OK){
            // if(it.resultCode == Activity.RESULT_OK) 코드는 이전 Activity에서 반환된 결과 코드가 성공인지 확인하며
            // 이전 Activity가 성공적으로 완료되었으면 true를 반환하고, 그렇지 않으면 false를 반환합니다.
            val task = Auth.GoogleSignInApi.getSignInResultFromIntent(it.data!!)
            // 위 함수를 사용하여 Google 로그인 결과를 가져온다
            if(task!!.isSuccess){
                // task!!.isSuccess 코드는 Google 로그인 결과가 성공이면 true를 반환하고, 그렇지 않으면 false를 반환합니다.
                var account = task.signInAccount
                // var account = task.signInAccount 코드는 Google 로그인 결과 사용자 계정 정보를 가져와서 account 변수에 저장한다.
                firebaseAuthWithGoogle(account)
                // 파이어베이스에 인증하고 로그인 한다.
            }
            /*var task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try{
                var account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
                Log.d("GoogleLogin","Firebase Auth "+account.id)

            }catch(e:ApiException){
                Log.d("GoogleLogin", "Google Login Failed")
            }
*/
        }
    }

    fun facebookLogin(){
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile","email"))
        //로그인 메니저 인스턴스에 퍼미션 메소드를 호출하여 이메일 권한을 요청한다.
        LoginManager.getInstance()
            .registerCallback(callbackManager,object: FacebookCallback<LoginResult>{
                // 로그인 메소드를 이용하여 로그인 콜백을 등혹한 후 Facebook 로그인이
                override fun onSuccess(result: LoginResult) {
                    handleFaceBookAccessToken(result?.accessToken)
                    // 로그인이 성공하면 onSuccess 콜백 함수가 호출되며 로그인 결과에 대한 정보를 이용할 수 있다.
                }

                override fun onCancel() {
                    // 로그인 정보가 취소될시 현 상태 그대로
                }

                override fun onError(error: FacebookException) {
                    // 로그인중 에러가 발생하면 에러 함수 호출
                }
            })
    }



    fun handleFaceBookAccessToken(token: AccessToken?){
        // 페이스북 로그인 성공 시
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)?.
            // 파이어베이스 권환으로 로그인을 시도하는 함수
            // 파이어베이스 권환에 Firebase Auth Credential로 로그인을 시도합니다.
        addOnCompleteListener {
                it->
            if(it.isSuccessful){
                // 로그인 작업이 완료되면 addOnCompleteListener를 호출하여
                //파이어 베이스의 로그인 성공여부를 확인한다

                Log.e(TAG,"signinEmail ")
                //Login Success 로그인 성공 후 로그 메세지 출력
                moveMainPage(it.result.user)
                // 메인화면으로 이동
            }
            else {
                //Show the error message
                Toast.makeText(this,it.exception?.message,Toast.LENGTH_SHORT).show()
                //로그인 실패할 경우 메세지 속성을 이용하여 실패이유에 대한 에러메세지를 출력한다.
            }

        }

    }

    override fun onActivityResult(requestCode:Int , resultCode : Int, data:Intent?){
        // onActivityResult 함수는 액티비티에서 다른 액티비티로 이동하여 작업이 완료된 후에 다시 돌아올때 호출된다
        callbackManager?.onActivityResult(requestCode,resultCode,data)
        // Facebook SDK에서 사용하는 callbackManager 객체에 로그인 결과를 처리할 수 있도록 전달된 requestCode, resultCode, data 정보를 전달한다.
        super.onActivityResult(requestCode,resultCode,data)
        //super.onActivityResult(requestCode,resultCode,data) 코드는 부모 클래스의 onActivityResult 함수를 호출한다.
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        // 구글 로그인을 수행한 후에 구글 클라이언트에 반환하는 개체
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        // GoogleAuthProvider를 사용하여 사용자의 아이디 토근을 포함하는 자격 증명 (credential) 그리덴쳐 객체를 만든다.
        auth?.signInWithCredential(credential)?.
            //Firebase 인증 객체에서 signInWithCredential()크리덴쳐 메서드를 호출하여 자격 증명을 사용하여 사용자를 인증한다.
        addOnCompleteListener {
            // 앞선 페이스북 로그인 성공시랑 동일하다
            it->
            if(it.isSuccessful){
                //Login Success
                // 로그인 작업이 완료되면 addOnCompleteListener를 호출하여
                //파이어 베이스의 로그인 성공여부를 확인한다
                Log.e(TAG,"signinEmail ")
                //Login Success 로그인 성공 후 로그 메세지 출력
                moveMainPage(it.result.user)
                // 메인화면으로 이동
            }
            else {
                //Show the error message
                Toast.makeText(this,it.exception?.message,Toast.LENGTH_SHORT).show()
                //로그인 실패할 경우 메세지 속성을 이용하여 실패이유에 대한 에러메세지를 출력한다.
            }
        }
    }


    fun signinAndSignup(){
        //로그인 회원가입
        auth?.createUserWithEmailAndPassword(login_email_edittext.text.toString(),login_pw_edittext.text.toString())?.addOnCompleteListener {
            //createUserWithEmailAndPassword() 메서드를 사용하여 사용자 계정을 만든다. 이메일 및 비밀번호는
            //login_email_edittext 및 login_pw_edittext EditText의 값에서 가져옵니다. 함수는 onCompleteListener를 사용하여 인증 결과를 처리합니다
             it->
            if(it.isSuccessful){
                saveFindIdData()
                Log.e(TAG,"signup ")
               // moveMainPage(it.result.user)
                //계정이 성공적으로 생성되면 moveMainPage() 함수를 호출하여 사용자를 앱의 메인 화면으로 이동
                //Create a user account
            }else if(it.exception?.message.isNullOrEmpty()){
                //Show the error message
                Log.e(TAG,"signup null ")
                Toast.makeText(this,it.exception?.message,Toast.LENGTH_SHORT).show()
                //계정 생성이 실패하면 exception의 메시지를 사용하여 사용자에게 오류 메시지를 표시합니다.

            }else{
                Log.e(TAG,"signinEmail first")
                signinEmail()
                //Login if you have account
                // 오류 메시지가 없는 경우, signinEmail() 함수를 호출하여 이미 생성된 계정으로 로그인

            }
        }

    }

    fun saveFindIdData(){
        finish()
        startActivity(Intent(this, InputNumberActivity::class.java))
    }

    fun findIdPasswordButton(){
        finish()
        startActivity(Intent(this, FindIdActivity::class.java))
    }

    fun signinEmail(){
        auth?.signInWithEmailAndPassword(login_email_edittext.text.toString(),login_pw_edittext.text.toString())?.
            //signInWithEmailAndPassword() 메서드를 사용하여 사용자 계정으로 로그인한다
            // 이메일 및 비밀번호는 login_email_edittext 및 login_pw_edittext EditText의 값에서 가져온다
        addOnCompleteListener {
            // onCompleteListener를 사용하여 인증 결과를 처리한다

            it->
            if(it.isSuccessful){
                //Login Success
                Log.e(TAG,"signinEmail second")
                moveMainPage(it.result.user)
                // 로그인 성공시 Login Success 출력 후 메인페이지로 이동
            }
            else {
                //Show the error message
                Toast.makeText(this,it.exception?.message,Toast.LENGTH_SHORT).show()
                //인증이 실패하면 exception의 메시지를 사용하여 사용자에게 오류 메시지를 표시
            }

        }

    }


    fun moveMainPage(user: FirebaseUser?){
        //moveMainPage() 함수는 FirebaseUser 객체를 매개 변수로 받고 FirebaseUser 객체는 Firebase 인증에 성공한 사용자의 인증 정보를 나타낸다
        if(user!=null){//user 객체가 null이 아니면,
            Log.e(TAG,"movemainpage ")
            startActivity(Intent(this,MainActivity::class.java))
            // startActivity() 함수를 사용하여 MainActivity로 이동 후 현재 액티비티를 종료한다.
            finish()
        }


    }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // 터치이벤트 키보드
        // dispatchTouchEvent() 함수는 모션 이벤트(MotionEvent)를 매개 변수로 받고 영역 내부에 터치 이벤트가 발생하는지 확인
        val focusView: View? = currentFocus
        // 뷰안에서 포커스 이벤트가 발생
        if (focusView != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            // 포커스 뷰의 직사각형 영역 밖에서 이벤트 발생할경우
            if (!rect.contains(x, y)) {
                val imm: InputMethodManager =
                    //객체를 사용하여 키보드를 숨기고 뷰포커스를 제거
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
        // return super.dispatchTouchEvent(ev)호출히여 다른 이벤트 처리를 할당한다.
    }

}