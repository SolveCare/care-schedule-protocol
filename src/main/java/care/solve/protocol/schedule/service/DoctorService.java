package care.solve.protocol.schedule.service;

import care.solve.protocol.schedule.entity.Doctor;

import java.io.IOException;
import java.util.List;

public interface DoctorService {
    Doctor create(Doctor doctor);

    Doctor get(String doctorId) throws IOException;

    List<Doctor> getAll() throws IOException;
}
