package edu.asu.diging.gilesecosystem.web.service.core.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.asu.diging.gilesecosystem.util.exceptions.UnstorableObjectException;
import edu.asu.diging.gilesecosystem.util.properties.IPropertiesManager;
import edu.asu.diging.gilesecosystem.web.domain.IUpload;
import edu.asu.diging.gilesecosystem.web.domain.impl.Upload;
import edu.asu.diging.gilesecosystem.web.files.IUploadDatabaseClient;
import edu.asu.diging.gilesecosystem.web.service.core.ITransactionalUploadService;
import edu.asu.diging.gilesecosystem.web.service.properties.Properties;

@Service
@Transactional("transactionManager")
public class TransactionalUploadService implements ITransactionalUploadService {

    @Autowired
    private IPropertiesManager propertyManager;

    @Autowired
    private IUploadDatabaseClient uploadDatabaseClient;

    /* (non-Javadoc)
     * @see edu.asu.diging.gilesecosystem.web.service.core.impl.ITransactionalUploadService#generateUploadId()
     */
    @Override
    public String generateUploadId() {
        return uploadDatabaseClient.generateId();
    }
    
    /* (non-Javadoc)
     * @see edu.asu.diging.gilesecosystem.web.service.core.impl.ITransactionalUploadService#getUpload(java.lang.String)
     */
    @Override
    public IUpload getUpload(String id) {
        return uploadDatabaseClient.getUpload(id);
    }
    
    /* (non-Javadoc)
     * @see edu.asu.diging.gilesecosystem.web.service.core.impl.ITransactionalUploadService#getUploadByProgressId(java.lang.String)
     */
    @Override
    public IUpload getUploadByProgressId(String progressId) {
        return uploadDatabaseClient.getUploadsByProgressId(progressId);
    }
    
    /* (non-Javadoc)
     * @see edu.asu.diging.gilesecosystem.web.service.core.impl.ITransactionalUploadService#saveUpload(edu.asu.diging.gilesecosystem.web.core.IUpload)
     */
    @Override
    public void saveUpload(IUpload upload) throws UnstorableObjectException {
        uploadDatabaseClient.saveUpload(upload);
    }
 
    @Override
    public List<IUpload> getUploadsOfUser(String username, int page, int pageSize, String sortBy, int sortDirection) {
        int defaultPageSize = new Integer(propertyManager.getProperty(Properties.DEFAULT_PAGE_SIZE));
        if (pageSize == -1) {
            pageSize = defaultPageSize;
        }
        if (page < 1) {
            page = 1;
        }
        int pageCount = getUploadsOfUserPageCount(username);
        pageCount = pageCount > 0 ? pageCount : 1;
        if (page > pageCount) {
            page = pageCount;
        }
        return uploadDatabaseClient.getUploadsForUser(username, page, pageSize, sortBy, sortDirection);
    }
    
    @Override
    public int getUploadsOfUserCount(String username) {
        List<IUpload> uploads = uploadDatabaseClient.getUploadsForUser(username);
        return uploads.size();
    }
    
    @Override
    public int getUploadsOfUserPageCount(String username) {
        int defaultPageSize = new Integer(propertyManager.getProperty(Properties.DEFAULT_PAGE_SIZE));
        int totalUploads = getUploadsOfUserCount(username);
        return (int) Math.ceil(new Double(totalUploads) / new Double(defaultPageSize));
    }
    
    @Override
    public IUpload createUpload(String username, String uploadId,
            String uploadDate, String uploadProgressId) {
        IUpload upload = new Upload(uploadId);
        upload.setCreatedDate(uploadDate);
        upload.setUsername(username);
        upload.setUploadProgressId(uploadProgressId);
        return upload;
    }

}