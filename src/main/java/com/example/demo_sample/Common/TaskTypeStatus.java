package com.example.demo_sample.Common;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class TaskTypeStatus {

    public static final Integer DESIGN = 1;
    public static final Integer CODING = 2;
    public static final Integer TESTING = 3;
    public static final Integer REVIEW = 4;

    private TaskTypeStatus() {}

    public static List<Integer> getAllStatusValues() {
        return List.of(DESIGN, CODING, TESTING, REVIEW);
    }

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();

    static {
        STATUS_MAP.put(DESIGN, "Thiết kế");
        STATUS_MAP.put(CODING, "Lập trình");
        STATUS_MAP.put(TESTING, "Kiểm thử");
        STATUS_MAP.put(REVIEW, "Code Review");
    }

    public static String getStatusName(Integer status) {
        return STATUS_MAP.getOrDefault(status, " ");
    }

    public static Map<Integer, String> getStatusMap() {
        return STATUS_MAP;
    }
}
