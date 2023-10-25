package com.newki.choosefile.detector

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import com.newki.choosefile.ChooseFile
import com.newki.choosefile.ChooseFileInfo
import com.newki.choosefile.R
import com.newki.choosefile.provider.ChooseFileProvider
import com.newki.choosefile.utils.FileUtil
import com.newki.choosefile.utils.TimeUtil
import java.io.File
import java.util.Date

/**
 * Android10以下的处理，获取文件夹
 */
internal class ChooseFileLowPolicy : IChooseFilePolicy {

    override fun getFileList(rootPath: String, callback: (fileList: List<ChooseFileInfo>, topInfo: ChooseFileInfo?) -> Unit) {

        ChooseFile.config?.mExecutor?.execute {

            val listData: ArrayList<ChooseFileInfo> = ArrayList()
            val rootFile = File(rootPath)
            var topInfo: ChooseFileInfo? = null
            val rootExternalPath = Environment.getExternalStorageDirectory().absolutePath
            if (rootExternalPath != rootPath) {
                //添加一个顶部的导航对象
                topInfo = ChooseFileInfo().apply {
                    fileName = rootFile.name
                    filePath = rootFile.absolutePath
                    isDir = true
                }
            }

            val listFiles = rootFile.listFiles()
            if (listFiles.isNullOrEmpty()) {
                //空数据回调
                callback(listData, topInfo)
                return@execute
            }

            for (file in listFiles) {
                if (file.isDirectory) {
                    //如果是文件夹
                    listData.add(
                        ChooseFileInfo().apply {
                            isDir = true
                            fileName = file.name
                            filePath = file.absolutePath
                            fileLastUpdateTime = TimeUtil.getDateInString(Date(file.lastModified()))
                            fileSize = "共" + FileUtil.getSubfolderNum(file.absolutePath) + "项"
                            fileType = ChooseFile.FILE_TYPE_FOLDER
                            fileTypeIconRes = R.drawable.file_folder
                        }
                    )
                } else {

                    //根据后缀类型封装自定义文件Bean
                    val fileInfo = ChooseFileInfo()

                    if (FileUtil.isAudioFileType(file.absolutePath)) {
                        fileInfo.fileType = ChooseFile.FILE_TYPE_AUDIO
                        fileInfo.fileTypeIconRes = R.drawable.file_audio

                    } else if (FileUtil.isImageFileType(file.absolutePath)) {
                        fileInfo.fileType = ChooseFile.FILE_TYPE_IMAGE
                        fileInfo.fileTypeIconRes = R.drawable.file_image

                    } else if (FileUtil.isVideoFileType(file.absolutePath)) {
                        fileInfo.fileType = ChooseFile.FILE_TYPE_VIDEO
                        fileInfo.fileTypeIconRes = R.drawable.file_video

                    } else if (FileUtil.isTextFileType(file.absolutePath)) {
                        fileInfo.fileType = ChooseFile.FILE_TYPE_TEXT
                        fileInfo.fileTypeIconRes = R.drawable.file_text

                    } else if (FileUtil.isXLSFileType(file.absolutePath)) {
                        fileInfo.fileType = ChooseFile.FILE_TYPE_XLS
                        fileInfo.fileTypeIconRes = R.drawable.file_excel

                    } else if (FileUtil.isPPTFileType(file.absolutePath)) {
                        fileInfo.fileType = ChooseFile.FILE_TYPE_PPT
                        fileInfo.fileTypeIconRes = R.drawable.file_ppt

                    } else if (FileUtil.isPDFFileType(file.absolutePath)) {
                        fileInfo.fileType = ChooseFile.FILE_TYPE_PDF
                        fileInfo.fileTypeIconRes = R.drawable.file_pdf

                    } else {
                        fileInfo.fileType = ChooseFile.FILE_TYPE_Unknown
                        fileInfo.fileTypeIconRes = R.drawable.file_unknown
                    }

                    fileInfo.apply {
                        isDir = false
                        fileName = file.name
                        filePath = file.absolutePath
                        filePathUri = getFileUri(ChooseFile.activityRef?.get(), file).toString()
                        fileLastUpdateTime = TimeUtil.getDateInString(Date(file.lastModified()))
                        fileSize = FileUtil.getFileSize(file.length())
                    }

                    listData.add(fileInfo)
                }
            }

            //根据Filter过滤数据并排序
            val filterData = ChooseFile.config?.mIFileTypeFilter?.doFilter(listData) ?: listData
            FileUtil.SortFilesByInfo(filterData)

            //满数据回调
            callback(filterData, topInfo)
        }

    }


    fun getFileUri(context: Context?, file: File): Uri {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ChooseFileProvider.getUriForFile(context, "com.newki.choosefile.file.path.share", file)
        } else Uri.fromFile(file)
    }
}