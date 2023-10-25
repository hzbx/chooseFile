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


class FileNavAdapter(private val navPathList: MutableList<ChooseFileInfo>, private val uiConfig: ChooseFileUIConfig) :
    RecyclerView.Adapter<FileNavAdapter.FileNavViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileNavViewHolder {
        val itemView = View.inflate(parent.context, R.layout.item_choose_file_nav, null)
        return FileNavViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FileNavViewHolder, position: Int) {
        holder.curPosition = position
        holder.tvPath.text = navPathList[position].fileName
        holder.tvPath.setTextColor(uiConfig.fileNavBarTextColor)
        holder.tvPath.setTextSize(TypedValue.COMPLEX_UNIT_SP, uiConfig.fileNameTextSize.toFloat())
        holder.ivPathSegment.setImageResource(uiConfig.fileNavBarArrowIconRes)

        if (position == (itemCount - 1)) holder.ivPathSegment.visibility = View.INVISIBLE
        else holder.ivPathSegment.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int = navPathList.size


    inner class FileNavViewHolder(private val itemView: View) : ViewHolder(itemView) {

        val tvPath: TextView = itemView.findViewById(R.id.tv_root)
        val ivPathSegment: ImageView = itemView.findViewById(R.id.iv_path_segment)
        var curPosition: Int = 0

        init {
            itemView.setOnClickListener {
                mListener?.onClick(curPosition)
            }
        }
    }

    private var mListener: OnNavClickListener? = null
    fun setOnNavClickListener(listener: OnNavClickListener) {
        mListener = listener
    }

    fun interface OnNavClickListener {
        fun onClick(position: Int)
    }

}