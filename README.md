# Android文件选择器的实现

## 前言

之前发布的项目都是不基于 Activity 实现的，只是一些功能性的小功能，而类似文件选择，图片选择，除了功能的实现还需要处理 UI 相关的配置。

在前面的[【如何操作文件的选择】](https://juejin.cn/post/7211422882253537339) 一文中我就想把逻辑做一下封装，做成开箱即用的文件选择器，本来这功能是项目中自用的，UI 等都是自有的，如果要做开源出去那必要要大改，设置可配置选项。

那么如何自定义一个文件下载器呢？

1. 我们需要配置 Activity 基本的 Theme，动画，状态栏，导航栏等处理。
2. 我们需要配置展示的文本大小，返回图标，列表与导航栏的文本大小等等。
3. 然后我们对XML的布局并构建导航列表与文件列表的数据适配器等。
4. 然后我们就可以处理权限以及对文件的操作了。
5. 可以使用策略模式不同的版本不同的实现方式。
6. 过滤操作是比不可少的，我们获取文件之后使用过滤操作展示我们想要的文件。

差不多到处就能完成一个基本的操作文件选择框架了，具体实现的效果如下：

Android 7.0 效果:

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/150f70466a6d4f97b7bf056717f484b0~tplv-k3u1fbpfcp-watermark.image?)

Android 9.0 效果:

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/aae254d8b4ef49a2ac69c2119fe6c4b8~tplv-k3u1fbpfcp-watermark.image?)

Android 12 效果：

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c9ea301d1fb347a5a15f7344ae5fadc8~tplv-k3u1fbpfcp-watermark.image?)

项目实现基于 target31，不配置兼容模式 requestLegacyExternalStorage ，可用于 4.4 版本以上Android系统，可保持UI的一致性。

说了这么多还是赶紧开始吧！

![300.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0e526cc29ee84b22911915126f8561bf~tplv-k3u1fbpfcp-watermark.image?)

### 一、文件选择的页面的配置

我们使用我们自定义的theme与动画即可。由于我们要自己实现可控的标题栏，所以我们的样式不需要toolbar：

```xml
<resources>
    <style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
        <item name="colorPrimary">@color/choose_file_app_blue</item>
        <item name="colorPrimaryDark">@color/choose_file_app_blue</item>
        <item name="colorAccent">@color/choose_file_app_blue</item>
        <item name="android:windowAnimationStyle">@style/My_AnimationActivity</item>
        <item name="android:windowIsTranslucent">false</item>
    </style>

    <style name="My_AnimationActivity" mce_bogus="1" parent="@android:style/Animation.Activity">
        <item name="android:activityOpenEnterAnimation">@anim/open_enter</item>
        <item name="android:activityCloseExitAnimation">@anim/close_exit</item>
    </style>
</resources>
```

为了可配置的状态栏与导航栏，这里我用到之前的项目中的 StatusBarHost 框架，具体的实现与细节可以查看之前的文章，[【传送门】](https://juejin.cn/post/7138312796474703909)。

那么我们创建选择文件的Activity大致如下：

```kotlin
class ChooseFileActivity : AppCompatActivity(), View.OnClickListener {

    private val mViewModel: ChooseFileViewModel by lazy {
        ViewModelProvider(this, ChooseFileViewModelFactory()).get(ChooseFileViewModel::class.java)
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

    }
```

而为了横竖屏切换的效果，或者说为了适配折叠屏设备，我们可以使用ViewModel保存一些页面状态：

```kotlin
class ChooseFileViewModel : ViewModel() {

    val mNavPathList = arrayListOf<ChooseFileInfo>()
    var mNavAdapter: FileNavAdapter? = null

    val mFileList = arrayListOf<ChooseFileInfo>()
    var mFileListAdapter: FileListAdapter? = null

    //根目录
    val rootPath = Environment.getExternalStorageDirectory().absolutePath

    var rootChoosePos = 0  //根目录文档选中的索引

    //当前选择的路径
    var mCurPath = Environment.getExternalStorageDirectory().absolutePath
}
```

这里已经用到了一些UI的配置选项，我们赶紧接下来往下走。

### 二、页面的UI配置与其他配置

一般我们都会根据不同的UI效果，设置不同的文本颜色和背景，所以我们需要把页面上的文本与背景和图标等选项抽取出来，配置成可选的属性：

```java
public class ChooseFileUIConfig {

    private int statusBarColor;   //状态栏颜色

    private int titleBarBgColor;  //标题栏的背景颜色
    private int titleBarBackRes;  //标题栏的返回按钮资源
    private int titleBarTitleColor; //标题栏的标题文字颜色
    private int titleBarTitleSize; //标题栏的标题文字大小(sp)

    private int navigationBarColor; //底部导航栏颜色

    private int fileNavBarTextColor; //文件导航栏的文本颜色
    private int fileNavBarTextSize; //文件导航栏的文本大小
    private int fileNavBarArrowIconRes; //文件导航栏的箭头图标资源

    private int fileNameTextColor;  //文件(夹)名称字体颜色
    private int fileNameTextSize;  //文件(夹)名称字体大小(sp)
    private int fileInfoTextColor;  //文件(夹)提示信息字体大小
    private int fileInfoTextSize;  //文件(夹)提示信息字体大小(sp)

    private ChooseFileUIConfig() {
    }

    ...
}
```

然后我们使用构建者模式创建可选的配置，如果不选择那么就可以使用默认的配置，就特别适合此场景：

```java
  public static class Builder {

        private int statusBarColor = Color.parseColor("#0689FB");   //状态栏颜色

        private int titleBarBgColor = Color.parseColor("#0689FB");  //标题栏的背景颜色
        private int titleBarBackRes = R.drawable.cf_back;  //标题栏的返回按钮资源
        private int titleBarTitleColor = Color.parseColor("#FFFFFF"); //标题栏的标题文字颜色
        private int titleBarTitleSize = 20; //标题栏的标题文字大小(sp)

        private int navigationBarColor = Color.parseColor("#F7F7FB"); //底部导航栏颜色

        private int fileNavBarTextColor = Color.parseColor("#333333"); //文件导航栏的文本颜色
        private int fileNavBarTextSize = 15; //文件导航栏的文本大小
        private int fileNavBarArrowIconRes = R.drawable.cf_next; //文件导航栏的箭头图标资源

        private int fileNameTextColor = Color.BLACK;  //文件(夹)名称字体颜色
        private int fileNameTextSize = 16;  //文件(夹)名称字体大小(sp)
        private int fileInfoTextColor = Color.parseColor("#A9A9A9");  //文件(夹)提示信息字体大小
        private int fileInfoTextSize = 14;  //文件(夹)提示信息字体大小(sp)

        public Builder() {
        }

        public Builder statusBarColor(int statusBarColor) {
            this.statusBarColor = statusBarColor;
            return this;
        }
     ...   
```

UI的配置完成之后，我们还需要对一些常规的配置做一些可选操作，例如线程池的自定义，过滤文件的选择等等。

```java
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
```

由于操作文件是耗时的操作，我们最好是在线程中进行，我们统一使用默认的线程池处理，如果用户想自定义使用可以他自己的线程池。

而 forResult 的实现我们是对 startActivityForResult 的封装，为了兼容低版本内部是 Ghost 实现。

而内部使用到的 ChooseFile 则是我们的单例使用入口，内部实现如下：

```kotlin
object ChooseFile {

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
```

到处我们就可以正常的使用框架了：

```kotlin

        findViewById<Button>(R.id.btn_get_file).setOnClickListener {

            ChooseFile.create(this)
                .setUIConfig(ChooseFileUIConfig.Builder().build())
                .setTypeFilter { listData ->
                    return@setTypeFilter ArrayList(listData.filter { item ->
                        //只要文件夹
                          item.isDir

                        //只要文档文件
//                        item.fileType == ChooseFile.FILE_TYPE_FOLDER ||
//                                item.fileType == ChooseFile.FILE_TYPE_TEXT ||
//                                item.fileType == ChooseFile.FILE_TYPE_PDF
                    })
                }
                .forResult {
                    Toast.makeText(this, "选中的文件：" + it?.fileName, Toast.LENGTH_SHORT).show()
                    val uri = Uri.parse(it?.filePathUri)
                    val fis = contentResolver.openInputStream(uri)

                    Log.w("TAG", "文件的Uri:" + it?.filePathUri + " uri:" + uri + " fis:" + fis)

                    fis?.close()
                }
        }

```

这样拉到列表的底部之后就只会显示文件夹类型：

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8afe612381a84c46b1a34611627da873~tplv-k3u1fbpfcp-watermark.image?)


### 三、导航列表与文件列表的展示

对应文件列表的展示以及文件导航的展示，我们需要先定义对应的xml：

代码大家都会，效果如下图：

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9d4833a21cb84e3c92c69bfb87669b32~tplv-k3u1fbpfcp-watermark.image?)

那么RV的处理如下：

```kotlin
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
```

Adapter的处理也很简单，我们把UI的配置选择传进来，然后做赋值操作即可，我们最好是只做赋值操作，处理的逻辑都在文件的处理那一边处理，那边是有子线程一并处理的。

```kotlin
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
```

两个 Adapter 的实现效果是类似的，就不多贴代码，有兴趣可以去文章末尾找源码。

关于展示的Item的Bean对象，我们需要使用自定义的 File 封装，作为展示的选项。我们需要对文件进行读取之后直接封装到这个 Bean 对象中，方便直接展示。

```java
public class ChooseFileInfo implements Serializable {

    public String fileName;
    public boolean isDir;  //是否是文件夹
    public String fileSize; //如果是文件夹则表示子目录项数,如果不是文件夹则表示文件大小,当值为-1的时候不显示
    public String fileLastUpdateTime;   //最后操作事件
    public String filePath;             //文件的路径
    public String filePathUri;          //文件的路径，URI形式
    public String fileType;           //文件类型
    public int fileTypeIconRes;         //文件类型对应的图标展示

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChooseFileInfo that = (ChooseFileInfo) o;
        return Objects.equals(filePath, that.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath);
    }

}
```

需要注意的是我们需要处理文件夹的选中与顶部文件导航的交互，两个RV选中之后需要有数据的逻辑处理。

底部的 RV 选中文件夹之后需要给顶部的文件导航添加数据，而顶部的文件导航选中之后需要刷新底部的 RV 选中：

底部 RV 的选中：
```kotlin
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
```

顶部 RV 的选中：
```kotlin
 mViewModel.mNavAdapter?.setOnNavClickListener { position ->
            val item = mViewModel.mNavPathList[position]
            mViewModel.mCurPath = item.filePath
            startRefreshAnim()
            obtainByPath(mViewModel.mCurPath)
        }
```

加载数据完成之后的顶部导航展示逻辑：

```kotlin
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
```

### 四、权限处理与文件的操作

到此，UI的部分就大致完成了，我们需要对数据与权限的逻辑做处理，我们为了演示之前文章中 FilrProvider 与 DocumentsProvider 的使用，这里用做高版本的作为展示。


首先我们需要处理动态权限问题，分为不同的版本的权限申请实现：

```java
public class PermissionUtil {

    //统一处理权限
    public static boolean isStoragePermissionGranted(Activity activity) {
        Context context = activity.getApplicationContext();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivityForResult(intent, 1);

                return false;
            } else {

                // 有外部存储的权限
                return true;
            }

        } else {

            int readPermissionCheck = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            int writePermissionCheck = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (readPermissionCheck == PackageManager.PERMISSION_GRANTED
                    && writePermissionCheck == PackageManager.PERMISSION_GRANTED) {
                Log.v("permission", "Permission is granted");
                return true;
            } else {
                Log.v("permission", "Permission is revoked");
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }

        }

    }

}
```

那么在 Activity 的权限回调中我们需要处理成功的回调：

```kotlin
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
```

对于文件的类型处理，我们使用工具类封装一下，大致的逻辑是根据文件的后缀名匹配。并且对文件的 mimeType 做了匹配，大致代码如下：

```java

    // Audio
    public static final int FILE_TYPE_MP3 = 1;
    public static final int FILE_TYPE_M4A = 2;
    public static final int FILE_TYPE_WAV = 3;
    public static final int FILE_TYPE_AMR = 4;
    public static final int FILE_TYPE_AWB = 5;
    public static final int FILE_TYPE_WMA = 6;
    public static final int FILE_TYPE_OGG = 7;
    private static final int FIRST_AUDIO_FILE_TYPE = 0;
    private static final int LAST_AUDIO_FILE_TYPE = 10;

    // MIDI
    public static final int FILE_TYPE_MID = 11;
    public static final int FILE_TYPE_SMF = 12;
    public static final int FILE_TYPE_IMY = 13;
    private static final int FIRST_MIDI_FILE_TYPE = 10;
    private static final int LAST_MIDI_FILE_TYPE = 20;

    // Video
    public static final int FILE_TYPE_MP4 = 21;
    public static final int FILE_TYPE_M4V = 22;
    public static final int FILE_TYPE_3GPP = 23;
    public static final int FILE_TYPE_3GPP2 = 24;
    public static final int FILE_TYPE_WMV = 25;
    private static final int FIRST_VIDEO_FILE_TYPE = 20;
    private static final int LAST_VIDEO_FILE_TYPE = 30;

    // Image
    public static final int FILE_TYPE_JPEG = 31;
    public static final int FILE_TYPE_GIF = 32;
    public static final int FILE_TYPE_PNG = 33;
    public static final int FILE_TYPE_BMP = 34;
    public static final int FILE_TYPE_WBMP = 35;
    private static final int FIRST_IMAGE_FILE_TYPE = 30;
    private static final int LAST_IMAGE_FILE_TYPE = 40;

    // Playlist
    public static final int FILE_TYPE_M3U = 41;
    public static final int FILE_TYPE_PLS = 42;
    public static final int FILE_TYPE_WPL = 43;
    private static final int FIRST_PLAYLIST_FILE_TYPE = 40;
    private static final int LAST_PLAYLIST_FILE_TYPE = 50;

    //TEXT
    public static final int FILE_TYPE_TXT = 51;
    public static final int FILE_TYPE_DOC = 52;
    public static final int FILE_TYPE_RTF = 53;
    public static final int FILE_TYPE_LOG = 54;
    public static final int FILE_TYPE_CONF = 55;
    public static final int FILE_TYPE_SH = 56;
    public static final int FILE_TYPE_XML = 57;
    public static final int FILE_TYPE_DOCX = 58;
    private static final int FIRST_TEXT_FILE_TYPE = 50;
    private static final int LAST_TEXT_FILE_TYPE = 60;

    //XLS
    public static final int FILE_TYPE_XLS = 61;
    public static final int FILE_TYPE_XLSX = 62;
    private static final int FIRST_XLS_FILE_TYPE = 60;
    private static final int LAST_XLS_FILE_TYPE = 70;

    //PPT
    public static final int FILE_TYPE_PPT = 71;
    public static final int FILE_TYPE_PPTX = 72;
    private static final int FIRST_PPT_FILE_TYPE = 70;
    private static final int LAST_PPT_FILE_TYPE = 80;

    //PDF
    public static final int FILE_TYPE_PDF = 81;
    private static final int FIRST_PDF_FILE_TYPE = 80;
    private static final int LAST_PDF_FILE_TYPE = 90;

    //静态内部类
    static class MediaFileType {

        int fileType;
        String mimeType;

        MediaFileType(int fileType, String mimeType) {
            this.fileType = fileType;
            this.mimeType = mimeType;
        }
    }

    private static HashMap<String, MediaFileType> sFileTypeMap
            = new HashMap<>();
    private static HashMap<String, Integer> sMimeTypeMap
            = new HashMap<>();

    static void addFileType(String extension, int fileType, String mimeType) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
        sMimeTypeMap.put(mimeType, fileType);
    }

    static {
        //根据文件后缀名匹配
        addFileType("MP3", FILE_TYPE_MP3, "audio/mpeg");
        addFileType("M4A", FILE_TYPE_M4A, "audio/mp4");
        addFileType("WAV", FILE_TYPE_WAV, "audio/x-wav");
        addFileType("AMR", FILE_TYPE_AMR, "audio/amr");
        addFileType("AWB", FILE_TYPE_AWB, "audio/amr-wb");
        addFileType("WMA", FILE_TYPE_WMA, "audio/x-ms-wma");
        addFileType("OGG", FILE_TYPE_OGG, "application/ogg");

        addFileType("MID", FILE_TYPE_MID, "audio/midi");
        addFileType("XMF", FILE_TYPE_MID, "audio/midi");
        addFileType("RTTTL", FILE_TYPE_MID, "audio/midi");
        addFileType("SMF", FILE_TYPE_SMF, "audio/sp-midi");
        addFileType("IMY", FILE_TYPE_IMY, "audio/imelody");

        addFileType("MP4", FILE_TYPE_MP4, "video/mp4");
        addFileType("M4V", FILE_TYPE_M4V, "video/mp4");
        addFileType("3GP", FILE_TYPE_3GPP, "video/3gpp");
        addFileType("3GPP", FILE_TYPE_3GPP, "video/3gpp");
        addFileType("3G2", FILE_TYPE_3GPP2, "video/3gpp2");
        addFileType("3GPP2", FILE_TYPE_3GPP2, "video/3gpp2");
        addFileType("WMV", FILE_TYPE_WMV, "video/x-ms-wmv");

        addFileType("JPG", FILE_TYPE_JPEG, "image/jpeg");
        addFileType("JPEG", FILE_TYPE_JPEG, "image/jpeg");
        addFileType("GIF", FILE_TYPE_GIF, "image/gif");
        addFileType("PNG", FILE_TYPE_PNG, "image/png");
        addFileType("BMP", FILE_TYPE_BMP, "image/x-ms-bmp");
        addFileType("WBMP", FILE_TYPE_WBMP, "image/vnd.wap.wbmp");

        addFileType("M3U", FILE_TYPE_M3U, "audio/x-mpegurl");
        addFileType("PLS", FILE_TYPE_PLS, "audio/x-scpls");
        addFileType("WPL", FILE_TYPE_WPL, "application/vnd.ms-wpl");

        addFileType("TXT", FILE_TYPE_TXT, "text/plain");
        addFileType("RTF", FILE_TYPE_RTF, "application/rtf");
        addFileType("LOG", FILE_TYPE_LOG, "text/plain");
        addFileType("CONF", FILE_TYPE_CONF, "text/plain");
        addFileType("SH", FILE_TYPE_SH, "text/plain");
        addFileType("XML", FILE_TYPE_XML, "text/plain");
        addFileType("DOC", FILE_TYPE_DOC, "application/msword");
        addFileType("DOCX", FILE_TYPE_DOCX, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        addFileType("XLS", FILE_TYPE_XLS, "application/vnd.ms-excel application/x-excel");
        addFileType("XLSX", FILE_TYPE_XLSX, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        addFileType("PPT", FILE_TYPE_PPT, "application/vnd.ms-powerpoint");
        addFileType("PPTX", FILE_TYPE_PPTX, "application/vnd.openxmlformats-officedocument.presentationml.presentation");

        addFileType("PDF", FILE_TYPE_PDF, "application/pdf");

        StringBuilder builder = new StringBuilder();

        for (String s : sFileTypeMap.keySet()) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(s);
        }
        sFileExtensions = builder.toString();
    }

    public static final String UNKNOWN_STRING = "<unknown>";

    public static boolean isAudioFileType(int fileType) {
        return ((fileType >= FIRST_AUDIO_FILE_TYPE &&
                fileType <= LAST_AUDIO_FILE_TYPE) ||
                (fileType >= FIRST_MIDI_FILE_TYPE &&
                        fileType <= LAST_MIDI_FILE_TYPE));
    }

    public static boolean isVideoFileType(int fileType) {
        return (fileType >= FIRST_VIDEO_FILE_TYPE &&
                fileType <= LAST_VIDEO_FILE_TYPE);
    }

    public static boolean isImageFileType(int fileType) {
        return (fileType >= FIRST_IMAGE_FILE_TYPE &&
                fileType <= LAST_IMAGE_FILE_TYPE);
    }

    public static boolean isPlayListFileType(int fileType) {
        return (fileType >= FIRST_PLAYLIST_FILE_TYPE &&
                fileType <= LAST_PLAYLIST_FILE_TYPE);
    }

    public static boolean isTextFileType(int fileType) {
        return (fileType >= FIRST_TEXT_FILE_TYPE &&
                fileType <= LAST_TEXT_FILE_TYPE);
    }

    public static boolean isXLSFileType(int fileType) {
        return (fileType >= FIRST_XLS_FILE_TYPE &&
                fileType <= LAST_XLS_FILE_TYPE);
    }

    public static boolean isPPTFileType(int fileType) {
        return (fileType >= FIRST_PPT_FILE_TYPE &&
                fileType <= LAST_PPT_FILE_TYPE);
    }

    public static boolean isPDFFileType(int fileType) {
        return (fileType >= FIRST_PDF_FILE_TYPE &&
                fileType <= LAST_PDF_FILE_TYPE);
    }

    public static MediaFileType getFileType(String path) {
        int lastDot = path.lastIndexOf(".");
        if (lastDot < 0)
            return null;
        return sFileTypeMap.get(path.substring(lastDot + 1).toUpperCase());
    }

    //根据视频文件路径判断文件类型
    public static boolean isVideoFileType(String path) {
        MediaFileType type = getFileType(path);
        if (null != type) {
            return isVideoFileType(type.fileType);
        }
        return false;
    }

    //根据音频文件路径判断文件类型
    public static boolean isAudioFileType(String path) {
        MediaFileType type = getFileType(path);
        if (null != type) {
            return isAudioFileType(type.fileType);
        }
        return false;
    }

    //根据图片文件路径判断文件类型
    public static boolean isImageFileType(String path) {
        MediaFileType type = getFileType(path);
        if (null != type) {
            return isImageFileType(type.fileType);
        }
        return false;
    }

    //根据文本文件路径判断文件类型
    public static boolean isTextFileType(String path) {
        MediaFileType type = getFileType(path);
        if (null != type) {
            return isTextFileType(type.fileType);
        }
        return false;
    }

    public static boolean isXLSFileType(String path) {
        MediaFileType type = getFileType(path);
        if (null != type) {
            return isXLSFileType(type.fileType);
        }
        return false;
    }

    public static boolean isPPTFileType(String path) {
        MediaFileType type = getFileType(path);
        if (null != type) {
            return isPPTFileType(type.fileType);
        }
        return false;
    }

    public static boolean isPDFFileType(String path) {
        MediaFileType type = getFileType(path);
        if (null != type) {
            return isPDFFileType(type.fileType);
        }
        return false;
    }

```

接下来我们就能在获取文件的时候，处理好队友的格式，赋值对应展示的Icon，就可以在数据适配器上面展示了。

### 五、不同版本的文件获取

其实获取到对应版本权限之后，都使用 File 就可以获取到对应版本的文件信息了，这里便于演示，所以把 Android10 以上与 Android10 以下区别开来，高版本的使用 DocumentProvider的方式实现：

使用接口+策略的方式，我们定义不同的实现方案：

```kotlin
internal interface IChooseFilePolicy {

    fun getFileList(rootPath: String, callback: (fileList: List<ChooseFileInfo>, topInfo: ChooseFileInfo?) -> Unit)

}
```

低版本的直接获取 FileList ,注意我们处理文件，赋值操作等都是耗时操作，所以我们最好是在线程池中处理，大致的逻辑如下：

```kotlin
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

            //满数据回调
            callback(filterData, topInfo)
        }

    }

}
```

Android 10以上的高版本我们启动 DocumentProvider 的查询方式：

```kotlin
internal class ChooseFileHighPolicy : IChooseFilePolicy {

    @SuppressLint("Range")
    override fun getFileList(rootPath: String, callback: (fileList: List<ChooseFileInfo>, topInfo: ChooseFileInfo?) -> Unit) {

        val uri = DocumentsContract.buildChildDocumentsUri(
            "com.newki.choosefile.authorities",
            rootPath
        )

        ChooseFile.config?.mExecutor?.execute {

            val cursor = ChooseFile.activityRef?.get()?.contentResolver?.query(uri, null, null, null, null)

            val listData: ArrayList<ChooseFileInfo> = ArrayList()
            var topInfo: ChooseFileInfo? = null

            if (cursor != null) {

                while (cursor.moveToNext()) {

                    val isTop = cursor.getInt(cursor.getColumnIndex("isTop"))
                    val isRoot = cursor.getInt(cursor.getColumnIndex("isRoot"))

                    val fileName = cursor.getString(cursor.getColumnIndex("fileName"))
                    val isDir = cursor.getInt(cursor.getColumnIndex("isDir"))
                    val fileSize = cursor.getString(cursor.getColumnIndex("fileSize"))
                    val fileLastUpdateTime = cursor.getString(cursor.getColumnIndex("fileLastUpdateTime"))
                    val filePath = cursor.getString(cursor.getColumnIndex("filePath"))
                    val filePathUri = cursor.getString(cursor.getColumnIndex("filePathUri"))
                    val fileTypeIconRes = cursor.getInt(cursor.getColumnIndex("fileTypeIconRes"))

                    if (isTop == 1) {

                        if (isRoot == 0) {
                            topInfo = ChooseFileInfo().apply {
                                this.fileName = fileName
                                this.isDir = isDir != 0
                                this.fileSize = fileSize
                                this.fileLastUpdateTime = fileLastUpdateTime
                                this.filePath = filePath
                                this.filePathUri = filePathUri
                                this.fileTypeIconRes = fileTypeIconRes
                            }
                        }

                    } else {

                        listData.add(ChooseFileInfo().apply {
                            this.fileName = fileName
                            this.isDir = isDir != 0
                            this.fileSize = fileSize
                            this.fileLastUpdateTime = fileLastUpdateTime
                            this.filePath = filePath
                            this.filePathUri = filePathUri
                            this.fileTypeIconRes = fileTypeIconRes
                        })
                    }

                }

                cursor.close()

                //满数据回调
                callback(filterData, topInfo)

            } else {

                callback(emptyList(), null)

            }

        }

    }
}
```

而 DocumentProvider 的具体实现如下，我们只需要重点关注 queryChildDocuments 方法的实现即可：

```java
public class ChooseFileDocumentProvider extends DocumentsProvider {

    private final static String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{"isTop", "isRoot", "fileName", "isDir", "fileSize", "fileLastUpdateTime",
            "filePath", "filePathUri", "fileType", "fileTypeIconRes"};

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        return null;
    }

    @Override
    public boolean isChildDocument(String parentDocumentId, String documentId) {
        return documentId.startsWith(parentDocumentId);
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {

        // 创建一个查询cursor, 来设置需要查询的项, 如果"projection"为空, 那么使用默认项
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        includeFile(result, new File(documentId), false, false);
        return result;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {

        // 创建一个查询cursor, 来设置需要查询的项, 如果"projection"为空, 那么使用默认项。
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);

        final File parent = new File(parentDocumentId);
        boolean isDirectory = parent.isDirectory();
        boolean canRead = parent.canRead();
        File[] files = parent.listFiles();
        boolean isRoot = parent.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath());
        includeFile(result, parent, isRoot, true);

        //遍历添加处理文件列表
        if (isDirectory && canRead && files != null && files.length > 0) {
            for (File file : files) {
                // 添加文件的名字, 类型, 大小等属性
                includeFile(result, file, isRoot, false);
            }
        }

        return result;
    }

    private void includeFile(final MatrixCursor result, final File file, boolean isRoot, boolean isTop) {
        final MatrixCursor.RowBuilder row = result.newRow();

        row.add("isTop", isTop ? "1" : "0");
        row.add("isRoot", isRoot ? "1" : "0");

        if (file.isDirectory()) {
            row.add("fileName", file.getName());
            row.add("isDir", 1);
            row.add("fileSize", "共" + FileUtil.getSubfolderNum(file.getAbsolutePath()) + "项");
            row.add("fileLastUpdateTime", TimeUtil.getDateInString(new Date(file.lastModified())));
            row.add("filePath", file.getAbsolutePath());
            row.add("filePathUri", file.getAbsolutePath());
            row.add("fileType", ChooseFile.FILE_TYPE_FOLDER);
            row.add("fileTypeIconRes", R.drawable.file_folder);

        } else {

            row.add("fileName", file.getName());
            row.add("isDir", 0);
            row.add("fileSize", FileUtil.getFileSize(file.length()));
            row.add("fileLastUpdateTime", TimeUtil.getDateInString(new Date(file.lastModified())));
            row.add("filePath", file.getAbsolutePath());
            row.add("filePathUri", getFileUri(ChooseFile.activityRef.get(), file).toString());

            setFileType(row, file.getAbsolutePath());
        }

    }


    private void setFileType(MatrixCursor.RowBuilder row, String absolutePath) {
        if (FileUtil.isAudioFileType(absolutePath)) {
            row.add("fileType", ChooseFile.FILE_TYPE_AUDIO);
            row.add("fileTypeIconRes", R.drawable.file_audio);

        } else if (FileUtil.isImageFileType(absolutePath)) {
            row.add("fileType", ChooseFile.FILE_TYPE_IMAGE);
            row.add("fileTypeIconRes", R.drawable.file_image);

        } else if (FileUtil.isVideoFileType(absolutePath)) {
            row.add("fileType", ChooseFile.FILE_TYPE_VIDEO);
            row.add("fileTypeIconRes", R.drawable.file_video);

        } else if (FileUtil.isTextFileType(absolutePath)) {
            row.add("fileType", ChooseFile.FILE_TYPE_TEXT);
            row.add("fileTypeIconRes", R.drawable.file_text);

        } else if (FileUtil.isXLSFileType(absolutePath)) {
            row.add("fileType", ChooseFile.FILE_TYPE_XLS);
            row.add("fileTypeIconRes", R.drawable.file_excel);

        } else if (FileUtil.isPPTFileType(absolutePath)) {
            row.add("fileType", ChooseFile.FILE_TYPE_PPT);
            row.add("fileTypeIconRes", R.drawable.file_ppt);

        } else if (FileUtil.isPDFFileType(absolutePath)) {
            row.add("fileType", ChooseFile.FILE_TYPE_PDF);
            row.add("fileTypeIconRes", R.drawable.file_pdf);

        } else {
            row.add("fileType", ChooseFile.FILE_TYPE_Unknown);
            row.add("fileTypeIconRes", R.drawable.file_unknown);
        }
    }

    @Override
    public String getDocumentType(String documentId) throws FileNotFoundException {
        return null;
    }


    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal) throws FileNotFoundException {
        return null;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

}
```

记得要在清单文件中注册哦:

```xml
        <provider
            android:name=".provider.ChooseFileDocumentProvider"
            android:authorities="com.newki.choosefile.authorities"
            android:exported="true"
            android:grantUriPermissions="true"
            android:permission="android.permission.MANAGE_DOCUMENTS">
            <intent-filter>
                <action android:name="android.content.action.DOCUMENTS_PROVIDER" />
            </intent-filter>
        </provider>
```

为了地址的可达性，对应 7.0以上的版本我们最好是提供到 Uri 的资源，所以我们定义到自己的 FileProvider ，而我们只用到了外置 SD 卡的资源，所以我们直接这么配置即可：

```xml
        <provider
            android:name=".provider.ChooseFileProvider"
            android:authorities="com.newki.choosefile.file.path.share"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/choose_file_paths" />
        </provider>
```

关于 FileProvider 的细节使用可以看我的这一篇文章[【别滥用FileProvider了，Android中FileProvider的各种场景应用】](https://juejin.cn/post/7140166121595863076)

使用起来的话，就都是这么固定的写法：

```java
    private Uri getFileUri(Context context, File file) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ChooseFileProvider.getUriForFile(context, "com.newki.choosefile.file.path.share", file);
        } else {
            return Uri.fromFile(file);
        }
    }
```

### 六、过滤的操作

对于我们的应用来说，我们只需要选中SD卡中的文档文件，Txt,word,pdf等文件，那么我们就一定是需要过滤的操作的。

由于在上文我们获取File,封装自定义的 Bean 对象 ChooseFileInfo 中我们已经把文件的自定义格式定义好了，所以我们再回调之前先进行过滤操作，然后在再排序之后返回最终的数据源即可。

而为了对过滤的信息进行更灵活的过滤，我们可以直接暴露 ChooseFileInfo 对象，这样我们甚至能根据文件类型，文件名称，文件最后操作时间等等的方式进行过滤了。

先定义一个过滤的抽象接口如下：

```java
public interface IFileTypeFilter {

    List<ChooseFileInfo> doFilter(List<ChooseFileInfo> list);
}
```

在 FileConfig 的配置中，我们可以加上过滤的接口处理逻辑

```kotlin

class ChooseFileConfig(private val chooseFile: ChooseFile) {

    internal var mIFileTypeFilter: IFileTypeFilter? = null

        fun setTypeFilter(filter: IFileTypeFilter): ChooseFileConfig {
        mIFileTypeFilter = filter
        return this
    }

    ...
}
```

我们在最后返回的时候就可以这样：

```kotlin

    override fun getFileList(rootPath: String, callback: (fileList: List<ChooseFileInfo>, topInfo: ChooseFileInfo?) -> Unit) {

        ChooseFile.config?.mExecutor?.execute {

            // ... 获取文件操作

            //根据Filter过滤数据并排序
            val filterData = ChooseFile.config?.mIFileTypeFilter?.doFilter(listData) ?: listData
            FileUtil.SortFilesByInfo(filterData)

            //满数据回调
            callback(filterData, topInfo)
        }

    }


```

而排序的逻辑就是先展示文件夹，然后根据文件名排序：

```java

    public static void SortFilesByInfo(List<ChooseFileInfo> fileList) {
        Collections.sort(fileList, (o1, o2) -> {
            if (o1.isDir && (!o2.isDir))
                return -1;
            if ((!o1.isDir) && o2.isDir)
                return 1;
            return Collator.getInstance(java.util.Locale.CHINA).compare(o1.fileName, o2.fileName);
        });
    }

```

到此我们的整体的基本框架就完成了，下面看看视频的演示：

![Screen_recording_20230404_140405 00_00_00-00_00_30.gif](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/70123f54e4654093b2dae339cbce7e82~tplv-k3u1fbpfcp-watermark.image?)


使用：

```agsl
        findViewById<Button>(R.id.btn_get_file).setOnClickListener {

            ChooseFile.create(this)
                .setUIConfig(ChooseFileUIConfig.Builder().build())
                .setTypeFilter { listData ->
                    return@setTypeFilter ArrayList(listData.filter { item ->
                        //只要文件夹
//                          item.isDir

                        //只要文档文件
                        item.fileType == ChooseFile.FILE_TYPE_FOLDER ||
                                item.fileType == ChooseFile.FILE_TYPE_TEXT ||
                                item.fileType == ChooseFile.FILE_TYPE_PDF
                    })
                }
                .forResult {
                    Toast.makeText(this, "选中的文件：" + it?.fileName, Toast.LENGTH_SHORT).show()

                }
        }
```


