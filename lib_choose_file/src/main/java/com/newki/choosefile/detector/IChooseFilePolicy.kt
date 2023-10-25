package com.newki.choosefile.detector

import com.newki.choosefile.ChooseFileInfo

/**
 * 不同版本获取文件的策略方式
 */
internal interface IChooseFilePolicy {

    fun getFileList(rootPath: String, callback: (fileList: List<ChooseFileInfo>, topInfo: ChooseFileInfo?) -> Unit)

}