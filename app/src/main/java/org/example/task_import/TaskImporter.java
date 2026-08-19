package org.example.task_import;

public interface TaskImporter {
    TaskImportPlan importTasks(TaskRowSource source);
}
