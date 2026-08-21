package org.example.task_import.application.service;

import org.example.task_import.application.exception.TaskImportException;
import org.example.task_import.application.port.in.TaskImporter;
import org.example.task_import.application.port.outbound.TaskRowSource;
import org.example.task_import.domain.ImportedTask;
import org.example.task_import.domain.TaskImportPlan;
import org.example.task_import.domain.TaskRow;

import java.io.IOException;
import java.util.*;

import static org.example.task_import.application.exception.TaskImportValidationException.validationError;

/** Sequential stream-based implementation of the task-import input port. */
public class StreamTaskImporter implements TaskImporter {

    /** Creates the stream importer. */
    public StreamTaskImporter() {
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
        }

        List<ImportedTask> importedTasks = taskRows.stream()
                .map(StreamTaskImporter::toImportedTask)
                .sorted(ImportedTask.COMPARATOR)
                .toList();

        List<String> aggregateLabels = importedTasks.stream()
                .flatMap(task -> task.labels().stream())
                .distinct()
                .toList();

        return new TaskImportPlan(importedTasks, aggregateLabels);
    }

    private static ImportedTask toImportedTask(TaskRow taskRow) {
        return new ImportedTask(
                taskRow.id(),
                taskRow.title().trim(),
                taskRow.priority(),
                normalizeLabels(taskRow.labels())
        );
    }

    private static List<String> normalizeLabels(List<String> labels) {
        return labels.stream()
                .map(String::trim)
                .filter(label -> !label.isEmpty())
                .map(label -> label.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    }
}
