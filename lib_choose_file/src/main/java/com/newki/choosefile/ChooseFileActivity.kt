package com.newki.choosefile


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.newki.choosefile.adapter.FileListAdapter
import com.newki.choosefile.adapter.FileNavAdapter
import com.newki.choosefile.detector.ChooseFileHighPolicy
import com.newki.choosefile.detector.ChooseFileLowPolicy
import com.newki.choosefile.statusBarHost.StatusBarHost
import com.newki.choosefile.utils.PermissionUtil
import com.newki.choosefile.vm.ChooseFileViewModel
import com.newki.choosefile.vm.ChooseFileViewModelFactory

@SuppressLint("NotifyDataSetChanged")
class ChooseFileActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var titleBar: ViewGroup
    private lateinit var ivBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvRoot: TextView
    private lateinit var ivPathSegment: ImageView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var rvNav: RecyclerView
    private lateinit var rvFiles: RecyclerView

    private val mViewModel: ChooseFileViewModel by lazy {
        ViewModelProvider(this, ChooseFileViewModelFactory()).get(ChooseFileViewModel::class.java)
    }

    private val mChooseFilePolicy by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ChooseFileHighPolicy()
        } else {
            ChooseFileLowPolicy()
        }
    }

    private var mainHandler = Handler(Looper.getMainLooper())


    //展示当前页面的UI风格
    private val uiConfig = ChooseFile.config?.mUIConfig ?: ChooseFileUIConfig.Builder().build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_choose_file)

        StatusBarHost.inject(this)
            .setStatusBarBackground(uiConfig.statusBarColor)
            .setStatusBarWhiteText()
            .setNavigationBarBackground(uiConfig.navigationBarColor)
            .setNavigatiopnBarIconBlack()

        initView()
        initTheme()
        initRV()
        initListener()
        requestPermission()
    }

    private fun requestPermission() {
        if (PermissionUtil.isStoragePermissionGranted(this)) {
            obtainByPath(mViewModel.rootPath)
        }
    }

    //设置跟目录并刷新
    private fun obtainByPath(path: String) {

        mChooseFilePolicy.getFileList(path, callback = { fileList, topInfo ->

            mainHandler.post {
                popupFileList(fileList, topInfo)
            }
        })

    }

    //展示文件列表数据
    private fun popupFileList(fileList: List<ChooseFileInfo>, topInfo: ChooseFileInfo?) {
        swipeRefresh.isRefreshing = false

        //设置顶部的文件导航路径
        setTopNavSelect(topInfo)

        if (fileList.isNotEmpty()) {
            //展示文件列表
            mViewModel.mFileList.clear()
            mViewModel.mFileList.addAll(fileList)
            mViewModel.mFileListAdapter?.notifyDataSetChanged()

            if (topInfo == null && mViewModel.rootChoosePos > 0) {
                mViewModel.mCurPath = mViewModel.rootPath
                (rvFiles.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(mViewModel.rootChoosePos, 0)

            }

        } else {
            //展示空视图
            Toast.makeText(this, "展示空视图", Toast.LENGTH_SHORT).show()
        }

    }

    //顶部文件导航的设置
    private fun setTopNavSelect(topInfo: ChooseFileInfo?) {

        if (topInfo != null) {
            if (mViewModel.mNavPathList.isEmpty()) {
                mViewModel.mNavPathList.add(topInfo)
            } else {
                val index = mViewModel.mNavPathList.indexOf(topInfo)

                if (index >= 0) {
                    mViewModel.mNavPathList.subList(index + 1, mViewModel.mNavPathList.size).clear()
                } else {
                    mViewModel.mNavPathList.add(topInfo)
                }
            }

        } else {
            mViewModel.mNavPathList.clear()
        }

        mViewModel.mNavAdapter?.notifyDataSetChanged()
    }


    private fun startRefreshAnim() {
        swipeRefresh.isRefreshing = true
    }

    private fun initTheme() {

        titleBar.setBackgroundColor(uiConfig.titleBarBgColor)
        ivBack.setImageResource(uiConfig.titleBarBackRes)
        tvTitle.setTextColor(uiConfig.titleBarTitleColor)
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, uiConfig.titleBarTitleSize.toFloat())

        tvRoot.setTextColor(uiConfig.fileNavBarTextColor)
        tvRoot.setTextSize(TypedValue.COMPLEX_UNIT_SP, uiConfig.fileNameTextSize.toFloat())
        ivPathSegment.setImageResource(uiConfig.fileNavBarArrowIconRes)

    }

    private fun initRV() {

        mViewModel.mNavAdapter = FileNavAdapter(mViewModel.mNavPathList, uiConfig)
        rvNav.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvNav.adapter = mViewModel.mNavAdapter
        mViewModel.mNavAdapter?.setOnNavClickListener { position ->
            val item = mViewModel.mNavPathList[position]
            mViewModel.mCurPath = item.filePath
            startRefreshAnim()
            obtainByPath(mViewModel.mCurPath)
        }

        mViewModel.mFileListAdapter = FileListAdapter(mViewModel.mFileList, uiConfig)
        rvFiles.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvFiles.adapter = mViewModel.mFileListAdapter
        mViewModel.mFileListAdapter?.setOnFileListClickListener() { position ->
            val item = mViewModel.mFileList[position]
            if (item.isDir) {
                //设置当前Root的选中
                if (mViewModel.mNavPathList.isEmpty()) {
                    mViewModel.rootChoosePos = (rvFiles.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                }

                //文件夹-直接刷新页面
                mViewModel.mCurPath = item.filePath
                startRefreshAnim()
                obtainByPath(mViewModel.mCurPath)
            } else {
                //选中文件-回调出去
                setResult(-1, Intent().putExtra("chooseFile", item))
                finish()
            }

        }

    }

    private fun initListener() {
        ivBack.setOnClickListener(this)
        tvRoot.setOnClickListener(this)

        //刷新
        swipeRefresh.setOnRefreshListener {
            startRefreshAnim()
            obtainByPath(mViewModel.mCurPath)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.iv_back -> finish()
            R.id.tv_root -> {
                mViewModel.mCurPath = mViewModel.rootPath
                startRefreshAnim()
                obtainByPath(mViewModel.rootPath)
            }
        }
    }

    private fun initView() {
        titleBar = findViewById(R.id.title_bar)
        ivBack = findViewById(R.id.iv_back)
        tvTitle = findViewById(R.id.tv_title)
        tvRoot = findViewById(R.id.tv_root)
        ivPathSegment = findViewById(R.id.iv_path_segment)
        swipeRefresh = findViewById(R.id.swipe_refresh)

        rvNav = findViewById(R.id.rv_nav)
        rvFiles = findViewById(R.id.rv_files)
    }

    //动态权限授权的回调
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtainByPath(mViewModel.rootPath)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                // 用户已经授权，执行需要访问外部存储的操作
                obtainByPath(mViewModel.rootPath)
            } else {
                // 用户未授权，无法访问外部存储
                Toast.makeText(this, "未授权，无法访问外部存储", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ChooseFile.release()
    }

}