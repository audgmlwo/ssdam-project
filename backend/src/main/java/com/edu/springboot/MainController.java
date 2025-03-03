package com.edu.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.edu.springboot.commboard.IBoardService;
import com.edu.springboot.member.IMemberService;

@Controller
public class MainController {

	@Autowired
	IBoardService dao; 


	@RequestMapping("/")
	public String home() {
		return "home";
	}
	
	@GetMapping("/boardList.do")
	public String BoardList(){
	    return "boardList";
	}

	@GetMapping("/boardView.do")
	public String boardView(){
	    return "boardView";
	}
	
	 @GetMapping("/boardWrite.do")
	 public String boardWrite() {
	    return "boardWrite";  
	}
	 
	 @GetMapping("/boardEdit.do")
	 public String boardEdit() {
	    return "boardEdit";  
	}
	
}
