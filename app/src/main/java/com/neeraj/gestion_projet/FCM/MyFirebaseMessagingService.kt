package com.neeraj.gestion_projet.FCM

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.neeraj.gestion_projet.R
import com.neeraj.gestion_projet.activities.IntroActivity
import com.neeraj.gestion_projet.activities.MainActivity
import com.neeraj.gestion_projet.firebase.FirestoreClass
import com.neeraj.gestion_projet.utils.Constants

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        Log.d(TAG,"Messages From: ${p0.from}")
        p0.data.isNotEmpty().let {
            Log.d(TAG,"Messages data payload: ${p0.data}")
            val title=p0.data[Constants.FCM_KEY_TITLE]!!
            val message=p0.data[Constants.FCM_KEY_MESSAGE]!!

            sendNotification(title,message)
        }

        p0.notification.let {
            Log.d(TAG,"Messages Notification Body: ${it?.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e(TAG,"Refreshed Token: ${token}")
        sendRegistrationtoServer(token)
    }
    private fun sendRegistrationtoServer(token:String?){

    }

    private fun sendNotification(title:String,message:String){
        val intent= if(FirestoreClass().getCurrentUserID().isNotEmpty()){
            Intent(this,MainActivity::class.java)
        }else{
            Intent(this,IntroActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)
        val channelID=resources.getString(R.string.default_notification_channel_id)
        val defaultSoundURI=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder=NotificationCompat.Builder(this,channelID).setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title).setContentText(message).setAutoCancel(true).setSound(defaultSoundURI)
            .setContentIntent(pendingIntent)

        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channel=NotificationChannel(channelID,"Channel Gestion_Projet title",NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0,notificationBuilder.build())
    }

    companion object{
        private const val TAG="MyFirebaseMsgService"
    }

}