package com.convofy.convofy.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.sql.Time;
import java.util.Date;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
@Table("meet")
public class MeetSession
{
    @PrimaryKey("sessionid")
    private String sessionid="";
    private String userid1="";
    private String userid2="";
    private String meetid="";
    private Date date=null;
    private Time time=null;
//    private boolean status=false;
}
