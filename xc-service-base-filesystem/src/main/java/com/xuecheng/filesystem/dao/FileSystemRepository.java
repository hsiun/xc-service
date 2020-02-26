package com.xuecheng.filesystem.dao;

import com.xuecheng.framework.domain.filesystem.FileSystem;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author growdane@gmail.com
 * @date 2020-02-14 14:47
 */

public interface FileSystemRepository extends MongoRepository<FileSystem, String> {
}
