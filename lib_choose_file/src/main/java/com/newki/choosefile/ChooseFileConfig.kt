package com.newki.choosefile

import android.util.Log
import com.newki.choosefile.ghost.gotoActivityForResult
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ChooseFileConfig(private val chooseFile: ChooseFile) {

    internal var mUIConfig: ChooseFileUIConfig? = null
    internal var mIFileTypeFilter: IFileTypeFilter? = null
    internal var mExecutor: ExecutorService? = ThreadPoolExecutor(
        1, 1, 10L, TimeUnit.MINUTES, LinkedBlockingDeque()
    )

    fun setUIConfig(uiConfig: ChooseFileUIConfig?): ChooseFileConfig {
        mUIConfig = uiConfig
        return this
    }

    fun setExecutor(executor: ExecutorService): ChooseFileConfig {
        mExecutor = executor
        return this
    }

    fun getExecutor(): ExecutorService? {
        return mExecutor
    }

    fun setTypeFilter(filter: IFileTypeFilter): ChooseFileConfig {
        mIFileTypeFilter = filter
        return this
    }

    fun forResult(listener: IFileChooseListener) {
        val activity = chooseFile.activityRef?.get()
        activity?.gotoActivityForResult<ChooseFileActivity> {
            it?.run {
                val info = getSerializableExtra("chooseFile") as ChooseFileInfo
                listener.doChoose(info)
            }
        }
    }

    //销毁资源
    fun clear() {
        mUIConfig = null
        mIFileTypeFilter = null
        if (mExecutor != null && !mExecutor!!.isShutdown) {
            mExecutor!!.shutdown()

        }

    }

}