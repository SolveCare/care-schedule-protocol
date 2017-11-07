package care.solve.protocol.schedule.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorPublic {

    private String id;
    private String firstName;
    private String lastName;

}
