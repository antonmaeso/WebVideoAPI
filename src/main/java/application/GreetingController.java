
package application;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties.Template;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class GreetingController {
	
	@Autowired
	SimpMessagingTemplate template;
	public Random rnd = new Random();

	@Scheduled(fixedDelay = 20000L)
	public void greeting() throws Exception {
		template.convertAndSend("/topic/greetings", rnd.nextInt());
	}

}
