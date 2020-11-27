package org.virginiaso.file_upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
	InputStream getSubmissionTableAsInputStream() throws FileNotFoundException;
	boolean doesSubmissionTableExist();
	File getTempSubmissionTableFile() throws IOException;
	void transferTempSubmissionTableFile(File tempSubmissionTableFile) throws IOException;
	void transferUploadedFile(MultipartFile file, String eventDirName, String newFileName) throws IOException;
}
