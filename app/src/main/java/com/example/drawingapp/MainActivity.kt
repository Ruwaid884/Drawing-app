package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.hardware.biometrics.BiometricManager.Strings
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
private var currentpaint:ImageButton?=null
    var customProgressDialog:Dialog? =null
    val gallerylauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result->
            if(result.resultCode == RESULT_OK && result.data!=null){
                val imagebackground: ImageView = findViewById(R.id.background)
                imagebackground.setImageURI(result.data?.data)

            }
        }

val requestpermission: ActivityResultLauncher<Array<String>> = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()){
    permissions->
    permissions.entries.forEach {
        val permissionName =it.key
        val isGranted = it.value
        if(isGranted){
            Toast.makeText(this,"permission granted",Toast.LENGTH_SHORT).show()
            val pickintent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            gallerylauncher.launch(pickintent)
        }
        else{
            if(permissionName==Manifest.permission.READ_EXTERNAL_STORAGE)
                Toast.makeText(this,"oops you just denied the permission",Toast.LENGTH_SHORT).show()
        }

    }
}
    private var drawingview: DrawingView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingview = findViewById(R.id.drawingscreen)
        drawingview?.setsizebrush(20.toFloat())
        val linearLayoutpaintColors = findViewById<LinearLayout>(R.id.paint_colors)
        currentpaint=linearLayoutpaintColors[1] as ImageButton
        currentpaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
        )
        val brush : ImageButton =findViewById(R.id.brush)
        brush.setOnClickListener{
            showBrushSizeChooserDialog()
        }
        val undo : ImageButton =findViewById(R.id.undo)
        undo.setOnClickListener{
           drawingview?.onclickundo()
        }
        val redo : ImageButton =findViewById(R.id.redo)
        redo.setOnClickListener{
            drawingview?.onclickredo()
        }

        val save : ImageButton =findViewById(R.id.save)
        save.setOnClickListener{

            if(isReadStorageAllowed()){
                lifecycleScope.launch{
                    showprogressDialog()
                    val framelayout:FrameLayout=findViewById(R.id.container)
                    savebitmap(getbitmapfromview(framelayout))
                }
            }
        }



        val gallery : ImageButton=findViewById(R.id.gallery)
        gallery.setOnClickListener{
requestphotoselection()
        }
    }
    private fun showBrushSizeChooserDialog(){

        val brushDialog = Dialog(this)
         brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")
        val xxxsmallBtn : ImageButton =brushDialog.findViewById(R.id.brush_size_xxxsmall)
        xxxsmallBtn.setOnClickListener{
            drawingview?.setsizebrush(2.toFloat())
            brushDialog.dismiss()
        }
        val xxsmallBtn : ImageButton =brushDialog.findViewById(R.id.brush_size_xxsmall)
        xxsmallBtn.setOnClickListener{
            drawingview?.setsizebrush(3.toFloat())
            brushDialog.dismiss()
        }
        val xsmallBtn : ImageButton =brushDialog.findViewById(R.id.brush_size_xsmall)
        xsmallBtn.setOnClickListener{
            drawingview?.setsizebrush(5.toFloat())
            brushDialog.dismiss()
        }
        val smallBtn : ImageButton =brushDialog.findViewById(R.id.brush_size_small)
        smallBtn.setOnClickListener{
            drawingview?.setsizebrush(10.toFloat())
            brushDialog.dismiss()
        }
        val MedBtn : ImageButton =brushDialog.findViewById(R.id.brush_size_medium)
        MedBtn.setOnClickListener{
            drawingview?.setsizebrush(20.toFloat())
            brushDialog.dismiss()
        }
        val largeBtn : ImageButton =brushDialog.findViewById(R.id.brush_size_large)
        largeBtn.setOnClickListener{
            drawingview?.setsizebrush(30.toFloat())
            brushDialog.dismiss()
        }

  brushDialog.show()
    }

    fun paintcliked(view: View){
      if(view != currentpaint){
          val imageButton = view as ImageButton
          val colorTag = imageButton.tag.toString()
          drawingview?.setcolor(colorTag)
          imageButton.setImageDrawable(
              ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
          )
          currentpaint?.setImageDrawable(
              ContextCompat.getDrawable(this,R.drawable.pallet_normal)
          )
          currentpaint =view
      }
    }
    private fun showrationaldialoge(title: String, message:String){
        val builder: AlertDialog.Builder= AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("cancel"){
            dialog,_,->dialog.dismiss()
        }
        builder.create().show()

    }

    private fun isReadStorageAllowed(): Boolean{
        val result= ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
   return result== PackageManager.PERMISSION_GRANTED
    }

    private fun requestphotoselection(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            showrationaldialoge("Kids drawing app","this app needs to access your gallery for background image")
        }
        else {
            requestpermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE
            ,Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }






    private fun getbitmapfromview(view : View):Bitmap{
        val returnedBitmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgdrawable= view.background
        if(bgdrawable != null){
            bgdrawable.draw(canvas)
        }
        else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap


    }
    private suspend fun savebitmap(mbitmap: Bitmap?):String{
        var result =""
        withContext(Dispatchers.IO){
            if(mbitmap!=null){
                try {
                    val bytes = ByteArrayOutputStream()
                    mbitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
               val f= File(externalCacheDir?.absoluteFile.toString()
               +File.separator+"KidsDrawingApp_" + System.currentTimeMillis()/1000 + ".png")
                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath

                    runOnUiThread{
                        if(result.isNotEmpty()){
                            cancelprogressDialog()
                        Toast.makeText(this@MainActivity,"file saved:$result",Toast.LENGTH_SHORT).show()
                        shareImage(result)
                        }
                        else{

                            Toast.makeText(
                                this@MainActivity,"something went wrong",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                catch (e: Exception){
                    result = ""
                    e.printStackTrace()
                }
            }
            }
        return  result
        }



    private fun showprogressDialog(){
        customProgressDialog = Dialog(this@MainActivity)

        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)

        customProgressDialog?.show()
    }


    private fun cancelprogressDialog(){
        if(customProgressDialog!=null){
            customProgressDialog?.dismiss()
        }
        customProgressDialog=null
    }


    private fun shareImage(result: String){
        MediaScannerConnection.scanFile(this, arrayOf(result), null){
            path,uri->
            val shareIntent = Intent()
            shareIntent.action=Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
            shareIntent.type= "image/png"
            startActivity(Intent.createChooser(shareIntent,"Share"))
        }
    }
}