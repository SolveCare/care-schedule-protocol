package care.solve.protocol.schedule.service;

import care.solve.protocol.schedule.entity.PatientPublic;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface PatientService {
    PatientPublic create(PatientPublic patientPublic) throws InterruptedException, ExecutionException, InvalidProtocolBufferException;

    PatientPublic get(String patientId) throws IOException;
}
