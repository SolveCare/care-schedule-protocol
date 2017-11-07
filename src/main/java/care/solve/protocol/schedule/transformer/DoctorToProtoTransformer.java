package care.solve.protocol.schedule.transformer;

import care.solve.protocol.schedule.entity.Doctor;
import care.solve.protocol.schedule.entity.ScheduleProtos;
import org.springframework.stereotype.Service;

@Service
public class DoctorToProtoTransformer implements ProtoTransformer<Doctor, ScheduleProtos.Doctor> {

    @Override
    public ScheduleProtos.Doctor transformToProto(Doctor doctor) {

        return ScheduleProtos.Doctor.newBuilder()
                .setDoctorId(doctor.getId())
                .setFirstName(doctor.getFirstName())
                .setLastName(doctor.getLastName())
                .build();
    }

    @Override
    public Doctor transformFromProto(ScheduleProtos.Doctor protoDoctor) {
        return Doctor.builder()
                .id(protoDoctor.getDoctorId())
                .firstName(protoDoctor.getFirstName())
                .lastName(protoDoctor.getLastName())
                .build();
    }
}
