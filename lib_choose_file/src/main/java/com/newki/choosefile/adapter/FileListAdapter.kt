package com.newki.choosefile.adapter

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.newki.choosefile.ChooseFileInfo
import com.newki.choosefile.ChooseFileUIConfig
import com.newki.choosefile.R

/**
 * 文件内容列表展示，区别不同的文件类型展示
 */
class FileListAdapter(private val fileList: MutableList<ChooseFileInfo>, private val uiConfig: ChooseFileUIConfig) :
    RecyclerView.Adapter<FileListAdapter.FileListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileListViewHolder {
        val itemView = View.inflate(parent.context, R.layout.item_file_list, null)
        return FileListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FileListViewHolder, position: Int) {
        val item = fileList[position]

        holder.curPosition = position
        holder.llRoot.setOnClickListener {
            mListener?.onClick(position)
        }

        holder.ivFileIcon.setImageResource(item.fileTypeIconRes)
        holder.tvFileName.text = item.fileName
        holder.tvFileName.setTextColor(uiConfig.fileNameTextColor)
        holder.tvFileName.setTextSize(TypedValue.COMPLEX_UNIT_SP, uiConfig.fileNameTextSize.toFloat())

        holder.tvFileSize.text = item.fileSize
        holder.tvFileSize.setTextColor(uiConfig.fileInfoTextColor)
        holder.tvFileSize.setTextSize(TypedValue.COMPLEX_UNIT_SP, uiConfig.fileInfoTextSize.toFloat())

        holder.tvFileDate.text = item.fileLastUpdateTime
        holder.tvFileDate.setTextColor(uiConfig.fileInfoTextColor)
        holder.tvFileDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, uiConfig.fileInfoTextSize.toFloat())
    }


    override fun getItemCount(): Int = fileList.size


    inner class FileListViewHolder(private val itemView: View) : ViewHolder(itemView) {

        val ivFileIcon: ImageView = itemView.findViewById(R.id.iv_file_icon)
        val tvFileName: TextView = itemView.findViewById(R.id.tv_file_name)
        val tvFileSize: TextView = itemView.findViewById(R.id.tv_file_size)
        val tvFileDate: TextView = itemView.findViewById(R.id.tv_file_date)
        val llRoot: ViewGroup = itemView.findViewById(R.id.ll_root)

        var curPosition: Int = 0

    }

    private var mListener: OnFileListClickListener? = null
    fun setOnFileListClickListener(listener: OnFileListClickListener) {
        mListener = listener
    }

    fun interface OnFileListClickListener {
        fun onClick(position: Int)
    }

}