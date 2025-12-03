package br.com.infnet.userCredits.model;

import br.com.infnet.userCredits.exception.InvalidCreditValueException;
import br.com.infnet.userCredits.exception.NoCreditAvailableException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("credits")
public class Credit {

    @Id
    private Integer id;
    private UUID userId;
    private Integer credit;
    private Instant updatedAt;
    private Instant renewAt;
    private Instant lastUsedAt;

    public void addCredit(Integer value) {
        if (value <= 0) {
            throw new InvalidCreditValueException(value);
        }

        this.credit += value;
        this.updatedAt = Instant.now();
    }

    public void useCredit() {
        if (this.credit <= 0) {
            throw new NoCreditAvailableException();
        }

        this.credit--;
        this.lastUsedAt = (new Date()).toInstant();
        this.updatedAt = Instant.now();
    }
}
