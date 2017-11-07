package care.solve.protocol.schedule.transformer;


import care.solve.protocol.schedule.entity.Patient;
import care.solve.protocol.schedule.entity.ScheduleProtos;
import org.springframework.stereotype.Service;

@Service
public class PatientToProtoTransformer implements ProtoTransformer<Patient, ScheduleProtos.Patient> {

    @Override
    public ScheduleProtos.Patient transformToProto(Patient obj) {
        return ScheduleProtos.Patient.newBuilder()
                .setPatientId(obj.getId())
                .build();
    }

    @Override
    public Patient transformFromProto(ScheduleProtos.Patient proto) {
        return Patient.builder()
                .id(proto.getPatientId())
                .build();
    }
}
