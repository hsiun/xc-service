package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 监听视频处理队列，并进行视频处理
 *
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-22 15:23
 */

public class MediaProcessTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaProcessTask.class);

    @Value("${xc-service-manage-media.ffmpeg-path}")
    private String ffmpegPath;
    @Value("${xc-service-manage-media.video-location}")
    private String videoLocation;
    @Autowired
    private MediaFileRepository mediaFileRepository;
    /**
     * 消费者并发数量
     */
    @Value("${xc-service-manage-media.mq.default-concurrent}")
    public static final int DEFAULT_CONCURRENT = 10;

    @Bean("customContainerFactory")
    public SimpleRabbitListenerContainerFactory containerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConcurrentConsumers(DEFAULT_CONCURRENT);
        factory.setMaxConcurrentConsumers(DEFAULT_CONCURRENT);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",
            containerFactory="customContainerFactory")
    public void receiveMediaProcessTask(String msg) throws IOException {
        Map msgMap = JSON.parseObject(msg, Map.class);
        LOGGER.info("receive media process task msg :{} ", msgMap);
        //解析消息
        //媒资文件id
        String mediaId = (String) msgMap.get("mediaId");
        //获取媒资文件信息
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            return;
        }
        MediaFile mediaFile = optional.get();
        String fileType = mediaFile.getFileType();
        if (fileType == null || !fileType.equals("avi")) {
            //目前只处理avi文件
            mediaFile.setProcessStatus("303004");
            //处理状态为无需处理
            mediaFileRepository.save(mediaFile);
            return;
        } else {
            mediaFile.setProcessStatus("303001");
            //处理状态为未处理
            mediaFileRepository.save(mediaFile);
        }
        //先转化为mp4
        String video_path = videoLocation + mediaFile.getFilePath() + mediaFile.getFileName();
        String mp4_name = mediaFile.getFileId() + ".mp4";
        String mp4folder_path = videoLocation + mediaFile.getFilePath();
        Mp4VideoUtil videoUtil = new
                Mp4VideoUtil(ffmpegPath, video_path, mp4_name, mp4folder_path);
        String result = videoUtil.generateMp4();
        if (getResult(mediaFile, result)) return;
        //再转化为hls
        video_path = videoLocation + mediaFile.getFilePath() + mp4_name;
        //此地址为mp4的地址
        String m3u8_name = mediaFile.getFileId() + ".m3u8";
        String m3u8folder_path = videoLocation + mediaFile.getFilePath() + "hls/";
        HlsVideoUtil hlsVideoUtil = new
                HlsVideoUtil(ffmpegPath, video_path, m3u8_name, m3u8folder_path);
        result = hlsVideoUtil.generateM3u8();
        if (getResult(mediaFile, result)) return;
        //获取m3u8列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        //更新处理状态为成功
        mediaFile.setProcessStatus("303002");
        //处理状态为处理成功
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        //m3u8文件url
        mediaFile.setFileUrl(mediaFile.getFilePath() + "hls/" + m3u8_name);
        mediaFileRepository.save(mediaFile);
    }

    private boolean getResult(MediaFile mediaFile, String result) {
        if (result == null || !result.equals("success")) {
            //操作失败写入处理日志
            mediaFile.setProcessStatus("303003");
            //处理状态为处理失败
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return true;
        }
        return false;
    }
}
