package edu.asu.diging.gilesecosystem.web.service.processing.impl;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.asu.diging.gilesecosystem.requests.ICompletionNotificationRequest;
import edu.asu.diging.gilesecosystem.requests.impl.CompletionNotificationRequest;
import edu.asu.diging.gilesecosystem.util.exceptions.UnstorableObjectException;
import edu.asu.diging.gilesecosystem.util.properties.IPropertiesManager;
import edu.asu.diging.gilesecosystem.web.core.IDocument;
import edu.asu.diging.gilesecosystem.web.core.ITask;
import edu.asu.diging.gilesecosystem.web.core.impl.Task;
import edu.asu.diging.gilesecosystem.web.files.IFilesManager;
import edu.asu.diging.gilesecosystem.web.service.processing.RequestProcessor;
import edu.asu.diging.gilesecosystem.web.service.properties.Properties;

@Service
public class CompletionNotificationProcessor implements RequestProcessor<ICompletionNotificationRequest> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private IPropertiesManager propertiesManager;
    
    @Autowired
    private IFilesManager filesManager;
   
    @Override
    public String getProcessedTopic() {
        return propertiesManager.getProperty(Properties.KAFKA_TOPIC_COMPLETION_NOTIFICATION_REQUEST);
    }

    @Override
    public void processRequest(ICompletionNotificationRequest request) {
        IDocument document = filesManager.getDocument(request.getDocumentId());
        ITask task = new Task();
        task.setFileId(request.getFileId());
        task.setStatus(request.getStatus());
        task.setTaskHandlerId(request.getNotifier());
        
        if (document.getTasks() == null) {
            document.setTasks(new ArrayList<ITask>());
        }
        
        document.getTasks().add(task);
        try {
            filesManager.saveDocument(document);
        } catch (UnstorableObjectException e) {
            // should never happen
            logger.error("Could not store document.", e);
        }
     }

    @Override
    public Class<? extends ICompletionNotificationRequest> getRequestClass() {
        return CompletionNotificationRequest.class;
    }

}