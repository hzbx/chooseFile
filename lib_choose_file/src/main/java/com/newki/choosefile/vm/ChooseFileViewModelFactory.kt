package com.newki.choosefile.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class ChooseFileViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ChooseFileViewModel::class.java)) {
            // 创建 MyViewModel 实例，并传入参数
            return ChooseFileViewModel() as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

}