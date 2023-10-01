package com.destrostudios.icetea.core.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {

    public static <T> List<List<T>> split(List<T> list, int maxSubLists) {
        List<List<T>> subLists = new ArrayList<>(maxSubLists);
        int listSize = list.size();
        int subListSize = Math.ceilDiv(listSize, maxSubLists);
        for (int start = 0; start < listSize; start += subListSize) {
            int end = Math.min(start + subListSize, listSize);
            subLists.add(list.subList(start, end));
        }
        return subLists;
    }
}
