package care.solve.protocol.schedule.transformer;

import care.solve.protocol.schedule.entity.Schedule;
import care.solve.protocol.schedule.entity.ScheduleProtos;
import care.solve.protocol.schedule.entity.Slot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleTransformer implements ProtoTransformer<Schedule, ScheduleProtos.Schedule> {

    private SlotTransformer slotTransformer;

    @Autowired
    public ScheduleTransformer(SlotTransformer slotTransformer) {
        this.slotTransformer = slotTransformer;
    }

    @Override
    public ScheduleProtos.Schedule transformToProto(Schedule obj) {
        List<Slot> scheduleRecords = obj.getSlots();
        ScheduleProtos.Schedule.Builder builder = ScheduleProtos.Schedule.newBuilder();

        if (scheduleRecords != null) {
            List<ScheduleProtos.Slot> protoScheduleRecordList = scheduleRecords.stream()
                    .map(slotTransformer::transformToProto)
                    .collect(Collectors.toList());

            builder.addAllSlots(protoScheduleRecordList);
        }

        if (obj.getOwnerId() != null) { builder.setOwnerId(obj.getOwnerId()); }
        if (obj.getScheduleId() != null) { builder.setScheduleId(obj.getScheduleId()); }

        return builder.build();
    }

    @Override
    public Schedule transformFromProto(ScheduleProtos.Schedule proto) {
        List<ScheduleProtos.Slot> protoScheduleRecords = proto.getSlotsList();

        Schedule.ScheduleBuilder builder = Schedule.builder();

        if (protoScheduleRecords != null) {
            List<Slot> scheduleRecordList = protoScheduleRecords.stream()
                    .map(slotTransformer::transformFromProto)
                    .collect(Collectors.toList());

            builder.slots(scheduleRecordList);
        }

        return builder
                .ownerId(proto.getOwnerId())
                .scheduleId(proto.getScheduleId())
                .build();
    }
}
