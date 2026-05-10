package com.japanesenote.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public class Note {

    private Long id;

    @Pattern(regexp = "vocab|grammar|other", message = "type must be vocab, grammar, or other")
    private String type;   // "vocab" | "grammar" | "other"

    @NotBlank(message = "title is required")
    private String title;

    private String meaning;
    private String example;
    private LocalDateTime createdAt;
    private Long userId;

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
