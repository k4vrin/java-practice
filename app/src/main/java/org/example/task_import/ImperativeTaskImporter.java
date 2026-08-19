package org.example.task_import;

import java.io.IOException;
import java.util.*;

public class ImperativeTaskImporter implements TaskImporter {

    @Override
    public TaskImportPlan importTasks(TaskRowSource source) {
        List<TaskRow> taskRows;
        try {
            taskRows = source.load();
        } catch (IOException e) {
            throw new TaskImportException("Import tasks failed", e);
        }

        List<ImportedTask> importedTasks = new ArrayList<>();
        List<String> aggLabels = new ArrayList<>();
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
            aggLabels.addAll(norLabels);
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

        return new TaskImportPlan(importedTasks, aggLabels);
    }

    private TaskImportValidationException validationError(
            int rowIndex,
            UUID taskId,
            String field,
            String reason
    ) {
        return new TaskImportValidationException(
                "Task import validation failed: row=" + rowIndex
                        + ", taskId=" + taskId
                        + ", field=" + field
                        + ", reason=" + reason
        );
    }
}
