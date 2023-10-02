package com.jia.loanprojectionsservice.infrastructure.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomErrorsDTO {

    /**
     * The local date & time
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;

    /**
     * The Message.
     */
    private String message;

}
