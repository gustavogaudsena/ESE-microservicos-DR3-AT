package br.com.infnet.userCredits.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditRequest {

    private UUID userId;
    private Integer value;
}
