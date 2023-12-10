package com.anast.lms.service;

import com.anast.lms.model.CourseSearchType;

public class StudyUtils {

    public static Boolean defineSearchMode(String selectValue) {

        CourseSearchType type = CourseSearchType.getEnum(selectValue);
        switch (type) {
            case ACTIVE: return true;
            case DONE: return false;
            default: return null;
        }
    }
}
