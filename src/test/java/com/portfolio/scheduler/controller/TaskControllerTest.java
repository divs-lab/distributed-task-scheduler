package com.portfolio.scheduler.controller;

import com.portfolio.scheduler.dto.*;
import com.portfolio.scheduler.entity.ScheduledTask;
import com.portfolio.scheduler.enums.*;
import com.portfolio.scheduler.exception.ApiExceptionHandler;
import com.portfolio.scheduler.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(ApiExceptionHandler.class)
class TaskControllerTest {
    @Autowired MockMvc mvc;
    @MockBean TaskService tasks;
    @MockBean ExecutionService executions;

    @Test void acceptsValidScheduleAndReturnsDto() throws Exception {
        ScheduledTask task = new ScheduledTask(TaskType.EMAIL, Instant.parse("2026-10-10T10:00:00Z"), 3);
        when(tasks.schedule(any())).thenReturn(task);
        mvc.perform(post("/api/v1/tasks/schedule").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"taskType":"EMAIL","executionTime":"2026-10-10T10:00:00Z","maxRetries":3}
                                """))
                .andExpect(status().isAccepted()).andExpect(jsonPath("$.status").value("PENDING"));
    }
    @Test void rejectsMissingFields() throws Exception {
        mvc.perform(post("/api/v1/tasks/schedule").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskType\":\"EMAIL\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(tasks);
    }
    @Test void validatesListPageSize() throws Exception {
        mvc.perform(get("/api/v1/tasks").param("size", "101"))
                .andExpect(status().isBadRequest());
    }
    @Test void mapsMissingTaskToNotFound() throws Exception {
        when(tasks.get(42L)).thenThrow(new com.portfolio.scheduler.exception.TaskNotFoundException(42L));
        mvc.perform(get("/api/v1/tasks/42")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }
    @Test void cancelsPendingTask() throws Exception {
        mvc.perform(delete("/api/v1/tasks/4")).andExpect(status().isNoContent());
        verify(tasks).cancel(4L);
    }
    @Test void reportsConflictWhenRetryStateIsInvalid() throws Exception {
        doThrow(new com.portfolio.scheduler.exception.InvalidTaskStateException("Only failed tasks can be retried"))
                .when(tasks).retry(9L);
        mvc.perform(post("/api/v1/tasks/9/retry"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
    }
}
