package com.dhrubok.taskmaster.persistence.features.task.models;

import com.dhrubok.taskmaster.persistence.features.task.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusRequest {
    private TaskStatus status;
}
