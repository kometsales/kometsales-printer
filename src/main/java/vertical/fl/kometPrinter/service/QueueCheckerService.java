package vertical.fl.kometPrinter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vertical.fl.kometPrinter.control.QueueController;

/**
 * Service class for SQS comunication.
 * 
 * @author Hector Mosquera Turner
 * @since 11/10/2012
 * @version 1.0
 */
@Service
public class QueueCheckerService {

	@Autowired
	QueueController aQueueController;

	// private ExecutorService pool;

	public void checkQueue() {
		// 1. call queueControll.readQueue
		aQueueController.readQueue();
	}

}
