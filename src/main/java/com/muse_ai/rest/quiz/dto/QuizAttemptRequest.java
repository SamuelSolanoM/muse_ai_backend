package com.muse_ai.rest.quiz.dto;

import java.util.List;

public class QuizAttemptRequest {

    private Long userId;
    private Long quizId;
    private List<QuizAttemptAnswerRequest> answers;

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public List<QuizAttemptAnswerRequest> getAnswers() { return answers; }
    public void setAnswers(List<QuizAttemptAnswerRequest> answers) { this.answers = answers; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
