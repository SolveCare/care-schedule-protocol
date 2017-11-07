package care.solve.protocol.schedule.service;

import care.solve.protocol.schedule.entity.DoctorPublic;

import java.io.IOException;
import java.util.List;

public interface DoctorService {
    DoctorPublic create(DoctorPublic doctorPublic);

    DoctorPublic get(String doctorId) throws IOException;

    List<DoctorPublic> getAll() throws IOException;
}
