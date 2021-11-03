package com.neeraj.gestion_projet.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat.startActivityForResult
import com.neeraj.gestion_projet.activities.MyProfileActivity

object Constants {
    const val users: String = "Users"

    const val boards: String = "boards"

    const val image: String = "image"
    const val NAME: String ="name"
    const val Mobile:String = "phone"

    const val ASSIGNED_TO:String="assignedTo"
    const val DOCUMENT_ID:String="docID"
    const val TASK_LIST:String="taskList"
    const val BOARD_DETAIL:String="boardDetail"
    const val ID:String="id"
    const val EMAIL:String="email"
    const val TASK_LIST_ITEM_POSITION:String="task_list_item_position"
    const val CARD_LIST_ITEM_POSITION:String="card_list_item_position"
    const val BOARD_MEMBERS_LIST:String="board_members_list"
    const val SELECT: String="Select"
    const val UNSELECT:String="Unselect"

    const val GESTIONPROJET_PREFS="gestionprojet preferences"
    const val FCM_TOKEN_UPDATED="fcm_token_updated"
    val FCM_TOKEN="fcmToken"

    const val FCM_BASE_URL:String="https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String="authorization"
    const val FCM_KEY:String="key"
    const val FCM_SERVER_KEY:String="AAAAmvVWIfY:APA91bHIv0ZnAAQju09qDwnmfzQdY_rucaIf7pnXaHPLP5JoN9B3-lVK5MjK3q9rvcqdTLxvNPD0Teu86R4-NIo_ajGC8yzvamJCE0OZx60_H9lvOZgeVosCEkrkYrdqeAVuPhQCF4a6"
    const val FCM_KEY_TITLE:String="title"
    const val FCM_KEY_MESSAGE:String="message"
    const val FCM_KEY_DATA:String="data"
    const val FCM_KEY_TO:String="to"

    const val READ_STORAGE_PERMISSION=1
    const val PICK_IMAGE_PERMISSION=2

     fun showImageChooser(activity:Activity)
    {
        var galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_PERMISSION)
    }

     fun getFileExtension(activity:Activity,uri: Uri?):String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}
