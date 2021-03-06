package edu.asu.diging.gilesecosystem.web.service;

import java.io.IOException;
import java.net.URL;

import edu.asu.diging.gilesecosystem.web.domain.IFile;
import edu.asu.diging.gilesecosystem.web.files.IFileStorageManager;

public interface IFileContentHelper {

    public abstract byte[] getFileContent(IFile file, IFileStorageManager storageManager);

    public abstract byte[] getFileContentFromUrl(URL url) throws IOException;

}