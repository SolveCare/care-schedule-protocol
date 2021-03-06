package care.solve.protocol.schedule.transformer;

import care.solve.protocol.schedule.entity.RegistrationInfo;
import care.solve.protocol.schedule.entity.ScheduleProtos;
import org.springframework.stereotype.Service;

@Service
public class RegistrationInfoTransformer implements ProtoTransformer<RegistrationInfo, ScheduleProtos.RegistrationInfo> {

    @Override
    public ScheduleProtos.RegistrationInfo transformToProto(RegistrationInfo obj) {
        return ScheduleProtos.RegistrationInfo.newBuilder()
                .setDescription(obj.getDescription())
                .setAttendeeId(obj.getAttendeeId())
                .build();
    }

    @Override
    public RegistrationInfo transformFromProto(ScheduleProtos.RegistrationInfo proto) {
        return RegistrationInfo.builder()
                .description(proto.getDescription())
                .attendeeId(proto.getAttendeeId())
                .build();
    }
}
