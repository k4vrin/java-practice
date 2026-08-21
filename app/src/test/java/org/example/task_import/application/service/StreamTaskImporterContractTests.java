package org.example.task_import.application.service;

import org.example.task_import.application.port.in.TaskImporter;

class StreamTaskImporterValidationContractTest extends TaskImporterValidationContractTest {
    @Override
    protected TaskImporter createImporter() {
        return new StreamTaskImporter();
    }
}

class StreamTaskImporterNormalizationContractTest extends TaskImporterNormalizationContractTest {
    @Override
    protected TaskImporter createImporter() {
        return new StreamTaskImporter();
    }
}

class StreamTaskImporterOrderingContractTest extends TaskImporterOrderingContractTest {
    @Override
    protected TaskImporter createImporter() {
        return new StreamTaskImporter();
    }
}

class StreamTaskImporterAggregateLabelsContractTest extends TaskImporterAggregateLabelsContractTest {
    @Override
    protected TaskImporter createImporter() {
        return new StreamTaskImporter();
    }
}

class StreamTaskImporterRemainingBehaviorContractTest extends TaskImporterRemainingBehaviorContractTest {
    @Override
    protected TaskImporter createImporter() {
        return new StreamTaskImporter();
    }
}
