package cn.arvin.generate.service;

import cn.arvin.generate.entity.GenerateProperties;
import cn.hutool.core.util.IdUtil;
import cn.smallbun.screw.core.Configuration;
import cn.smallbun.screw.core.engine.EngineConfig;
import cn.smallbun.screw.core.engine.EngineFileType;
import cn.smallbun.screw.core.engine.EngineTemplateType;
import cn.smallbun.screw.core.execute.DocumentationExecute;
import cn.smallbun.screw.core.process.ProcessConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;

/**
 * @author: arvin.xiao
 * @date: 2023/11/23
 * @description: 导出数据库文档
 * @version: 1.0
 */
@Slf4j
public class DataBaseDocExport {

    private static final String DOC_FILE_NAME = "数据库文档";
    private static final String DOC_VERSION = "1.0.0";
    private static final String DOC_DESCRIPTION = "文档描述";
    private static final String DB_URL = "jdbc:mysql://%s:3306/%s?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai";


    public static void outputDatabaseDoc(GenerateProperties properties) {
        log.info("------开始输出数据库文档------");
        String docFileName = DOC_FILE_NAME + "_" + IdUtil.fastSimpleUUID();
        String filePath = doExportFile(EngineFileType.MD, docFileName, properties);
        log.info("------输出数据库文档完成,路径为:{}------", filePath);
    }

    /**
     * 输出文件，返回文件路径
     *
     * @param fileOutputType 文件类型
     * @param fileName       文件名, 无需 ".docx" 等文件后缀
     * @return 生成的文件所在路径
     */
    private static String doExportFile(EngineFileType fileOutputType, String fileName, GenerateProperties properties) {
        String dir = properties.getExportSqlDir();
        try (HikariDataSource dataSource = buildDataSource(properties)) {
            // 创建 screw 的配置
            Configuration config = Configuration.builder()
                    .version(DOC_VERSION)  // 版本
                    .description(DOC_DESCRIPTION) // 描述
                    .dataSource(dataSource) // 数据源
                    .engineConfig(buildEngineConfig(fileOutputType, dir, fileName)) // 引擎配置
                    .produceConfig(buildProcessConfig()) // 处理配置
                    .build();

            // 执行 screw，生成数据库文档
            new DocumentationExecute(config).execute();
            return dir + File.separator + fileName + fileOutputType.getFileSuffix();
        }
    }

    /**
     * 创建 screw 的引擎配置
     */
    private static EngineConfig buildEngineConfig(EngineFileType fileOutputType, String dir, String docFileName) {
        return EngineConfig.builder()
                .fileOutputDir(dir) // 生成文件路径
                .openOutputDir(false) // 打开目录
                .fileType(fileOutputType) // 文件类型
                .produceType(EngineTemplateType.velocity) // 文件类型
                .fileName(docFileName) // 自定义文件名称
                .build();
    }

    /**
     * 创建 screw 的处理配置，一般可忽略
     * 指定生成逻辑、当存在指定表、指定表前缀、指定表后缀时，将生成指定表，其余表不生成、并跳过忽略表配置
     */
    private static ProcessConfig buildProcessConfig() {
        // 忽略表前缀
        return ProcessConfig.builder().ignoreTablePrefix(Arrays.asList("QRTZ_", "ACT_", "FLW_")).build();
    }

    private static HikariDataSource buildDataSource(GenerateProperties properties) {
        String url = String.format(DB_URL, properties.getDataSource().getHost(), properties.getDataSource().getDatabase());
        String user = properties.getDataSource().getUser();
        String password = properties.getDataSource().getPassword();
        // 创建 HikariConfig 配置类
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.addDataSourceProperty("useInformationSchema", "true"); // 设置可以获取 tables remarks 信息
        // 创建数据源
        return new HikariDataSource(hikariConfig);
    }

}
