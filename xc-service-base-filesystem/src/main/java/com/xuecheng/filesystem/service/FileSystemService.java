package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.Map;

/**
 * @author growdane@gmail.com
 * @date 2020-02-14 15:00
 */

@Service
public class FileSystemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystem.class);

    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    int connectTimeout;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    int networkTimeout;
    @Value("${xuecheng.fastdfs.charset}")
    String charset;
    @Value("${xuecheng.fastdfs.tracker_servers}")
    String trackerServers;
    @Autowired
    FileSystemRepository fileSystemRepository;

    /**
     * 初始化文件系统
     */
    public void initFdfsConfig() {
        try {
            ClientGlobal.initByTrackers(trackerServers);
            ClientGlobal.setG_charset(charset);
            ClientGlobal.setG_connect_timeout(connectTimeout);
            ClientGlobal.setG_network_timeout(networkTimeout);

        } catch (Exception e) {
            e.printStackTrace();
            ExceptionCast.cast(FileSystemCode.FS_INITFILE_ERROR);
        }
    }

    /**
     * 上传文件
     * @param file
     * @param filetag
     * @param businesskey
     * @param metadata
     * @return
     */
    public UploadFileResult upload(MultipartFile file,
                                   String filetag,
                                   String businesskey,
                                   String metadata){
        if (file == null) {
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }
        String fileId = uploadFdfs(file);
        FileSystem fileSystem = new FileSystem();
        fileSystem.setFileId(fileId);
        fileSystem.setFiletag(filetag);
        fileSystem.setBusinesskey(businesskey);
        fileSystem.setFilePath(fileId);
        if (StringUtils.isNotEmpty(metadata)) {
            Map map = JSON.parseObject(metadata, Map.class);
            fileSystem.setMetadata(map);
        }
        fileSystem.setFileName(file.getOriginalFilename());
        fileSystem.setFileSize(file.getSize());
        fileSystem.setFileType(file.getContentType());
        fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS, fileSystem);
    }

    private String uploadFdfs(MultipartFile file) {

        try {
            initFdfsConfig();
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);

            byte[] bytes = file.getBytes();
            String filename = file.getOriginalFilename();
            String extname = filename.substring(filename.lastIndexOf(".") + 1);
            String fileId = storageClient1.upload_file1(bytes, extname, null);
            return fileId;

        } catch (Exception e) {
            e.printStackTrace();
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }
        return null;

    }
}
