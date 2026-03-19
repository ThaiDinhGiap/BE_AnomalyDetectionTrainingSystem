package com.sep490.anomaly_training_backend.scheduler;


import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewConfigRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingSampleReviewScheduler {

    private final Scheduler scheduler;
    private final TrainingSampleReviewConfigRepository configRepository;
    private final TrainingSampleReviewPolicyRepository policyRepository;
}
