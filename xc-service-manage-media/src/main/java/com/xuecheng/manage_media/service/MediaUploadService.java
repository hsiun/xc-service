package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;


/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-20 15:45
 */

@Service
public class MediaUploadService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MediaUploadService.class);
    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    private String uploadPath;


    /**
     * 获取文件绝对路径
     *
     * @param fileMd5
     * @param fileExt
     * @return
     */
    private String getFilePath(String fileMd5, String fileExt) {
        return uploadPath + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2)
                + "/" + fileMd5 + "." + fileExt;
    }

    /**
     * 获取文件相对路径
     *
     * @param fileMd5
     * @return
     */
    private String getFileFolderRelativePath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/";
    }

    /**
     * 文件所在目录
     *
     * @param fileMd5
     * @return
     */
    private String getFileFolderPath(String fileMd5) {
        return uploadPath + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/";
    }

    /**
     * 创建目录
     *
     * @param fileMd5
     * @return
     */
    private boolean createFileFold(String fileMd5) {
        //创建上传文件目录
        String fileFolderPath = getFileFolderPath(fileMd5);
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            return fileFolder.mkdirs();
        }
        return true;
    }

    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimeType, String fileExt) {
        //1.得到文件的路径
        String filePath = getFilePath(fileMd5, fileExt);
        File file = new File(filePath);
        //2.查询数据库文件是否存在
        Optional<MediaFile> meidiaFileOptional = mediaFileRepository.findById(fileMd5);
        if (file.exists() && meidiaFileOptional.isPresent()) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        boolean fileFold = createFileFold(fileMd5);
        if (!fileFold) {
            //文件存在直接返回
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 得到块文件所在目录
     *
     * @param fileMd5
     * @return
     */
    private String getChunkFileFolderPath(String fileMd5) {
        String fileChunkFolderPath = getFileFolderPath(fileMd5) + "/" + "chunks" + "/";
        return fileChunkFolderPath;
    }

    //检查块文件
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //得到块文件所在路径
        String chunkfileFolderPath = getChunkFileFolderPath(fileMd5);
        //块文件的文件名称以1,2,3..序号命名，没有扩展名
        File chunkFile = new File(chunkfileFolderPath + chunk);
        if (chunkFile.exists()) {
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, true);
        } else {
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, false);
        }
    }

    /**
     * 块文件上传
     *
     * @param file
     * @param fileMd5
     * @param chunk
     * @return
     */
    public ResponseResult uploadchunk(MultipartFile file, Integer fileMd5, String chunk) {
        if (file == null) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_ISNULL);
        }
        //创建块文件目录
        createChunkFileFolder(fileMd5.toString());
        //块文件
        File chunkfile = new File(getChunkFileFolderPath(fileMd5.toString()) + chunk);
        //上传的块文件
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(chunkfile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("upload chunk file fail:{}", e.getMessage());
            ExceptionCast.cast(MediaCode.CHUNK_FILE_UPLOAD_FAIL);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }


    /**
     * 创建块文件目录
     *
     * @param fileMd5
     * @return
     */
    private boolean createChunkFileFolder(String fileMd5) {
        //创建上传文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            //创建文件夹
            boolean mkdirs = chunkFileFolder.mkdirs();
            return mkdirs;
        }
        return true;
    }

    /**
     * 文件合并
     *
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String
            mimetype, String fileExt) {
        //获取块文件路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            chunkFileFolder.mkdirs();
        }
        //合并文件路径
        File mergeFile = new File(getFilePath(fileMd5, fileExt));
        //创建合并文件
        //合并文件存在先删除再创建
        if (mergeFile.exists()) {
            mergeFile.delete();
        }
        boolean newFile = false;
        try {
            newFile = mergeFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!newFile) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        List<File> chunkFiles = getChunkFiles(chunkFileFolder);
        mergeFile = mergeFile(mergeFile, chunkFiles);
        if (mergeFile == null) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        //检验生成的文件
        boolean checkResult = checkFileMd5(mergeFile, fileMd5);
        if (!checkResult) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //将文件信息保存到数据库
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        mediaFile.setFileOriginalName(fileName);
        //文件路径保存相对路径
        mediaFile.setFilePath(getFileFolderRelativePath(fileMd5));
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功 mediaFile.setFileStatus("301002");
        MediaFile save = mediaFileRepository.save(mediaFile);

        String mediaId = mediaFile.getFileId();
        //向MQ发送视频处理消息
        sendProcessVideoMsg(mediaId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;
    /**
     * 向mq发送消息
     * @param mediaId
     * @return
     */
    private ResponseResult sendProcessVideoMsg(String mediaId) {
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            return new ResponseResult(CommonCode.FAIL);
        }
        MediaFile mediaFile = optional.get();
        //发送视频处理消息
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("mediaId", mediaId);
        //发送的消息
        String msg = JSON.toJSONString(msgMap);
        try {
            this.rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK, routingkey_media_video, msg);
            LOGGER.info("send media process task msg:{}", msg);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("send media process task error,msg is:{},error:{}", msg, e.getMessage());
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }


    //校验文件的md5值
    private boolean checkFileMd5(File mergeFile, String md5) {
        if (mergeFile == null || StringUtils.isEmpty(md5)) {
            return false;
        }
        //进行md5校验
        FileInputStream mergeFileInputstream = null;
        try {
            mergeFileInputstream = new FileInputStream(mergeFile);
            //得到文件的md5
            String mergeFileMd5 = DigestUtils.md5Hex(mergeFileInputstream);
            //比较md5
            if (md5.equalsIgnoreCase(mergeFileMd5)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("checkFileMd5 error,file is:{},md5 is: {}", mergeFile.getAbsoluteFile(), md5);
        } finally {
            try {
                mergeFileInputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    //获取所有块文件
    private List<File> getChunkFiles(File chunkfileFolder) {
        //获取路径下的所有块文件
        File[] chunkFiles = chunkfileFolder.listFiles();
        //将文件数组转成list，并排序
        List<File> chunkFileList = new ArrayList<File>();
        chunkFileList.addAll(Arrays.asList(chunkFiles));
        //排序
        Collections.sort(chunkFileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())) {
                    return 1;
                }
                return -1;
            }
        });
        return chunkFileList;
    }

    /**
     * 合并文件
     *
     * @param mergeFile
     * @param chunkFiles
     * @return
     */
    private File mergeFile(File mergeFile, List<File> chunkFiles) {
        try {
            //创建写文件对象
            RandomAccessFile write = new RandomAccessFile(mergeFile, "rw");
            //遍历分块文件开始合并
            //读取文件缓冲区
            byte[] b = new byte[1024];
            for (File chunkFile : chunkFiles) {
                RandomAccessFile read = new RandomAccessFile(chunkFile, "r");
                int len = -1;
                //读取分块文件
                while ((len = read.read(b)) != -1) {
                    //向合并文件中写数据
                    write.write(b, 0, len);
                }
                read.close();
            }
            write.close();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("merge file error:{}", e.getMessage());
            return null;
        }
        return mergeFile;
    }

}
