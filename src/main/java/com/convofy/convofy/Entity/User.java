package com.convofy.convofy.Entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table("user")
public class User {
    @PrimaryKey("userid")
    private String userid;
    private boolean status;
    private LocalDate dob;        // Change from String to LocalDate
    private ZonedDateTime doj;      // Matches the table's `doj` column (Cassandra `timestamp` type)
    private String email;  // Matches the table's `email` column
    private String name;   // Matches the table's `name` column
    private String password; // Matches the table's `password` column
    private String phone;  // Matches the table's `phone` column
}