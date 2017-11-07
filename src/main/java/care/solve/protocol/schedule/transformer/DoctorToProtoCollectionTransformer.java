package care.solve.protocol.schedule.transformer;


import care.solve.protocol.schedule.entity.DoctorPublic;
import care.solve.protocol.schedule.entity.ScheduleProtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorToProtoCollectionTransformer implements ProtoTransformer<List<DoctorPublic>, ScheduleProtos.DoctorCollection> {

    private DoctorToProtoTransformer doctorToProtoTransformer;

    @Autowired
    public DoctorToProtoCollectionTransformer(DoctorToProtoTransformer doctorToProtoTransformer) {
        this.doctorToProtoTransformer = doctorToProtoTransformer;
    }

    @Override
    public ScheduleProtos.DoctorCollection transformToProto(List<DoctorPublic> doctors) {

        List<ScheduleProtos.DoctorPublic> protoDoctors = doctors.stream()
                .map(doctorToProtoTransformer::transformToProto)
                .collect(Collectors.toList());

        return ScheduleProtos.DoctorCollection.newBuilder()
                .addAllDoctors(protoDoctors)
                .build();
    }

    @Override
    public List<DoctorPublic> transformFromProto(ScheduleProtos.DoctorCollection protoDoctorCollection) {

        return protoDoctorCollection.getDoctorsList().stream()
                .map(doctorToProtoTransformer::transformFromProto)
                .collect(Collectors.toList());
    }
}
