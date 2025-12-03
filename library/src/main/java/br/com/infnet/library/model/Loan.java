package br.com.infnet.library.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("loan")
public class Loan {
    @Id
    private Integer id;
    private UUID userId;
    private UUID bookId;
    private Instant lendAt;
    private Instant dueDate;
    private LoanStatus status;


    public enum LoanStatus {
        ACTIVE,
        OVERDUE,
        RETURNED;
    }
}