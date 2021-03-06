package edu.asu.diging.gilesecosystem.web.service.processing;

import edu.asu.diging.gilesecosystem.web.domain.IFile;

public interface IDistributedStorageManager {

    public abstract String getFileUrl(IFile file);

    public abstract byte[] getFileContent(IFile file);

}