package org.example.task_import.application.service;

import java.io.IOException;
import java.util.*;
import org.example.task_import.application.exception.TaskImportException;
import org.example.task_import.application.exception.TaskImportValidationException;
import org.example.task_import.application.port.in.TaskImporter;
import org.example.task_import.application.port.outbound.TaskRowSource;
import org.example.task_import.domain.ImportedTask;
import org.example.task_import.domain.TaskImportPlan;
import org.example.task_import.domain.TaskRow;

import static org.example.task_import.application.exception.TaskImportValidationException.validationError;

/** Loop-based implementation of the task-import input port. */
public class ImperativeTaskImporter implements TaskImporter {

    /** Creates the imperative importer. */
    public ImperativeTaskImporter() {
    }

    @Override
    public TaskImportPlan importTasks(TaskRowSource source) {
        Objects.requireNonNull(source, "source is null");
        List<TaskRow> taskRows;
        try {
            taskRows = Objects.requireNonNull(source.load(), "TaskRowSource.load returned null");
        } catch (IOException e) {
            throw new TaskImportException("Task import failed while loading task rows", e);
        }

        List<ImportedTask> importedTasks = new ArrayList<>();
        Set<String> aggLabels = new LinkedHashSet<>();
        Set<UUID> taskIds = new HashSet<>();

        for (int i = 0; i < taskRows.size(); i++) {
            TaskRow taskRow = taskRows.get(i);
            if (taskRow == null) {
                throw validationError(i, null, "row", "taskRow is null");
            }
            if (taskRow.id() == null) {
                throw validationError(i, null, "id", "taskRow.id is null");
            }
            if (taskRow.title() == null) {
                throw validationError(i, taskRow.id(), "title", "taskRow.title is null");
            }
            if (taskRow.labels() == null) {
                throw validationError(i, taskRow.id(), "labels", "taskRow.labels is null");
            }
            for (int j = 0; j < taskRow.labels().size(); j++) {
                var label = taskRow.labels().get(j);
                if (label == null) {
                    throw validationError(i, taskRow.id(), "label["+j+"]", "label is null");
                }
            }
            if (!taskIds.add(taskRow.id())) {
                throw validationError(i, taskRow.id(), "id", "taskRow.id is already in use");
            }
            if (taskRow.title().trim().isBlank()) {
                throw validationError(i, taskRow.id(), "title", "taskRow.title is empty");
            }


            var title = taskRow.title().trim();
            var norLabels = new LinkedHashSet<String>();
            for (String label : taskRow.labels()) {
                var trimmedLabel = label.trim();
                if (trimmedLabel.isEmpty()) continue;
                var lowerCaseLabel = trimmedLabel.toLowerCase(Locale.ROOT);
                norLabels.add(lowerCaseLabel);
            }

            importedTasks.add(
                    new ImportedTask(
                            taskRow.id(),
                            title,
                            taskRow.priority(),
                            List.copyOf(norLabels)
                    )
            );
        }
        importedTasks.sort(ImportedTask.COMPARATOR);

        for (ImportedTask task : importedTasks) {
            aggLabels.addAll(task.labels());
        }

        return new TaskImportPlan(importedTasks, List.copyOf(aggLabels));
    }
}
