
package webinterface.application.controllers;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class FacePostionController {
	
	@Autowired
	SimpMessagingTemplate template;
	public Random rnd = new Random();

	@Scheduled(fixedDelay = 2000L)
	public void greeting() throws Exception {
		template.convertAndSend("/headposition", rnd.nextInt());
	}

}
