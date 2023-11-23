package cn.arvin.generate;

import cn.arvin.generate.entity.GenerateProperties;
import cn.arvin.generate.service.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) {
        ReadConfiguration readConfiguration = new ReadConfiguration();
        GenerateProperties properties = readConfiguration.getGenerateProperties();

        ReadExcel readExcel = new ReadExcel(properties);
        if (properties.isOutputScriptFile()) {
            readExcel.outputFile();
            log.info("SQL文件写出成功");
        }

        if (properties.isAutoRunScript()) {
            DataBaseAutoImport autoImport = new DataBaseAutoImport(properties);
            autoImport.execute(readExcel.getScriptList());
            log.info("创建数据表成功");
        }

        if (properties.isOutputDatabaseDoc()) {
            DataBaseDocExport.outputDatabaseDoc(properties);
            log.info("输出数据库文档成功");
        }

        if (properties.isBuildJava()) {
            BuildJavaProject.build(properties);
            log.info("构建java项目成功");
        }

        log.info("程序结束");
    }

}
