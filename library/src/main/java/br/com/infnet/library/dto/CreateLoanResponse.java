package br.com.infnet.library.dto;

import br.com.infnet.library.model.Loan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLoanResponse {

    private UUID userId;
    private UUID bookId;
    private String dueDate;
    private Loan.LoanStatus status;
}
