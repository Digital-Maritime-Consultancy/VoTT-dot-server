package org.dmc.vottdotserver.controller;

import java.io.IOException;
import java.nio.file.Files;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Value("classpath:jsons/data.json")
    Resource resourceFile;

    @RequestMapping(value = "/{name}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String getImages(HttpServletResponse response, @PathVariable("name") String name) throws IOException {
        String actual = Files.readString(resourceFile.getFile().toPath());
        return actual;
    }

    @RequestMapping(value = "/{name}/{fileName}", method = RequestMethod.GET,
            produces = MediaType.IMAGE_JPEG_VALUE)

    public void getImage(HttpServletResponse response, @PathVariable("name") String name, @PathVariable("fileName") String fileName) throws IOException {

        var imgFile = new ClassPathResource("images/"+fileName);

        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(imgFile.getInputStream(), response.getOutputStream());
    }
}