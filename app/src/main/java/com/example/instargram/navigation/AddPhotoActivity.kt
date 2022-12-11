package com.example.instargram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.instargram.R
import com.example.instargram.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firesotore : FirebaseFirestore? = null
    lateinit var getResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)
        //startActivityForResult(photoPickerIntnet,PICK_IMAGE_FROM_ALBUM)

        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                //이미지 경로 넘어옴
                photoUri = result.data?.data
                addphoto_image.setImageURI(photoUri)
            }else{
                //취소버튼
                this.finish()
            }
        }

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firesotore = FirebaseFirestore.getInstance()

        val photoPickIntent = Intent(Intent.ACTION_PICK)
        photoPickIntent.type = "image/*"
        getResult.launch(photoPickIntent)

        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    fun contentUpload(){
        Log.d("hwkang","contentUpload");
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        /*storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            contentDTO.imageUrl = uri.toString()
            contentDTO.uid = auth?.currentUser?.uid
            contentDTO.userid = auth?.currentUser?.email
            contentDTO.explain = addphoto_edit_explain.text.toString()
            contentDTO.timestamp = System.currentTimeMillis()
            firesotore?.collection("images")?.document()?.set(contentDTO)
            setResult(Activity.RESULT_OK)
            finish()
        }*/

        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            //Toast.makeText(this,getString(R.string.upload_success),Toast.LENGTH_LONG).show()
            storageRef.downloadUrl.addOnSuccessListener { it ->
                var contentDTO = ContentDTO()

                contentDTO.imageUrl = it.toString()
                contentDTO.uid = auth?.currentUser?.uid
                contentDTO.userid = auth?.currentUser?.email
                contentDTO.explain = addphoto_edit_explain.text.toString()
                contentDTO.timestamp = System.currentTimeMillis()
                firesotore?.collection("images")?.document()?.set(contentDTO)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}