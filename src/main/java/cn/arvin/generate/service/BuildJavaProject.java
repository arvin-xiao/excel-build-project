package cn.arvin.generate.service;

import cn.arvin.generate.entity.GenerateProperties;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 构建Java文件
 */
@Slf4j
public class BuildJavaProject {

    private static final String DB_URL = "jdbc:mysql://%s:3306/%s?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai";

    public static void build(GenerateProperties properties) {
        log.info("------开始构建Java项目------");
        String path = properties.getExportSqlDir();
        if (StringUtils.isBlank(path)) {
            throw new RuntimeException("请配置输出路径");
        }
        if (properties.getDataSource() == null) {
            throw new RuntimeException("请配置DataSource");
        }

        String url = String.format(DB_URL, properties.getDataSource().getHost(), properties.getDataSource().getDatabase());
        String user = properties.getDataSource().getUser();
        String password = properties.getDataSource().getPassword();
        FastAutoGenerator.create(url, user, password)
                .globalConfig(builder ->
                        builder.author(properties.getBuildConf().getAuthor())
                                .enableSwagger()
                                .dateType(DateType.ONLY_DATE)
                                .outputDir(path)
                )
                .dataSourceConfig(builder ->
                        builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) ->
                                typeRegistry.getColumnType(metaInfo)
                        ))
                .packageConfig(builder ->
                        builder.parent(properties.getBuildConf().getPackageName())
                                //.moduleName("system")
                                .pathInfo(Collections.singletonMap(OutputFile.xml, path))
                )
                .strategyConfig(builder -> {
                    String[] includeTables = properties.getBuildConf().getIncludeTable().split(",");
                    String[] excludeTables = properties.getBuildConf().getExcludeTable().split(",");
                    List<String> includes = Stream.of(includeTables).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                    List<String> excludes = Stream.of(excludeTables).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                    if (!includes.isEmpty()) {
                        builder.addInclude(includes);
                    }
                    if (!excludes.isEmpty()) {
                        builder.addExclude(excludes);
                    }
                    String tablePrefix = properties.getBuildConf().getTablePrefix();
                    if (StringUtils.isNotBlank(tablePrefix)) {
                        builder.addTablePrefix(tablePrefix);
                    }
                    builder.enableCapitalMode()
                            .controllerBuilder().enableFileOverride().enableRestStyle()
                            .serviceBuilder().enableFileOverride().convertServiceFileName(s -> String.format("%sService", s))
                            .mapperBuilder().enableFileOverride().enableBaseResultMap().enableBaseColumnList()
                            .entityBuilder().enableFileOverride().enableLombok().enableTableFieldAnnotation();
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
        log.info("------构建Java项目完成------");
    }

}
