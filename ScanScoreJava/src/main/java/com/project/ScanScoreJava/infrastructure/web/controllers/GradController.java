package com.project.ScanScoreJava.infrastructure.web.controllers;

import com.project.ScanScoreJava.core.usecases.GradExamUseCase;
import com.project.ScanScoreJava.infrastructure.web.dto.ExamResultDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/grad")
public class GradController {

    private final GradExamUseCase gradExamUseCase;
    private final ObjectMapper objectMapper;

    public GradController(GradExamUseCase gradExamUseCase, ObjectMapper objectMapper) {
        this.gradExamUseCase = gradExamUseCase;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/grade", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExamResultDto> gradeExam(
            @RequestPart("file") MultipartFile file,
            @RequestParam("officialKey") String officialKeyJson) throws Exception {

        Map<Integer, Character> officialKey = objectMapper.readValue(
                officialKeyJson,
                new TypeReference<Map<Integer, Character>>() {}
        );

        ExamResultDto result = gradExamUseCase.execute(file.getBytes(), officialKey);

        return ResponseEntity.ok(result);
    }
}
