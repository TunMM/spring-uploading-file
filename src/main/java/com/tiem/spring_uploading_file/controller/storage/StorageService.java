package com.tiem.spring_uploading_file.controller.storage;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
	
	void init();
	
	void stroe(MultipartFile file);
	
	Stream<Path> loadAll();
	
	Path load(String filename);
	
	Resource loadAsResource(String fileName);
	
	void deleteAll();

}
