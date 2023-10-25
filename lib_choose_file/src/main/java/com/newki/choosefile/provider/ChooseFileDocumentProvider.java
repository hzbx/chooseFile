package com.newki.choosefile.provider;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.os.EnvironmentCompat;


import com.newki.choosefile.ChooseFile;
import com.newki.choosefile.ChooseFileInfo;
import com.newki.choosefile.R;
import com.newki.choosefile.utils.FileUtil;
import com.newki.choosefile.utils.TimeUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * 选择文件
 */
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

    private Uri getFileUri(Context context, File file) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ChooseFileProvider.getUriForFile(context, "com.newki.choosefile.file.path.share", file);
        } else {
            return Uri.fromFile(file);
        }
    }

}
