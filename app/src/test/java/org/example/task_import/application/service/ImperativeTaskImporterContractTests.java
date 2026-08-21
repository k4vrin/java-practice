package org.example.task_import.application.service;

import org.example.task_import.application.port.in.TaskImporter;

class ImperativeTaskImporterValidationContractTest extends TaskImporterValidationContractTest {
    @Override
    protected TaskImporter createImporter() {
        return new ImperativeTaskImporter();
    }
}

class ImperativeTaskImporterNormalizationContractTest extends TaskImporterNormalizationContractTest {
    @Override
    protected TaskImporter createImporter() {
        return new ImperativeTaskImporter();
    }
}

class ImperativeTaskImporterOrderingContractTest extends TaskImporterOrderingContractTest {
    @Override
    protected TaskImporter createImporter() {
        return new ImperativeTaskImporter();
    }
}

class ImperativeTaskImporterAggregateLabelsContractTest extends TaskImporterAggregateLabelsContractTest {
    @Override
    protected TaskImporter createImporter() {
        return new ImperativeTaskImporter();
    }
}

class ImperativeTaskImporterRemainingBehaviorContractTest extends TaskImporterRemainingBehaviorContractTest {
    @Override
    protected TaskImporter createImporter() {
        return new ImperativeTaskImporter();
    }
}
