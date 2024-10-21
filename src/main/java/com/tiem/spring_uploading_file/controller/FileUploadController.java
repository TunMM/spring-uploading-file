package com.tiem.spring_uploading_file.controller;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tiem.spring_uploading_file.controller.storage.StorageFileNotFoundException;
import com.tiem.spring_uploading_file.controller.storage.StorageService;

@Controller
public class FileUploadController {

	@Autowired
	private StorageService storageService;

//	public FileUploadController(@Autowired StorageService storageService) {
//		this.storageService = storageService;
//	}

	@GetMapping("/")
	public String listUploadFiles(Model model) {
		model.addAttribute("files",
				storageService.loadAll()
						.map(path -> MvcUriComponentsBuilder
								.fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString())
								.build().toUri().toString())
						.collect(Collectors.toList()));
		return "uploadForm";
	}

	@GetMapping("/files/{fileName:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {

		Resource file = storageService.loadAsResource(fileName);
		
		if(file == null) {
			return ResponseEntity.notFound().build();
		}
		
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "\"attachment; filename=\""+file.getFilename()+"\"").body(file);
	}

	@PostMapping("/")
	public String handleFileUpload(MultipartFile file, RedirectAttributes redirectAttributes) {
		storageService.stroe(file);
		redirectAttributes.addFlashAttribute("message", "You successfully uploaded "+file.getOriginalFilename()+"!");
		return "redirect:/";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exception) {
		return ResponseEntity.notFound().build();
	}
}
