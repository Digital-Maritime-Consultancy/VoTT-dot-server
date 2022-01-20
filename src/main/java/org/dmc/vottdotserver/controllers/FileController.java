package org.dmc.vottdotserver.controllers;

import org.dmc.vottdotserver.models.domain.File;
import org.dmc.vottdotserver.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@Validated
@RequestMapping("/file")
public class FileController {
    @Autowired
    FileRepository fileRepository;

    @GetMapping("")
    public ResponseEntity<List<File>> getAllMetadatas() {
        try {
            List<File> metadata = new ArrayList<File>();

            fileRepository.findAll().forEach(metadata::add);

            if (metadata.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(metadata, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getFileId(String fileName, String id) {
        return fileName + "_" + id;
    }

    @RequestMapping(value = "/{fileName}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getMetadataById(@PathVariable("fileName") String fileName, @Valid @NotBlank @RequestParam("uuid") String id) {
        Optional<File> metadatum = fileRepository.findByFileName(getFileId(fileName, id));

        if (metadatum.isPresent()) {
            return new ResponseEntity<>(metadatum.get().getData(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value = "/{fileName}", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<String> updateMetadata(@PathVariable("fileName") String fileName, @Valid @NotBlank @RequestParam("uuid") String id, @RequestBody String jsonBody) {
        Optional<File> metadatum = fileRepository.findByFileName(getFileId(fileName, id));

        if (jsonBody.isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.NOT_ACCEPTABLE);
        }
        if (metadatum.isPresent()) {
            File _metadatum = metadatum.get();
            _metadatum.setData(jsonBody);
            return new ResponseEntity<>(fileRepository.save(_metadatum).getData(), HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(fileRepository.save(new File(getFileId(fileName, id), jsonBody)).getData(), HttpStatus.ACCEPTED);
        }
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<HttpStatus> deleteMetadata(@PathVariable("fileName") String fileName, @Valid @NotBlank @RequestParam("uuid") String id) {
        try {
            Optional<File> metadatum = fileRepository.findByFileName(getFileId(fileName, id));

            if (metadatum.isPresent()) {
                File _metadatum = metadatum.get();
                fileRepository.deleteById(_metadatum.getId());
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}