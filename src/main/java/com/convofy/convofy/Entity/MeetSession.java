package com.convofy.convofy.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Table;

import java.sql.Time;
import java.util.Date;

@AllArgsConstructor
@Setter
@Getter
@Table("meet")
public class MeetSession
{
    private String sessionid="";
    private String userid1="";
    private String userid2="";
    private String meetid="";
    private Date date=null;
    private Time time=null;
    private boolean status=false;
}
