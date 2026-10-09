package com.portfolio.scheduler.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) { super("Task " + id + " was not found"); }
}
