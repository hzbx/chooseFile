package com.newki.choosefile.ghost

import android.content.Intent
import androidx.fragment.app.FragmentActivity

/**
 * 管理GhostFragment用于StartActivityForResult
 * 启动的时候添加Fragment 返回的时移除Fragment
 */
object Ghost {
    var requestCode = 0
        set(value) {
            field = if (value >= Integer.MAX_VALUE) 1 else value
        }

    inline fun launchActivityForResult(
        starter: FragmentActivity?,
        intent: Intent,
        crossinline callback: ((result: Intent?) -> Unit)
    ) {
        starter ?: return
        val fm = starter.supportFragmentManager
        val fragment = GhostFragment()
        fragment.init(++requestCode, intent) { result ->
            callback(result)
            fm.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
        fm.beginTransaction().add(fragment, GhostFragment::class.java.simpleName)
            .commitAllowingStateLoss()
    }

}