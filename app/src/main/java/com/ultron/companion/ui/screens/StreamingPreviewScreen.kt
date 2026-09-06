package com.ultron.companion.ui.screens
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ultron.companion.system.ScreenMirrorState
import com.ultron.companion.utils.MediaProjectionHelper
@Composable
fun StreamingPreviewScreen(){
 val context=LocalContext.current
 val bytes by ScreenMirrorState.jpeg.collectAsState()
 var active by remember{mutableStateOf(ScreenMirrorState.active)}
 val launcher=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){r->
  if(r.resultCode==Activity.RESULT_OK&&r.data!=null){
   context.startForegroundService(MediaProjectionHelper.startServiceIntent(context,r.resultCode,r.data!!));active=true
  }
 }
 LaunchedEffect(bytes){if(bytes!=null)active=true}
 Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  Text("Screen Mirror",style=MaterialTheme.typography.headlineSmall)
  bytes?.let{b->BitmapFactory.decodeByteArray(b,0,b.size)?.let{bmp->Image(bmp.asImageBitmap(),"ULTRON screen preview",Modifier.fillMaxWidth().weight(1f))}}
    ?: Text(if(active)"Waiting for the first frame…" else "Screen capture is stopped.")
  Button(onClick={if(active){context.stopService(Intent(context,com.ultron.companion.services.ScreenMirrorService::class.java));active=false}else launcher.launch(MediaProjectionHelper.createCaptureIntent(context))}){Text(if(active)"Stop Screen Mirror" else "Start Screen Mirror")}
 }
}
