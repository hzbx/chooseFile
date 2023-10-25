package com.newki.choosefile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference


/**
 * 入口
 */
object ChooseFile {

    //文件类型
    const val FILE_TYPE_FOLDER = "Folder"
    const val FILE_TYPE_VIDEO = "Folder"
    const val FILE_TYPE_AUDIO = "Audio"
    const val FILE_TYPE_IMAGE = "Image"
    const val FILE_TYPE_TEXT = "Text"
    const val FILE_TYPE_XLS = "XLS"
    const val FILE_TYPE_PPT = "PPT"
    const val FILE_TYPE_PDF = "PDF"
    const val FILE_TYPE_Unknown = "Unknown"

    @JvmField
    internal var activityRef: WeakReference<FragmentActivity>? = null

    @JvmField
    internal var config: ChooseFileConfig? = null

    @JvmStatic
    fun create(activity: FragmentActivity): ChooseFileConfig {
        activityRef?.clear()
        this.activityRef = WeakReference(activity)
        config = ChooseFileConfig(this)
        return config!!
    }

    @JvmStatic
    fun create(fragment: Fragment): ChooseFileConfig {
        activityRef?.clear()
        val activity = fragment.requireActivity()
        this.activityRef = WeakReference(activity)
        config = ChooseFileConfig(this)
        return config!!
    }

    @JvmStatic
    fun release() {
        activityRef?.clear()
        config?.clear()
        config = null
    }
}