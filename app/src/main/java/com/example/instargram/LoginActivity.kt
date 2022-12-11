package com.example.instargram

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthOptions

class LoginActivity : AppCompatActivity() {

    val TAG : String = "Insta"
    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    var callbackManager : CallbackManager? = null

    var email : String? = ""
    var password : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        email_login_button.setOnClickListener{
            signinAndsignup()
        }

        google_sign_in_button.setOnClickListener {
            gooleLogin()
        }

        facebook_login_button.setOnClickListener{
            facebookLogin()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            //.requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        //printHashKey() //페이스북 해쉬키 생성한 거 확인하는 코드
        callbackManager = CallbackManager.Factory.create()

        //startActivity(Intent(this,MainActivity::class.java))

    }

    override fun onStart() {
        super.onStart()
        val account = this?.let { GoogleSignIn.getLastSignedInAccount(it) }
        if (account!=null){
            moveMainPage(auth?.currentUser)
        }
    }

    //r01udmaDuK3juBhLwCLxcfrSxbo=
    fun printHashKey() {
        try {
            val info: PackageInfo = packageManager.getPackageInfo(packageName,PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey: String = String(Base64.encode(md.digest(), 0))
                Log.i(TAG, "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "printHashKey()", e)
        } catch (e: Exception) {
            Log.e(TAG, "printHashKey()", e)
        }
    }

    fun gooleLogin(){
        val signInIntent = googleSignInClient?.signInIntent
        Log.d(TAG,"gooleLogin()");
        //startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
        resultLauncher.launch(signInIntent)
    }

    /*var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("LOG","resultCode : ${result.resultCode}")
        if (result.resultCode == 1) {
            val data: Intent? = result.data
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }*/

    var resultLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()){ result ->
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task)
    }

    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>){
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account?.email.toString()
            val familyName = account?.familyName.toString()
        } catch (e: ApiException){
            Log.w("failed", "signInResult:failed code=" + e.statusCode)
        }
    }

    /*fun handleSignInResult(completedTask: Task<GoogleSignInAccount>){
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account?.email.toString()
            val familyName = account?.familyName.toString()

            Log.i("LOG","account : $account   , email : $email ,  familyName : $familyName")
        } catch (e: ApiException){
            Log.w("failed", "signInResult:failed code=" + e.statusCode)
        }
    }*/

    /*fun facebookLogin(){
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile","email"))
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    handleFacebookAccessToken(result?.accessToken)
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                }
            })
    }*/

    fun facebookLogin(){
        Log.d("facebookLogin","facebookLogin()")
        //LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"))
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    Log.d("facebookLogin","facebookLogin onSuccess")
                    handleFacebookAccessToken(result?.accessToken)
                    //로그인 성공시 값을 파이어베이스에 넘겨줌
                }

                override fun onCancel() {
                    Log.d("facebookLogin","facebookLogin onCancel")
                }

                override fun onError(error: FacebookException?) {
                    Log.d("facebookLogin","facebookLogin onError")
                }

            })
    }

    fun handleFacebookAccessToken(token : AccessToken?){
        val credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{
                    it->
                if(it.isSuccessful){
                    //아이디가 생성 되었을 떄
                    moveMainPage(it.result?.user)
                }  else {
                    //틀렸을 떄
                    Toast.makeText(this,it.exception?.message,Toast.LENGTH_LONG).show()
                }
            }

    }

    /*//구글 로그인 이전 사양 현재는 못쓴다.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode,resultCode,data)
        Log.d(TAG,"onActivityResult() : $requestCode  :  $GOOGLE_LOGIN_CODE  data : $data");
        if(requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            Log.d(TAG,"result : $result")
            if(result!!.isSuccess){
                Log.d(TAG,"gooleLogin() isSuccess");
                var account = result.signInAccount
                firebaseAuthWithGoole(account!!)
            }
        }
    }

    fun firebaseAuthWithGoole(account : GoogleSignInAccount){
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{
                    it->
                if(it.isSuccessful){
                    //아이디가 생성 되었을 떄
                    moveMainPage(it.result?.user)
                }  else {
                    //틀렸을 떄
                    Toast.makeText(this,it.exception?.message,Toast.LENGTH_LONG).show()
                }
            }
    }*/

    fun signinAndsignup(){

        email = when {
            !email_edittext.text.toString().isNullOrEmpty() ->
                email_edittext.text.toString()
            else -> " "
        }

        password = when {
            !password_edittext.text.toString().isNullOrEmpty() ->
                password_edittext.text.toString()
            else -> " "
        }

        Log.d(TAG,"Email : $email   password : $password");

        auth?.createUserWithEmailAndPassword(email!!,password!!)
            ?.addOnCompleteListener{
                it->
                    if(it.isSuccessful){
                        //아이디가 생성 되었을 떄
                        moveMainPage(it.result?.user)
                    } else if(it.exception?.message.isNullOrEmpty()) {
                        //아이디 생성 시에 발생한 에러 로그
                        Toast.makeText(this,it.exception?.message,Toast.LENGTH_LONG).show()
                    } else {
                        // Login if you have account
                        signinEmail()
                    }
            }
    }
    fun signinEmail(){
        auth?.signInWithEmailAndPassword(email!!,password!!)
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful){
                    //Login
                    moveMainPage(task.result?.user)
                }else{
                    //Show the error message
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
            }
    }

    fun moveMainPage(user : FirebaseUser?){
        if(user != null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}
