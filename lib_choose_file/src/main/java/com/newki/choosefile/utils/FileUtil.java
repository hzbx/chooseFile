package com.newki.choosefile.utils;

import android.content.Context;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.telephony.mbms.FileInfo;

import androidx.annotation.RequiresApi;


import com.newki.choosefile.ChooseFileInfo;

import java.io.File;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class FileUtil {
    public static String sFileExtensions;

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

    //根据mime类型查看文件类型
    public static int getFileTypeForMimeType(String mimeType) {
        Integer value = sMimeTypeMap.get(mimeType);
        return (value == null ? 0 : value);
    }


    //获取文件夹下面的文件数量
    public static int getSubfolderNum(String path) {
        int i = 0;
        File[] files = new File(path).listFiles();
        if (files == null) return -1;
        for (File f : files) {
            if (f.getName().indexOf(".") != 0) {
                i++;
            }
        }
        return i;
    }

    //获取文件的大小
    public static String getFileSize(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        } else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        } else {
            if (size <= 0) {
                bytes.append("0B");
            } else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();

    }

    //根据File对象 文件名排序 （文件夹在上面，文件在下面）
    public static void SortFilesByName(List<File> fileList) {
        Collections.sort(fileList, (o1, o2) -> {
            if (o1.isDirectory() && o2.isFile())
                return -1;
            if (o1.isFile() && o2.isDirectory())
                return 1;
            return Collator.getInstance(java.util.Locale.CHINA).compare(o1.getName(), o2.getName());
        });
    }

    //根据自定义对象 文件名排序 （文件夹在上面，文件在下面）
    public static void SortFilesByInfo(List<ChooseFileInfo> fileList) {
        Collections.sort(fileList, (o1, o2) -> {
            if (o1.isDir && (!o2.isDir))
                return -1;
            if ((!o1.isDir) && o2.isDir)
                return 1;
            return Collator.getInstance(java.util.Locale.CHINA).compare(o1.fileName, o2.fileName);
        });
    }

}
