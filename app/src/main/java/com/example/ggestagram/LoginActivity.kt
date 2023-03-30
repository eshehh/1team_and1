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

    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var callbackManager : CallbackManager? = null


    private val GOOGLE_LOGIN_CODE = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()


        login_btn1.setOnClickListener(this)
        login_btn11.setOnClickListener(this)
        login_btn2.setOnClickListener(this)
        login_btn3.setOnClickListener(this)




        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("564761092046-mcu3f1lgrboiuhijs8ikrtr6kkrtfsvg.apps.googleusercontent.com")
            .requestEmail()
            .build()


        googleSignInClient = GoogleSignIn.getClient(this,gso)
        callbackManager = CallbackManager.Factory.create()
        //printHashKey()*/

    }


    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.login_btn1 -> signinAndSignup()
            R.id.login_btn2 -> facebookLogin()
            R.id.login_btn3 -> googleLogin()
            R.id.login_btn11 -> findIdPasswordButton()
        }
    }

    /*fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName,PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray());
                val hashKey = String(Base64.encode(md.digest(),0))
                Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "printHashKey()", e);
        } catch (e: Exception) {
            Log.e(TAG, "printHashKey()", e);
        }
    }*/



    fun googleLogin(){
        var signinIntent = googleSignInClient!!.signInIntent
        loginLauncher.launch(signinIntent)
    }
    private val loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.e("REQSULT CODE Start= ",it.resultCode.toString())
        if(it.resultCode == Activity.RESULT_OK){
            val task = Auth.GoogleSignInApi.getSignInResultFromIntent(it.data!!)
            if(task!!.isSuccess){
                var account = task.signInAccount
                firebaseAuthWithGoogle(account)
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
        LoginManager.getInstance()
            .registerCallback(callbackManager,object: FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    handleFaceBookAccessToken(result?.accessToken)
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {

                }
            })
    }



    fun handleFaceBookAccessToken(token: AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)?.
        addOnCompleteListener {
                it->
            if(it.isSuccessful){
                //Login Success
                Log.e(TAG,"signinEmail ")
                moveMainPage(it.result.user)
            }
            else {
                //Show the error message
                Toast.makeText(this,it.exception?.message,Toast.LENGTH_SHORT).show()

            }

        }

    }

    override fun onActivityResult(requestCode:Int , resultCode : Int, data:Intent?){
        callbackManager?.onActivityResult(requestCode,resultCode,data)
        super.onActivityResult(requestCode,resultCode,data)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)?.
        addOnCompleteListener {
                it->
            if(it.isSuccessful){
                //Login Success
                Log.e(TAG,"signinEmail ")
                moveMainPage(it.result.user)
            }
            else {
                //Show the error message
                Toast.makeText(this,it.exception?.message,Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun signinAndSignup(){
        auth?.createUserWithEmailAndPassword(login_email_edittext.text.toString(),login_pw_edittext.text.toString())?.addOnCompleteListener {
             it->
            if(it.isSuccessful){
                saveFindIdData()
                Log.e(TAG,"signup ")
               // moveMainPage(it.result.user)
                //Create a user account
            }else if(it.exception?.message.isNullOrEmpty()){
                //Show the error message
                Log.e(TAG,"signup null ")
                Toast.makeText(this,it.exception?.message,Toast.LENGTH_SHORT).show()
            }else{
                Log.e(TAG,"signinEmail first")
                signinEmail()
                //Login if you have account
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
        addOnCompleteListener {
                it->
            if(it.isSuccessful){
                //Login Success
                Log.e(TAG,"signinEmail second")
                moveMainPage(it.result.user)
            }
            else {
                //Show the error message
                Toast.makeText(this,it.exception?.message,Toast.LENGTH_SHORT).show()

            }

        }

    }


    fun moveMainPage(user: FirebaseUser?){
        if(user!=null){
            Log.e(TAG,"movemainpage ")
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }


    }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val focusView: View? = currentFocus
        if (focusView != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            if (!rect.contains(x, y)) {
                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

}