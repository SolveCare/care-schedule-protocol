package care.solve.protocol.schedule.transformer;


import care.solve.protocol.schedule.entity.Doctor;
import care.solve.protocol.schedule.entity.ScheduleProtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorToProtoCollectionTransformer implements ProtoTransformer<List<Doctor>, ScheduleProtos.DoctorCollection> {

    private DoctorToProtoTransformer doctorToProtoTransformer;

    @Autowired
    public DoctorToProtoCollectionTransformer(DoctorToProtoTransformer doctorToProtoTransformer) {
        this.doctorToProtoTransformer = doctorToProtoTransformer;
    }

    @Override
    public ScheduleProtos.DoctorCollection transformToProto(List<Doctor> doctors) {

        List<ScheduleProtos.Doctor> protoDoctors = doctors.stream()
                .map(doctorToProtoTransformer::transformToProto)
                .collect(Collectors.toList());

        return ScheduleProtos.DoctorCollection.newBuilder()
                .addAllDoctors(protoDoctors)
                .build();
    }

    @Override
    public List<Doctor> transformFromProto(ScheduleProtos.DoctorCollection protoDoctorCollection) {

        return protoDoctorCollection.getDoctorsList().stream()
                .map(doctorToProtoTransformer::transformFromProto)
                .collect(Collectors.toList());
    }
}
