package com.modelroute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modelroute.domain.FileOperationType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class FileOperationPlanParserTest {

    private final FileOperationPlanParser parser = new FileOperationPlanParser(new ObjectMapper());

    @Test
    void parsesJsonFromMarkdownCodeFence() {
        var plan = parser.parse("""
                ```json
                {
                  "operationType": "UPDATE_FILE",
                  "sourcePath": "nested/notes.txt",
                  "targetPath": null,
                  "content": "updated",
                  "summary": "Update the selected note."
                }
                ```
                """);

        assertThat(plan.operationType()).isEqualTo(FileOperationType.UPDATE_FILE);
        assertThat(plan.sourcePath()).isEqualTo("nested/notes.txt");
        assertThat(plan.content()).isEqualTo("updated");
    }

    @Test
    void rejectsNonJsonProviderResponse() {
        assertThatThrownBy(() -> parser.parse("I cannot create a plan."))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
    }
}
