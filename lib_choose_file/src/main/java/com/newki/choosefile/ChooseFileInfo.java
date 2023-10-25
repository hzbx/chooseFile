package com.newki.choosefile;


import java.io.Serializable;
import java.util.Objects;

/**
 * 自定义文件对象
 */
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
