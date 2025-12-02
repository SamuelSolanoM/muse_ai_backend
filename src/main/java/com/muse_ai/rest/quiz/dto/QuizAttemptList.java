package com.muse_ai.rest.quiz.dto;

import java.time.LocalDateTime;

public class QuizAttemptList {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private String quizImage;
    private int score;
    private int totalQuestions;
    private LocalDateTime timestamp;

    public QuizAttemptList(Long attemptId, Long quizId, String quizTitle, String quizImage, int score, int totalQuestions, LocalDateTime timestamp) {
        this.attemptId = attemptId;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.quizImage = quizImage;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.timestamp = timestamp;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public String getQuizImage() {
        return quizImage;
    }

    public void setQuizImage(String quizImage) {
        this.quizImage = quizImage;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
