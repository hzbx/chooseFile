package com.newki.choosefile.detector

import android.annotation.SuppressLint
import android.provider.DocumentsContract
import android.system.Os.close
import android.util.Log
import com.newki.choosefile.ChooseFile
import com.newki.choosefile.ChooseFileInfo
import com.newki.choosefile.utils.FileUtil

/**
 * Android10以上的处理，获取文件
 */
internal class ChooseFileHighPolicy : IChooseFilePolicy {

    @SuppressLint("Range")
    override fun getFileList(rootPath: String, callback: (fileList: List<ChooseFileInfo>, topInfo: ChooseFileInfo?) -> Unit) {

        val uri = DocumentsContract.buildChildDocumentsUri(
            "com.newki.choosefile.authorities",
            rootPath
        )

        ChooseFile.config?.mExecutor?.execute {

            val cursor = ChooseFile.activityRef?.get()?.contentResolver?.query(uri, null, null, null, null)

            val listData: ArrayList<ChooseFileInfo> = ArrayList()
            var topInfo: ChooseFileInfo? = null

            if (cursor != null) {

                while (cursor.moveToNext()) {

                    val isTop = cursor.getInt(cursor.getColumnIndex("isTop"))
                    val isRoot = cursor.getInt(cursor.getColumnIndex("isRoot"))

                    val fileName = cursor.getString(cursor.getColumnIndex("fileName"))
                    val isDir = cursor.getInt(cursor.getColumnIndex("isDir"))
                    val fileSize = cursor.getString(cursor.getColumnIndex("fileSize"))
                    val fileLastUpdateTime = cursor.getString(cursor.getColumnIndex("fileLastUpdateTime"))
                    val filePath = cursor.getString(cursor.getColumnIndex("filePath"))
                    val filePathUri = cursor.getString(cursor.getColumnIndex("filePathUri"))
                    val fileType = cursor.getString(cursor.getColumnIndex("fileType"))
                    val fileTypeIconRes = cursor.getInt(cursor.getColumnIndex("fileTypeIconRes"))

                    if (isTop == 1) {

                        if (isRoot == 0) {
                            topInfo = ChooseFileInfo().apply {
                                this.fileName = fileName
                                this.isDir = isDir != 0
                                this.fileSize = fileSize
                                this.fileLastUpdateTime = fileLastUpdateTime
                                this.filePath = filePath
                                this.filePathUri = filePathUri
                                this.fileTypeIconRes = fileTypeIconRes
                            }
                        }

                    } else {

                        listData.add(ChooseFileInfo().apply {
                            this.fileName = fileName
                            this.isDir = isDir != 0
                            this.fileSize = fileSize
                            this.fileLastUpdateTime = fileLastUpdateTime
                            this.filePath = filePath
                            this.filePathUri = filePathUri
                            this.fileTypeIconRes = fileTypeIconRes
                            this.fileType = fileType
                        })
                    }

                }

                cursor.close()

                //根据Filter过滤数据并排序
                val filterData = ChooseFile.config?.mIFileTypeFilter?.doFilter(listData) ?: listData
                FileUtil.SortFilesByInfo(filterData)

                //满数据回调
                callback(filterData, topInfo)

            } else {

                callback(emptyList(), null)

            }

        }

    }
}