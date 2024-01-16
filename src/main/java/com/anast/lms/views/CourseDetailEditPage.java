package com.anast.lms.views;

import com.anast.lms.model.*;
import com.anast.lms.model.course.*;
import com.anast.lms.service.StudyUtils;
import com.anast.lms.service.external.CourseServiceClient;
import com.anast.lms.service.security.SecurityService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Route(value = "/my_courses/:id?/edit", layout=MainLayout.class)
@PageTitle("Мои курсы | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class CourseDetailEditPage extends VerticalLayout implements HasUrlParameter<Integer>, BeforeEnterObserver {

    private final SecurityService securityService;
    private final CourseServiceClient courseClient;

    private Integer currentCourseId;
    private VerticalLayout modulesLayout;
    private ModulesUpdateRequest request = new ModulesUpdateRequest();
    private UploadingCourseFilesContext uploadingContext = new UploadingCourseFilesContext();

    private final static String MODULE_TITLE_FIELD_ID = "module_title";
    private final static String CONTENT_FIELD_ID = "module_content";
    private final static String MODULE_MATERIALS_LAYOUT_ID = "module_materials_layout";
    private final static String TASK_TITLE_FIELD_ID = "task_title";
    private final static String TASK_DEADLINE_FIELD_ID = "task_deadline";
    private final static String TASK_DESCRIPTION_FIELD_ID = "task_description";
    private final static String TASKS_LAYOUT_ID = "task_layout";
    private final static String TASK_MATERIALS_LAYOUT_ID = "task_materials_layout";
    private final static String TASK_TYPE_SELECT_ID = "task_type_select_id";




    public CourseDetailEditPage(SecurityService securityService, CourseServiceClient courseClient) {
        this.securityService = securityService;
        this.courseClient = courseClient;
        this.modulesLayout = new VerticalLayout();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        build();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Integer integer) {
        currentCourseId = integer;

    }

    private void build() {

        buildPanel();

        CourseFullInfoResponse courseDetailPage = courseClient.getCourseFullInfo(currentCourseId);
        Course course = courseDetailPage.getCourse();
        DisciplineInstance discipline = course.getDiscipline();

        Label title = new Label(discipline.getTitle());
        title.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "bold");

        Label description = new Label(discipline.getSpecialty() + " " +
                discipline.getStudyFormShortName() + " форма. " + discipline.getStageName());
        add(title, description);

        if(discipline.getDescription() != null && !discipline.getDescription().isEmpty()) {
            Label discDescription = new Label(discipline.getDescription());
            add(discDescription);
        }

        Button addModule = new Button("Добавить модуль", new Icon(VaadinIcon.PLUS));
        addModule.addClickListener(e -> modulesLayout.add(buildModuleItem(null)));

        List<CourseModule> modules = courseDetailPage.getModules();
        modules.forEach(module -> modulesLayout.add(buildModuleItem(module)));

        add(addModule, modulesLayout);

        //если много контента, дублруем панель вниз
        if(modules.size() > 1) {
            buildPanel();
        }

    }


    private HorizontalLayout buildModuleItem(CourseModule module) {

        HorizontalLayout itemMainLayout = new HorizontalLayout();
        VerticalLayout moduleLayout = new VerticalLayout();

        TextField moduleTitle = new TextField();
        moduleTitle.setPlaceholder("Название модуля");
        moduleTitle.setWidth("80%");
        moduleTitle.setId(MODULE_TITLE_FIELD_ID);

        TextArea content = new TextArea();
        content.setPlaceholder("Детали курса");
        content.setWidthFull();
        content.setMaxHeight("400px");
        content.setId(CONTENT_FIELD_ID);

        //ресурсы
        VerticalLayout moduleMaterials = new VerticalLayout();
        moduleMaterials.setId(MODULE_MATERIALS_LAYOUT_ID);
        //задания
        VerticalLayout tasksLayout = new VerticalLayout();
        tasksLayout.setId(TASKS_LAYOUT_ID);

        if(module != null) {
            itemMainLayout.setId(module.getId().toString());
            moduleTitle.setValue(module.getTitle());
            content.setValue(module.getContent());
            appendResources(moduleMaterials, module.getResources(), false);
            appendTasks(tasksLayout, module.getModuleTasks(), false);
        }

        moduleLayout.add(moduleTitle, content, moduleMaterials);
        moduleLayout.getStyle().set("width", "95%");
        moduleLayout.setSpacing(false);

        //прикрепление файлов для ресурсов
        appendUploadElement(moduleLayout, moduleMaterials, UploadingCourseFilesContext.ResourceType.module);

        //задачи
        Button addTask = new Button("Добавить задание", new Icon(VaadinIcon.PLUS));
        addTask.addClickListener(e -> appendTasks(tasksLayout, List.of(new Task()), true));
        addTask.setId("add_task_button");
        moduleLayout.add(tasksLayout, addTask);

        //кнопка удаления в правом столбце
        Button deleteModule = new Button(new Icon(VaadinIcon.TRASH));
        deleteModule.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        deleteModule.addClickListener(e -> {
            registerDeletedModule(itemMainLayout);
            modulesLayout.remove(itemMainLayout);
        });

        itemMainLayout.add(moduleLayout, deleteModule);
        itemMainLayout.setSpacing(false);
        itemMainLayout.getStyle().set("width", "70%")
                .set("border", "1px solid cadetblue")
                .set("border-radius", "var(--lumo-border-radius-s)");

        return itemMainLayout;
    }


    void buildPanel() {

        Button done = new Button("Сохранить");
        done.addClickListener(e -> {
           request.setModules(getUpdatedModulesData());
           try{
               courseClient.updateCourseModules(currentCourseId, request);
               try {
                   uploadFilesData();
               } catch (Exception ex) {
                   //todo clear resources
                   Notification notification = new Notification("Произошла ошибке при сохранении загруженных файлов");
                   notification.open();
                   ex.printStackTrace();
               }

               done.getUI().ifPresent(ui -> ui.navigate(
                       CourseDetailPage.class, currentCourseId));
           } catch (Exception ex) {
               Notification notification = new Notification("Ошибка при сохранении данных курса");
               notification.open();
               ex.printStackTrace();
           }
        });

        Button cancel = new Button("Отмена");
        cancel.addClickListener(e -> cancel.getUI().ifPresent(ui -> ui.navigate(
                CourseDetailPage.class, currentCourseId)
        ));

        HorizontalLayout layout = new HorizontalLayout(done, cancel);
        add(layout);
    }

    private List<CourseModule> getUpdatedModulesData() {
        List<CourseModule> updatedModules = new ArrayList<>();

        var counterRef = new Object() {
            short orderCount = 1;
        };

        modulesLayout.getChildren().forEach(moduleLayout -> {

            Integer moduleId = StudyUtils.getIntegerIdFromComponent(moduleLayout);

            Component contentLayout = moduleLayout.getChildren().findFirst().get();
            TextField titleComponent = (TextField) contentLayout.getChildren().filter(c -> MODULE_TITLE_FIELD_ID.equals(c.getId().get())).findAny().get();
            TextArea contentComponent = (TextArea) contentLayout.getChildren().filter(c -> CONTENT_FIELD_ID.equals(c.getId().get())).findAny().get();
            VerticalLayout resourcesLayout = (VerticalLayout) contentLayout.getChildren().filter(c -> MODULE_MATERIALS_LAYOUT_ID.equals(c.getId().get())).findAny().get();
            VerticalLayout tasksLayout = (VerticalLayout) contentLayout.getChildren().filter(c -> TASKS_LAYOUT_ID.equals(c.getId().get())).findAny().get();

            String title = titleComponent.getValue();
            String content = contentComponent.getValue();

            List<ModuleResource> moduleResources = getUpdatedResources(resourcesLayout);
            List<Task> moduleTasks = getUpdatedTasks(tasksLayout);

            CourseModule module = new CourseModule(moduleId, title, content, counterRef.orderCount, moduleResources, moduleTasks);
            updatedModules.add(module);
            counterRef.orderCount++;
        });

        return updatedModules;
    }

    private void registerDeletedModule(HorizontalLayout itemMainLayout) {
        Integer moduleId = StudyUtils.getIntegerIdFromComponent(itemMainLayout);
        if(moduleId != null) {
            request.getDeletedModulesId().add(moduleId);
        }
    }

    private void registerDeletedTask(HorizontalLayout itemTaskLayout) {
        Integer taskId = StudyUtils.getIntegerIdFromComponent(itemTaskLayout);
        if(taskId != null) {
            request.getDeletedTasks().add(taskId);
        }
    }

    private void registerDeletedResource(HorizontalLayout resourceLayout) {
        Integer resourceId = StudyUtils.getIntegerIdFromComponent(resourceLayout);
        if(resourceId != null) {
            request.getDeletedResources().add(resourceId);
        }
    }

    /**
     * Отображение списка ресурсов модуля
     *
     * @param layout
     * @param resources
     * @param isNew - если ресурс был загружен, но еще не сохранен, отобразить элемент, но ссылка фиктивная.
     *                  Отображение необходимо для дальнейшего парсинга по элементам
     */
    private void appendResources(VerticalLayout layout, List<ModuleResource> resources, boolean isNew) {

        for (ModuleResource resource : resources) {

            HorizontalLayout resourceItemLayout = new HorizontalLayout();
            if(!isNew) {
                resourceItemLayout.setId(resource.getId().toString());
            }

            StreamResource streamResource = new StreamResource(resource.getDisplayFileName(), (InputStreamFactory) () -> {
                if(isNew) {
                    return new ByteArrayInputStream(new byte[0]);
                } else {
                    byte[] fileDataArray = courseClient.getFileData(resource).getBody();
                    return new ByteArrayInputStream(fileDataArray);
                }
            });

            Button deleteResourceButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            deleteResourceButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            deleteResourceButton.addClickListener(e -> {
                registerDeletedResource(resourceItemLayout);
                layout.remove(resourceItemLayout);
            });

            Anchor anchor = new Anchor(streamResource, resource.getDisplayFileName());
            anchor.setId(resource.getFileName());
            anchor.setTarget( "_blank" );  // Specify `_blank` to open in a new browser tab/window.

            resourceItemLayout.add(deleteResourceButton, anchor);
            resourceItemLayout.setSpacing(false);
            resourceItemLayout.setVerticalComponentAlignment(Alignment.CENTER);
            layout.add(resourceItemLayout);
        }
    }

    private void appendTasks(VerticalLayout layout, List<Task> tasks, boolean isNew) {

        for (Task task : tasks) {

            HorizontalLayout taskItemLayout = new HorizontalLayout();
            if(!isNew) {
                taskItemLayout.setId(task.getId().toString());
            }

            VerticalLayout taskContentLayout = new VerticalLayout();
            VerticalLayout taskMaterialsLayout = new VerticalLayout();
            taskMaterialsLayout.setId(TASK_MATERIALS_LAYOUT_ID);

            TextField titleTextField = new TextField();
            titleTextField.setPlaceholder("Наименование задания");
            titleTextField.setWidth("60%");
            titleTextField.setId(TASK_TITLE_FIELD_ID);

            Select<TaskType> taskTypeSelect = new Select<>();
            taskTypeSelect.setItems(TaskType.values());
            taskTypeSelect.setItemLabelGenerator(TaskType::getTitle);
            taskTypeSelect.setPlaceholder("Тип задания");
            taskTypeSelect.setId(TASK_TYPE_SELECT_ID);

            DatePicker deadLine = new DatePicker();
            deadLine.setLabel("Срок сдачи");
            deadLine.setId(TASK_DEADLINE_FIELD_ID);

            TextArea taskDescription = new TextArea();
            taskDescription.setPlaceholder("Описание");
            taskDescription.setId(TASK_DESCRIPTION_FIELD_ID);
            taskDescription.getStyle().set("font-size", "var(--lumo-font-size-s)");
            taskDescription.setWidthFull();
            taskDescription.setMaxHeight("200px");


            if(!isNew) {
                titleTextField.setValue(task.getTitle());
                deadLine.setValue(task.getDeadLine());
                taskDescription.setValue(task.getDescription());
                taskTypeSelect.setValue(TaskType.getTaskTypeEnum(task.getTaskType()));
                appendResources(taskMaterialsLayout, task.getResources(), false);
            }

            taskContentLayout.add(titleTextField, taskTypeSelect, deadLine, taskDescription, taskMaterialsLayout);

            //прикрепленае файлов для задач
            appendUploadElement(taskContentLayout, taskMaterialsLayout, UploadingCourseFilesContext.ResourceType.task);

            Button deleteTask = new Button(new Icon(VaadinIcon.TRASH));
            deleteTask.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            deleteTask.addClickListener(e -> {
               registerDeletedTask(taskItemLayout);
                layout.remove(taskItemLayout);
            });

            taskContentLayout.setSpacing(false);
            taskItemLayout.getStyle().set("width", "90%")
                    .set("border", "1px solid cadetblue")
                    .set("border-radius", "var(--lumo-border-radius-s)");

            taskItemLayout.add(taskContentLayout, deleteTask);
            taskItemLayout.setSpacing(false);
            taskItemLayout.setVerticalComponentAlignment(Alignment.START);

            layout.add(taskItemLayout);
        }
    }

    /**
     * Размещение элемента для загрузки файлов
     *
     * @param parentLayout лэйаут, где будет размещен элемент
     * @param contentLayout лэйаут, где будет отображен новый ресурс
     * @param resourceType тип объекта родителя
     */
    private void appendUploadElement(VerticalLayout parentLayout, VerticalLayout contentLayout,
                                     UploadingCourseFilesContext.ResourceType resourceType) {
        MemoryBuffer memoryBuffer = new MemoryBuffer();
        Upload materialUpload = new Upload(memoryBuffer);
        materialUpload.setId("upload");

        materialUpload.addSucceededListener(event -> {

            UploadingCourseFilesContext.UploadingFileResource resource = new UploadingCourseFilesContext.UploadingFileResource(
                    UUID.randomUUID().toString() + "_" + event.getFileName(),
                    event.getFileName(),
                    event.getContentLength(),
                    memoryBuffer.getInputStream(),
                    resourceType
            );
            uploadingContext.registerFile(resource);

            //фиктивное отображение нового ресурса в списке
            ModuleResource newModuleResource = new ModuleResource(null, resource.getFileName(), resource.getDisplayFileName());
            appendResources(contentLayout, List.of(newModuleResource), true);
        });

        parentLayout.add(materialUpload);
    }

    /**
     * Перебираем лэйаут ресурсов модуля, формируем список. Получаем имя файла из тайтла ссылки
     */
    private List<ModuleResource> getUpdatedResources(VerticalLayout resourcesLayout) {
        List<ModuleResource> resourceList = new ArrayList<>();

        resourcesLayout.getChildren().forEach(resLayout -> {
            Integer resourceId = StudyUtils.getIntegerIdFromComponent(resLayout);
            Anchor resAnchor = (Anchor) resLayout.getChildren().collect(Collectors.toList()).get(1);
            resourceList.add(new ModuleResource(resourceId, resAnchor.getId().get(), resAnchor.getText()));
        });
        return resourceList;
    }

    /**
     * Перебираем лэйаут задач, формируем список. Для каждой задачи получием ресурсы
     */
    private List<Task> getUpdatedTasks(VerticalLayout tasksLayout) {
        List<Task> tasksList = new ArrayList<>();

        tasksLayout.getChildren().forEach( taskItemLayout -> {
            Integer taskId = StudyUtils.getIntegerIdFromComponent(taskItemLayout);

            VerticalLayout taskContentLayout = (VerticalLayout) taskItemLayout.getChildren().collect(Collectors.toList()).get(0);
            TextField titleComponent = (TextField) getChildComponentById(taskContentLayout, TASK_TITLE_FIELD_ID);
            DatePicker deaLinePicker = (DatePicker) getChildComponentById(taskContentLayout, TASK_DEADLINE_FIELD_ID);
            TextArea taskDescriptionTextArea = (TextArea) getChildComponentById(taskContentLayout, TASK_DESCRIPTION_FIELD_ID);
            Select<TaskType> taskTypeSelect = (Select) getChildComponentById(taskContentLayout, TASK_TYPE_SELECT_ID);
            VerticalLayout resourcesLayout = (VerticalLayout) getChildComponentById(taskContentLayout, TASK_MATERIALS_LAYOUT_ID);

            List<ModuleResource> taskResources = getUpdatedResources(resourcesLayout);
            TaskType taskType = taskTypeSelect.getValue() != null ? taskTypeSelect.getValue() : TaskType.practical;
            Task updatedTask = new Task(taskId, taskType.getCode(), titleComponent.getValue(),
                    taskDescriptionTextArea.getValue(), deaLinePicker.getValue(), taskResources);
            tasksList.add(updatedTask);
        });
        return tasksList;
    }

    private void uploadFilesData() throws IOException {
        for(UploadingCourseFilesContext.UploadingFileResource resource : uploadingContext.getAllFilesToSave()) {
            courseClient.uploadFileData(resource.getFileData().readAllBytes(), resource.getFileName());
        }
    }

    private Component getChildComponentById(@NotNull Component parent, @NotNull String targetId) {
        return parent.getChildren().filter(c -> targetId.equals(c.getId().get())).findAny().get();
    }
}

