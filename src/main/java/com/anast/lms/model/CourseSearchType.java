package com.anast.lms.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum CourseSearchType {
    ACTIVE("Актуальные"),
    DONE("Пройденые"),
    ALL("Все");

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    private String display;


    CourseSearchType(String display) {
        this.display = display;
    }

    private static final Map<String, CourseSearchType> lookup =
            Arrays.stream(values()).collect(Collectors.toMap(CourseSearchType::getDisplay, e ->e));

    public static CourseSearchType getEnum(String v) {
        return lookup.get(v);
    }
}
