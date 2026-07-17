package com.modelroute.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FileOperationIntentDetectorTest {

    private final FileOperationIntentDetector detector = new FileOperationIntentDetector();

    @Test
    void requiresBothMutationAndFileContext() {
        assertThat(detector.isMutation("把当前文件修改为两行内容", "nested/demo.txt")).isTrue();
        assertThat(detector.isMutation("创建文件 nested/result.md", null)).isTrue();
        assertThat(detector.isMutation("分析当前 Java 文件的问题", "Demo.java")).isFalse();
        assertThat(detector.isMutation("帮我修改学习计划", null)).isFalse();
        assertThat(detector.isMutation("帮我修改学习计划", "Demo.java")).isFalse();
    }
}
