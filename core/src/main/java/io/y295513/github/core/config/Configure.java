package io.y295513.github.core.config;

import lombok.Data;

import java.util.Map;

/**
 * @author yang.yue
 */
@Data
public class Configure {
    private Integer port = 9999;
    private String spyJarPath = "D:\\myCode\\ArthasX\\spy\\target\\spy-1.0-SNAPSHOT.jar";
    private String spyApiClassNAME = "java.arthasx.SpyAPI";


    public Configure(Map<String, String> args) {
        // todo
    }
}
