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

    private Map<Integer, List<UploadingFileResource>> modulesFiles = new HashMap<>();

    private Map<Integer, List<UploadingFileResource>> taskFiles = new HashMap<>();

    private List<UploadingFileResource> filesForNewEntries = new ArrayList<>();


    public void registerFile(Integer parentEntryId, UploadingFileResource uploadingFileResource) {
        ResourceType resourceType = uploadingFileResource.getResourceType();

        //todo стоит ли непривязанные ресурсы делить на типы или всё в кучу?
        if(parentEntryId == null) {
            filesForNewEntries.add(uploadingFileResource);
            return;
        }

        if(ResourceType.module.equals(resourceType)) {
            registerModuleFile(parentEntryId, uploadingFileResource);

        } else if (ResourceType.task.equals(resourceType)) {
            registerTaskFile(parentEntryId, uploadingFileResource);
        }
    }

    private void registerModuleFile(Integer moduleId, UploadingFileResource uploadingFileResource) {
        if(moduleId == null) {
            //todo
            return;
        }
        if(modulesFiles.containsKey(moduleId)) {
            modulesFiles.get(moduleId).add(uploadingFileResource);
        } else {
            modulesFiles.put(moduleId, List.of(uploadingFileResource));
        }
    }

    private void registerTaskFile(Integer taskId, UploadingFileResource uploadingFileResource) {
        if(taskId == null) {
            //todo
            return;
        }
        if(taskFiles.containsKey(taskId)) {
            taskFiles.get(taskId).add(uploadingFileResource);
        } else {
            taskFiles.put(taskId, List.of(uploadingFileResource));
        }
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
