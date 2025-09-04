package org.borrowbook.borrowbookbackend.exception.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class ExceptionResult {
    private final String message;
    private final HttpStatus status;
    private final ZonedDateTime timestamp;
}
