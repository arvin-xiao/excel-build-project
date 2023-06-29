package cn.arvin.generate.service;

import cn.arvin.generate.entity.GenerateProperties;
import cn.hutool.db.Session;
import com.mysql.cj.jdbc.MysqlDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * 数据库自动创建表 sql执行
 */
@Slf4j
public class DataBaseAutoImport {

    private GenerateProperties properties;

    private Session session;

    public DataBaseAutoImport(GenerateProperties properties) {
        this.properties = properties;
        GenerateProperties.DataSource dataSource = properties.getDataSource();
        if (StringUtils.isBlank(dataSource.getDatabase()) || StringUtils.isBlank(dataSource.getHost())
                || StringUtils.isBlank(dataSource.getPassword()) || StringUtils.isBlank(dataSource.getUser())
                || dataSource.getPort() == null) {
            throw new RuntimeException("数据源参数不完整");
        }

        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setServerName(properties.getDataSource().getHost());
        mysqlDataSource.setPort(properties.getDataSource().getPort());
        mysqlDataSource.setDatabaseName(properties.getDataSource().getDatabase());
        mysqlDataSource.setUser(properties.getDataSource().getUser());
        mysqlDataSource.setPassword(properties.getDataSource().getPassword());
        try {
            mysqlDataSource.setServerTimezone("Asia/Shanghai");
            mysqlDataSource.setCharacterEncoding("UTF-8");
            mysqlDataSource.setUseSSL(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.session = new Session(mysqlDataSource);
    }


    /**
     * SQL执行器 创建数据库 加入事务的操作
     *
     * @param script
     */
    public void execute(List<String> script) {
        try {
            String[] sqlScripts = new String[script.size()];
            for (int i = 0; i < script.size(); i++) {
                sqlScripts[i] = script.get(i).replaceAll("\n", " ");
            }
            log.info("批量执行建表SQL = " + script);
            session.beginTransaction();
            int[] rows = session.executeBatch(sqlScripts);
            session.commit();
            log.info("批量执行建表数 = " + rows.length);
        } catch (Exception e) {
            e.printStackTrace();
            session.quietRollback();
        } finally {
            session.close();
        }
    }

}
