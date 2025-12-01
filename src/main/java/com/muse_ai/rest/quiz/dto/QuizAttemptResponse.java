package com.muse_ai.rest.quiz.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuizAttemptResponse {

    private Long attemptId;
    private Long quizId;
    private String quizTitle;

    private int score;
    private int totalQuestions;
    private double percentage;

    private LocalDateTime timestamp;
    private List<QuizAttemptAnswerResult> answers;

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

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<QuizAttemptAnswerResult> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuizAttemptAnswerResult> answers) {
        this.answers = answers;
    }
}
