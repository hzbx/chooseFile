package com.newki.sample

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import com.newki.choosefile.ChooseFile
import com.newki.choosefile.ChooseFileInfo
import com.newki.choosefile.ChooseFileUIConfig
import com.newki.choosefile.IFileTypeFilter
import java.io.File


class DemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_demo)

        findViewById<Button>(R.id.btn_get_file).setOnClickListener {

            ChooseFile.create(this)
                .setUIConfig(ChooseFileUIConfig.Builder().build())
                .setTypeFilter { listData ->
                    return@setTypeFilter ArrayList(listData.filter { item ->
                        //只要文件夹
//                          item.isDir

                        //只要文档文件
                        item.fileType == ChooseFile.FILE_TYPE_FOLDER ||
                                item.fileType == ChooseFile.FILE_TYPE_TEXT ||
                                item.fileType == ChooseFile.FILE_TYPE_PDF
                    })
                }
                .forResult {
                    Toast.makeText(this, "选中的文件：" + it?.fileName, Toast.LENGTH_SHORT).show()
                    val uri = Uri.parse(it?.filePathUri)
                    val fis = contentResolver.openInputStream(uri)

                    Log.w("TAG", "文件的Uri:" + it?.filePathUri + " uri:" + uri + " fis:" + fis)

                    fis?.close()
                }
        }
    }

}