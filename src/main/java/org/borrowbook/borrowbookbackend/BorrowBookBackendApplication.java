package org.borrowbook.borrowbookbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BorrowBookBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BorrowBookBackendApplication.class, args);
    }

}
