package com.newki.choosefile.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/**
 * Desc:
 * <p>
 * Date: 2023/10/26
 * Copyright: Copyright (c) 2010-2020
 * Company: @微微科技有限公司
 * Updater:
 * Update Time:
 * Update Comments:
 *
 * @Author: [huangzhibo]
 */
class TestProvider: ContentProvider() {
    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        return MatrixCursor(arrayOf("huangzhibo"),2)
    }

    override fun getType(uri: Uri): String? {
        return "hzb"
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 1008
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 1008
    }
}