package br.com.infnet.userCredits.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditResponse {

    private UUID userId;
    private Integer credit;
    private String renewAt;
}
