package cn.arvin.generate.service;

import cn.arvin.generate.entity.GenerateProperties;
import cn.hutool.setting.dialect.Props;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 读取配置文件
 */
@Slf4j
public class ReadConfiguration {

    private File root;

    /**
     * 要读取的配置文件名称
     */
    public static final String CON_NAME = "config.properties";

    private GenerateProperties generateProperties;

    public ReadConfiguration() {
        String jarLocal = new String(getClass().getProtectionDomain().getCodeSource().getLocation().getPath().getBytes(), StandardCharsets.UTF_8);
        /*this.root = new File(jarLocal).getParentFile();*/
        this.root = new File(jarLocal);
        log.info("---初始化获得路径--" + this.root.getAbsolutePath());

        Props props = new Props(new File(root, CON_NAME), StandardCharsets.UTF_8);

        generateProperties = new GenerateProperties();
        generateProperties.setExcelPath(jarLocal + props.getStr("excelPath"));
        generateProperties.setExportSqlDir(props.getStr("exportSqlDir"));
        generateProperties.setAutoRunScript(props.getBool("autoRunScript", false));
        GenerateProperties.DataSource dataSource = new GenerateProperties.DataSource();
        dataSource.setHost(props.getStr("dataSource.host"));
        dataSource.setPassword(props.getStr("dataSource.password"));
        dataSource.setUser(props.getStr("dataSource.user"));
        dataSource.setDatabase(props.getStr("dataSource.database"));
        dataSource.setPort(props.getInt("dataSource.port"));
        generateProperties.setDataSource(dataSource);

        generateProperties.setOutputScriptFile(props.getBool("outputScriptFile", true));
        generateProperties.setBuildJava(props.getBool("buildJava", false));
        GenerateProperties.BuildConf buildConf = new GenerateProperties.BuildConf();
        buildConf.setAuthor(props.getStr("buildConf.author"));
        buildConf.setPackageName(props.getStr("buildConf.packageName"));
        buildConf.setExcludeTable(props.getStr("buildConf.excludeTable"));
        buildConf.setIncludeTable(props.getStr("buildConf.includeTable"));
        generateProperties.setBuildConf(buildConf);
    }

    public GenerateProperties getGenerateProperties() {
        return generateProperties;
    }
}
