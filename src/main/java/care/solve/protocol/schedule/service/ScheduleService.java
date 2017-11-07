package care.solve.protocol.schedule.service;

import care.solve.protocol.schedule.entity.Schedule;
import care.solve.protocol.schedule.entity.Slot;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;

import java.io.IOException;

public interface ScheduleService {
    Schedule createSchedule(Schedule schedule);

    Slot createSlot(String scheduleId, Slot slot);

    void updateSlot(String scheduleId, String slotId, Slot slot);

    Schedule getSchedule(String ownerId) throws IOException, ChaincodeEndorsementPolicyParseException;
}
