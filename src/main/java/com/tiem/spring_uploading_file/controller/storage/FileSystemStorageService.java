package com.tiem.spring_uploading_file.controller.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

	private final Path rootLocation;

	@Autowired
	public FileSystemStorageService(StorageProperties properties) {

		if (properties.getLocation().trim().length() == 0) {
			throw new StorageException("File upload location cannot be empty!");
		}
		this.rootLocation = Paths.get(properties.getLocation());
	}

	@Override
	public void init() {
		try {
			Files.createDirectories(rootLocation);
		} catch (IOException e) {
			e.printStackTrace();
			throw new StorageException("Could not initialize storage", e);
		}
	}

	@Override
	public void stroe(MultipartFile file) {

		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file.");
			}
			Path destinationFile = this.rootLocation.resolve(Paths.get(file.getOriginalFilename())).normalize()
					.toAbsolutePath();

			if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
				throw new StorageException("Cannot store file outside current directory.");
			}

			try (InputStream input = file.getInputStream()) {
				Files.copy(input, destinationFile, StandardCopyOption.REPLACE_EXISTING);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new StorageException("Failed to store file", e);
		}
	}

	@Override
	public Stream<Path> loadAll() {
		try {
			return Files.walk(this.rootLocation, 1).filter(path -> !path.equals(this.rootLocation))
					.map(this.rootLocation::relativize);
		} catch (IOException e) {
			e.printStackTrace();
			throw new StorageException("Fail to read stored files", e);
		}
	}

	@Override
	public Path load(String filename) {
		return this.rootLocation.resolve(filename);
	}

	@Override
	public Resource loadAsResource(String fileName) {
		
		try {
			Path file = load(fileName);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new StorageFileNotFoundException("Could not read file : "+fileName);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new StorageFileNotFoundException("Could not read file : "+fileName, e);
		}
		
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(this.rootLocation.toFile());
	}

}
