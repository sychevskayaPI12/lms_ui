package com.anast.lms.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для временного хранения загруженных файлов
 */
public class UploadingCourseFilesContext {

    private List<UploadingFileResource> moduleResourceFiles = new ArrayList<>();

    private List<UploadingFileResource> taskResourceFiles = new ArrayList<>();


    public void registerFile(UploadingFileResource uploadingFileResource) {
        ResourceType resourceType = uploadingFileResource.getResourceType();
        if(ResourceType.module.equals(resourceType)) {
            moduleResourceFiles.add(uploadingFileResource);
        } else if (ResourceType.task.equals(resourceType)) {
            taskResourceFiles.add(uploadingFileResource);
        }
    }

    public List<UploadingFileResource> getAllFilesToSave() {
        List<UploadingFileResource> res = this.moduleResourceFiles;
        res.addAll(this.taskResourceFiles);
        return res;
    }
    public List<UploadingFileResource> getModuleResourceFiles() {
        return moduleResourceFiles;
    }

    public void setModuleResourceFiles(List<UploadingFileResource> moduleResourceFiles) {
        this.moduleResourceFiles = moduleResourceFiles;
    }

    public List<UploadingFileResource> getTaskResourceFiles() {
        return taskResourceFiles;
    }

    public void setTaskResourceFiles(List<UploadingFileResource> taskResourceFiles) {
        this.taskResourceFiles = taskResourceFiles;
    }

    /**
     * Вложенный класс для хранения данных о файле
     */
    public static class UploadingFileResource {

        private String fileName;
        private String displayFileName;
        private long contentLength;
        private InputStream fileData;
        private ResourceType resourceType;

        public UploadingFileResource(String fileName, String displayFileName, long contentLength,
                                     InputStream fileData, ResourceType resourceType) {
            this.fileName = fileName;
            this.displayFileName = displayFileName;
            this.contentLength = contentLength;
            this.fileData = fileData;
            this.resourceType = resourceType;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public long getContentLength() {
            return contentLength;
        }

        public void setContentLength(long contentLength) {
            this.contentLength = contentLength;
        }

        public InputStream getFileData() {
            return fileData;
        }

        public void setFileData(InputStream fileData) {
            this.fileData = fileData;
        }

        public ResourceType getResourceType() {
            return resourceType;
        }

        public void setResourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
        }

        public String getDisplayFileName() {
            return displayFileName;
        }

        public void setDisplayFileName(String displayFileName) {
            this.displayFileName = displayFileName;
        }
    }

    public enum ResourceType {
        module,
        task;
    }
}
