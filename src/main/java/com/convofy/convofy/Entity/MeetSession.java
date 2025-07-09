package com.convofy.convofy.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.sql.Time;
import java.util.Date;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name="meet")
public class MeetSession
{
    @Id
    private String sessionid="";
    private String userid1="";
    private String userid2="";
    private String meetid="";
    private Date date=null;
    private Time start_time=null;
    private boolean status=false;
    private Time end_time=null;
    private boolean endedByClient = false;
}
