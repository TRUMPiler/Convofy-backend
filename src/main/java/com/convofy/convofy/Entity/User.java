package com.convofy.convofy.Entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="\"user\"")
public class User {

    private String userid="";
    private boolean status=false;
    private LocalDate dob=null;
    @Id
    private String email="";
    private String name="";
    private String password="";
    private String phone="";
    private String image="";
}