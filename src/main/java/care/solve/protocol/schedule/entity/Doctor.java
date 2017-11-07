package care.solve.protocol.schedule.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Doctor {

    private String id;
    private String firstName;
    private String lastName;

}
