package com.newki.choosefile;

import java.util.List;

public interface IFileTypeFilter {

    List<ChooseFileInfo> doFilter(List<ChooseFileInfo> list);
}
