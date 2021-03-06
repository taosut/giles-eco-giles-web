package edu.asu.diging.gilesecosystem.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.asu.diging.gilesecosystem.web.aspects.access.annotations.AccountCheck;
import edu.asu.diging.gilesecosystem.web.aspects.access.annotations.UploadIdAccessCheck;
import edu.asu.diging.gilesecosystem.web.controllers.pages.DocumentPageBean;
import edu.asu.diging.gilesecosystem.web.controllers.pages.FilePageBean;
import edu.asu.diging.gilesecosystem.web.controllers.util.StatusBadgeHelper;
import edu.asu.diging.gilesecosystem.web.domain.IDocument;
import edu.asu.diging.gilesecosystem.web.domain.IFile;
import edu.asu.diging.gilesecosystem.web.domain.IProcessingRequest;
import edu.asu.diging.gilesecosystem.web.domain.IUpload;
import edu.asu.diging.gilesecosystem.web.exceptions.GilesMappingException;
import edu.asu.diging.gilesecosystem.web.files.IFilesManager;
import edu.asu.diging.gilesecosystem.web.files.IProcessingRequestsDatabaseClient;
import edu.asu.diging.gilesecosystem.web.service.IGilesMappingService;
import edu.asu.diging.gilesecosystem.web.service.IMetadataUrlService;
import edu.asu.diging.gilesecosystem.web.service.core.ITransactionalFileService;
import edu.asu.diging.gilesecosystem.web.service.core.ITransactionalUploadService;
import edu.asu.diging.gilesecosystem.web.service.impl.GilesMappingService;

@Controller
public class ViewUploadController {

    @Autowired
    private IFilesManager filesManager;
    
    @Autowired
    private ITransactionalUploadService uploadService;
    
    @Autowired
    private ITransactionalFileService fileService;
    
    @Autowired
    private IMetadataUrlService metadataService;
    
    @Autowired
    private StatusBadgeHelper statusHelper;
    
    @Autowired
    private IProcessingRequestsDatabaseClient procReqDbClient;

    
    @AccountCheck
    @UploadIdAccessCheck
    @RequestMapping(value = {"/uploads/{uploadId}"})
    public String showUploadPage(@PathVariable("uploadId") String uploadId,
            Model model, Locale locale) throws GilesMappingException {
        IUpload upload = uploadService.getUpload(uploadId);
        List<IDocument> docs = filesManager.getDocumentsByUploadId(uploadId);
        
        IGilesMappingService<IFile, FilePageBean> fileMappingService = new GilesMappingService<>();
        IGilesMappingService<IDocument, DocumentPageBean> docMappingService = new GilesMappingService<>();
        
        List<DocumentPageBean> mappedDocs = new ArrayList<DocumentPageBean>();
        for (IDocument doc : docs) {
            DocumentPageBean docBean = docMappingService.convertToT2(doc, new DocumentPageBean());
            mappedDocs.add(docBean);
            docBean.setFiles(new ArrayList<>());
            docBean.setTextFiles(new ArrayList<>());
            docBean.setMetadataUrl(metadataService.getDocumentLink(doc));
            
            IFile origFile = fileService.getFileById(doc.getUploadedFileId());  
            if (origFile != null) {
                FilePageBean bean = fileMappingService.convertToT2(origFile, new FilePageBean());
                bean.setMetadataLink(metadataService.getFileLink(origFile));
                docBean.setUploadedFile(bean);
            }
            
            IFile textFile = fileService.getFileById(doc.getExtractedTextFileId());
            if (textFile != null) {
                FilePageBean bean = fileMappingService.convertToT2(textFile, new FilePageBean());
                bean.setMetadataLink(metadataService.getFileLink(textFile));
                docBean.setExtractedTextFile(bean);
            }
            
            for (IFile file : doc.getFiles()) {
                FilePageBean bean = fileMappingService.convertToT2(file, new FilePageBean());
                bean.setMetadataLink(metadataService.getFileLink(file));
                docBean.getFiles().add(bean);
            }
            if (doc.getTextFileIds() != null) {
                for (String fileId : doc.getTextFileIds()) {
                    IFile file = fileService.getFileById(fileId);
                    FilePageBean bean = fileMappingService.convertToT2(file, new FilePageBean());
                    bean.setMetadataLink(metadataService.getFileLink(file));
                    docBean.getTextFiles().add(bean);
                }
            }
            
            List<IProcessingRequest> procRequests = procReqDbClient.getRequestByDocumentId(doc.getId());
            statusHelper.createBadges(docBean, procRequests);
        }
        
        model.addAttribute("upload", upload);
        model.addAttribute("docs", mappedDocs);

        return "uploads/upload";
    }
}
