package com.gzd.mappercreator.mapperCreator;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class Creat {
//	private static String packageName  ="com.zjrc.ssc.manager.bean.entity";
	private static String packageName  ="com.insigma.facade.po";
	private static String jdbcurl  ="10.85.94.191";
	private static String schemaName  = "sysbase";
	private static String user = "sysbase";
	private static String pass = "Epsoft2019";
	private static Map<String,Object> tableMap=null ;
	//需要导出的表名放这里，全部导出留空保证tableMap 为null
	static {
//		tableMap = new HashMap<String, Object>();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DatabaseMetaData dbMetaData = null;
		try {
			Properties props = new Properties();
			props.put("remarksReporting","true");//orcale获取字段注释需要添加这个条件。mysql不需要
			props.put("user",user);
			props.put("password",pass);
			props.setProperty("useInformationSchema", "true");//设置可以获取tables remarks信息
			//orcale的驱动
//			Class.forName("oracle.jdbc.driver.OracleDriver");
			//mysql的驱动
			Class.forName("com.mysql.cj.jdbc.Driver");
			String url = "jdbc:mysql://"+jdbcurl+":3306/"+schemaName+
					"?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf-8";
			Connection con = DriverManager.getConnection(url, props);
			dbMetaData = con.getMetaData();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String[] types = { "TABLE" };
		ResultSet tableRs;

		try {
			tableRs = dbMetaData.getTables(null, schemaName, "%", types);
			List<String> tableNameList = new ArrayList<String>();
			//获取所有表的字段和注释
			while (tableRs.next()) {
				String tableName=tableRs.getString("TABLE_NAME");
				String tableRemark=tableRs.getString("REMARKS");
				if(tableMap!=null&&tableMap.get(tableName)==null){
					continue;
				}
				List<FieldInfo> fieldInfoList = new ArrayList<FieldInfo>();
				ResultSet fieldRs = dbMetaData.getColumns(null, "%",tableName,"%");
				while (fieldRs.next()) {
					FieldInfo fieldInfo = new FieldInfo();
					String fieldSqlName = fieldRs.getString("COLUMN_NAME");//列名
					fieldInfo.setName(Util.getFieldName(fieldSqlName));
					fieldInfo.setSqlName(fieldSqlName.toLowerCase());
					fieldInfo.setType(Util.getBeanType(fieldRs.getString("TYPE_NAME"),fieldRs.getInt("DECIMAL_DIGITS")));
					//mysql获取字段注释使用
					if(fieldRs.getString("REMARKS")!=null){
						fieldInfo.setRemark(fieldRs.getString("REMARKS"));
					}
					//用于判断是有数据类型遗漏没转换
					if(fieldInfo.getType()==null){
						System.out.println("-----------------------");
						System.out.println(tableName);
						System.out.println(fieldRs.getString("TYPE_NAME"));
						System.out.println(fieldInfo);
					}
					fieldInfoList.add(fieldInfo);
				}
				String className = Util.getClassName(tableName);
				//生成表名和字段数量
				System.out.println(className);
				System.out.println(fieldInfoList.size());
				//生成实体文件
				Util.creatDao(packageName,className);
				Util.creatEntity(packageName,className, fieldInfoList);
				Util.createServieceImpl(packageName,className);
				//1：orcale模板，2：mysql模板
				Util.creatMapper(packageName,className,tableName, fieldInfoList,2);
				Util.creatController(packageName,className,tableRemark);
//				break;
			}
			System.out.println("======Creat is over=======");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
