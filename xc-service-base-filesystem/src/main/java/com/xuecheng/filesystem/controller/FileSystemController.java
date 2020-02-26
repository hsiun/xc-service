package com.xuecheng.filesystem.controller;

import com.xuecheng.api.filesystem.FileSystemControllerApi;
import com.xuecheng.filesystem.service.FileSystemService;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author growdane@gmail.com
 * @date 2020-02-14 15:26
 */

@RestController
@RequestMapping("/filesystem")
public class FileSystemController implements FileSystemControllerApi {
    @Autowired
    private FileSystemService fileSystemService;

    @Override
    @RequestMapping("/upload")
    public UploadFileResult upload(@RequestParam("file") MultipartFile multipartFile,
                                   @RequestParam("filetag") String filetag,
                                   @RequestParam(value = "businesskey", required = false) String businesskey,
                                   @RequestParam(value = "metedata", required = false) String metedata) {
        return fileSystemService.upload(multipartFile, filetag, businesskey, metedata);
    }
}
