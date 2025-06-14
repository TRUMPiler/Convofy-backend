package com.convofy.convofy.Entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table("user")
public class User {
    @PrimaryKey("userid")
    private String userid="";
    private boolean status=false;
    private LocalDate dob=null;
    private ZonedDateTime doj =null ;
    private String email="";
    private String name="";
    private String password="";
    private String phone="";

}