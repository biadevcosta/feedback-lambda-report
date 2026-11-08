package com.feedback.report.application.port.out;

import com.feedback.report.domain.model.Evaluation;
import java.time.LocalDate;
import java.util.List;

/** Reads evaluations by Brasília day. */
public interface EvaluationQueryRepository {

    List<Evaluation> findByDay(LocalDate day);
}
