package com.ruoyi.business.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 图片只承载内容，不参与答案值和评分。 */
public class QuestionMedia {
    public int schemaVersion = 1;
    public List<ImageRef> stem = new ArrayList<>();
    public Map<String, List<ImageRef>> options = new LinkedHashMap<>();

    public QuestionMedia() {
        for (String key : new String[] {"A", "B", "C", "D"}) options.put(key, new ArrayList<>());
    }

    public static class ImageRef {
        public String imageId;
        public ImageRef() { }
        public ImageRef(String imageId) { this.imageId = imageId; }
    }
}
