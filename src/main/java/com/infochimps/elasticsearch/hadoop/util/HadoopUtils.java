package com.infochimps.elasticsearch.hadoop.util;

import java.io.File;

import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;

public class HadoopUtils {
    
    /**
       Upload a local file to the cluster
     */
    public static void uploadLocalFile(Path localsrc, Path hdfsdest, Configuration conf) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        if (fs.exists(hdfsdest) && fs.getFileStatus(hdfsdest).isDir()) {
            fs.delete(hdfsdest, true);
        } 
        fs.copyFromLocalFile(false, true, localsrc, hdfsdest);            
    }

    
    /**
       Upload a local file to the cluster, if it's newer or nonexistent
     */
    public static void uploadLocalFileIfChanged(Path localsrc, Path hdfsdest, Configuration conf) throws IOException {
        long l_time = new File(localsrc.toUri()).lastModified();
        try {
            long h_time = FileSystem.get(conf).getFileStatus(hdfsdest).getModificationTime();
            if ( l_time > h_time ) {
                uploadLocalFile(localsrc, hdfsdest, conf);
            }
        }
        catch (FileNotFoundException e) {
            uploadLocalFile(localsrc, hdfsdest, conf);
        }
    }


    /**
       Fetches a file with the basename specified from the distributed cache. Returns null if no file is found
     */
    public static String fetchFileFromCache(String basename, Configuration conf) throws IOException {
        Path[] cacheFiles = DistributedCache.getLocalCacheFiles(conf);
        if (cacheFiles != null && cacheFiles.length > 0) {
            for (Path cacheFile : cacheFiles) {
                if (cacheFile.getName().equals(basename)) {
                    return cacheFile.toString();
                }
            }
        }
        System.out.println("[HadoopUtils: the file is not in distributed cache] " + basename);
        return null;
    }

    /**
       Fetches a file with the basename specified from the distributed cache. Returns null if no file is found
     */
    public static String fetchArchiveFromCache(String basename, Configuration conf) throws IOException {
        Path[] cacheArchives = DistributedCache.getLocalCacheArchives(conf);
        if (cacheArchives != null && cacheArchives.length > 0) {
            for (Path cacheArchive : cacheArchives) {
                if (cacheArchive.getName().equals(basename)) {
                    return cacheArchive.toString();
                }
            }
        }
        return null;
    }

    /**
       Takes a path on the hdfs and ships it in the distributed cache if it is not already in the distributed cache
     */
    public static void shipFileIfNotShipped(Path hdfsPath, Configuration conf) throws IOException {
        if (fetchFileFromCache(hdfsPath.getName(), conf) == null) {
            try {
                DistributedCache.addCacheFile(hdfsPath.toUri(), conf);
                System.out.println("[HadoopUtil add file to distributed cache] "+ hdfsPath.toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }        
    }

        /**
       Takes a path on the hdfs and ships it in the distributed cache if it is not already in the distributed cache
     */
    public static void shipArchiveIfNotShipped(Path hdfsPath, Configuration conf) throws IOException {
        if (fetchArchiveFromCache(hdfsPath.getName(), conf) == null) {
            try {
                DistributedCache.addCacheArchive(hdfsPath.toUri(), conf);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }        
    }
}
