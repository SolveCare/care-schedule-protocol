package care.solve.protocol.schedule.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Schedule {

    private String scheduleId;
    private String ownerId;
    private List<Slot> slots;
}
