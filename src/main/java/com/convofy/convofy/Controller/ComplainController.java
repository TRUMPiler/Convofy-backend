package com.convofy.convofy.Controller;

import com.convofy.convofy.Entity.Complain;
import com.convofy.convofy.Repository.ComplainRepository;
import com.convofy.convofy.dto.ComplainDTO;
import com.convofy.convofy.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/complain")
public class ComplainController {

    @Autowired
    private ComplainRepository complainRepository;

    @PostMapping("/create")
    private ResponseEntity<Response<String>> createComplain(@RequestBody ComplainDTO complain) throws Exception {
        if(complain==null||complain.subject==null)
        {
            return ResponseEntity.badRequest().body(new Response<>(false,"Complain is incomplete",null));

        }
        Complain complainEntity = new Complain(complain);
        complainRepository.save(complainEntity);
        return ResponseEntity.ok(new Response<>(true,"Complain created",null));
    }

    @GetMapping("/get")
    private ResponseEntity<Response<List<Complain>>> getComplain(@RequestParam String id) throws Exception {
        List<Complain> complains=complainRepository.findAll();
        if(complains==null||complains.isEmpty()||complains.size()==0)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false,"Complains not found",null));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response<>(true,"Complains found",complains));
    }
}
