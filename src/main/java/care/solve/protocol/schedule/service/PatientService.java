package care.solve.protocol.schedule.service;

import care.solve.protocol.schedule.entity.Patient;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface PatientService {
    Patient create(Patient patient) throws InterruptedException, ExecutionException, InvalidProtocolBufferException;

    Patient get(String patientId) throws IOException;
}
