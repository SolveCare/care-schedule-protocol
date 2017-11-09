package care.solve.protocol.schedule.transformer;

import care.solve.protocol.schedule.entity.ScheduleProtos;
import care.solve.protocol.schedule.entity.Slot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SlotTransformer implements ProtoTransformer<Slot, ScheduleProtos.Slot> {

    private RegistrationInfoTransformer registrationInfoTransformer;

    @Autowired
    public SlotTransformer(RegistrationInfoTransformer registrationInfoTransformer) {
        this.registrationInfoTransformer = registrationInfoTransformer;
    }

    @Override
    public ScheduleProtos.Slot transformToProto(Slot obj) {
        ScheduleProtos.Slot.Builder builder = ScheduleProtos.Slot.newBuilder();
        if (obj.getSlotId() != null) { builder.setSlotId(obj.getSlotId());}
        if (obj.getTimeStart() != null) { builder.setTimeStart(obj.getTimeStart());}
        if (obj.getTimeFinish() != null) { builder.setTimeFinish(obj.getTimeFinish());}
        if (obj.getAvailability() != null) { builder.setAvaliable(ScheduleProtos.Slot.Type.valueOf(obj.getAvailability().name()));}
        if (obj.getRegistrationInfo() != null) { builder.setRegistrationInfo(registrationInfoTransformer.transformToProto(obj.getRegistrationInfo()));}

        return builder.build();
    }

    @Override
    public Slot transformFromProto(ScheduleProtos.Slot proto) {
        Slot.SlotBuilder builder = Slot.builder()
                .timeStart(proto.getTimeStart())
                .timeFinish(proto.getTimeFinish());

        if (proto.getSlotId() != null) {builder.slotId(proto.getSlotId());}
        if (proto.getRegistrationInfo() != null) {builder.registrationInfo(registrationInfoTransformer.transformFromProto(proto.getRegistrationInfo()));}
        if (proto.getAvaliable() != null) {builder.availability(Slot.Type.valueOf(proto.getAvaliable().name()));}
        if (proto.getSlotId() != null) {builder.slotId(proto.getSlotId());}

        return builder.build();
    }
}
