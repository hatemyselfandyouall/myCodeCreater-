package com.gzd.mapperComparetor;

import com.ucmed.common.waybill.model.SampleInfoModel;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.html.HTMLConversionImageHandler;
import org.docx4j.convert.out.html.HtmlExporterNonXSLT;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.ui.ModelMap;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 比较本地库和生产库的区别,避免愚蠢
 */
public class PDFCompareUtil {

	private static String LOCAL_URL="jdbc:mysql://115.159.113.37:3306/jypt?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true";
	private static String LOCAL_USERNAME="ucmed";
	private static String LOCAL_PASSWORD="TiAw0@dae6r4Vnb";
	private static String WORK_URL="jdbc:mysql://123.206.217.254:3306/jypt?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true";
	private static String WORK_USERNAME="jypt";
	private static String WORK_PASSWORD="V#OPFOIlsDv2!pV";

	public static void compare() throws IOException {

		try{
			DatabaseMetaData localMate=DriverManager.getConnection(LOCAL_URL, LOCAL_USERNAME, LOCAL_PASSWORD).getMetaData();
			DatabaseMetaData worklMate=DriverManager.getConnection(WORK_URL, WORK_USERNAME, WORK_PASSWORD).getMetaData();
			ResultSet locolTables = localMate.getTables(null, "%", "%", new String[]{"TABLE"});
			ResultSet workTables = worklMate.getTables(null, "%", "%", new String[]{"TABLE"});
			while(locolTables.next()){
				List<ComlunModel>  localComlunModelList=new ArrayList<>();
				List<ComlunModel>  workComlunModelList=new ArrayList<>();
//				System.out.println("开始对比表:"+locolTables.getString("TABLE_NAME"));
				ResultSet colRet = localMate.getColumns(null,"%", locolTables.getString("TABLE_NAME"),"%");
				ResultSet workRet = worklMate.getColumns(null, "%", locolTables.getString("TABLE_NAME"), "%");
				while(colRet.next()) {
					ComlunModel localCom=new ComlunModel();
					if(workRet.next()) {
						ComlunModel workCom=new ComlunModel();
						localCom.setName(colRet.getString("COLUMN_NAME"));
						localCom.setType(colRet.getString("TYPE_NAME"));
						localCom.setSize(colRet.getString("COLUMN_SIZE"));
						localCom.setAbleNull(colRet.getString("NULLABLE"));
						workCom.setName(workRet.getString("COLUMN_NAME"));
						workCom.setType(workRet.getString("TYPE_NAME"));
						workCom.setSize(workRet.getString("COLUMN_SIZE"));
						workCom.setAbleNull(workRet.getString("NULLABLE"));
						localComlunModelList.add(localCom);
						workComlunModelList.add(workCom);

//						System.out.println(colRet.getString("COLUMN_NAME") + " " + colRet.getString("TYPE_NAME") + " " + colRet.getString("COLUMN_SIZE") + " " + colRet.getString("DECIMAL_DIGITS") + " " +
//								colRet.getString("NULLABLE")+ " "+workRet.getString("COLUMN_NAME") + " " + workRet.getString("TYPE_NAME") + " " + workRet.getString("COLUMN_SIZE") + " " + workRet.getString("DECIMAL_DIGITS") + " " +
//								workRet.getString("NULLABLE"));
					}else{
						localCom.setName(colRet.getString("COLUMN_NAME"));
						localCom.setType(colRet.getString("TYPE_NAME"));
						localCom.setSize(colRet.getString("COLUMN_SIZE"));
						localCom.setAbleNull(colRet.getString("NULLABLE"));
						localComlunModelList.add(localCom);
						System.out.println("表行数不对!:表名为:"+locolTables.getString("TABLE_NAME"));
					}
				}
					Comparator<ComlunModel> comparator = new Comparator<ComlunModel>() {
						@Override
						public int compare(ComlunModel o1, ComlunModel o2) {
							String name1 =o1.getName();
							String name2 =o2.getName();
							//按照检验条码正序来排列
							return name1.compareTo(name2);
						}
					};
					Collections.sort(localComlunModelList, comparator);
					Collections.sort(workComlunModelList, comparator);
				if(workComlunModelList.size()< localComlunModelList.size()){
					for(int i=0;i<localComlunModelList.size();i++){
						if(!workComlunModelList.contains(localComlunModelList.get(i))){
							System.out.println("缺失字段:位于表:"+locolTables.getString("TABLE_NAME")+",字段"+localComlunModelList.get(i).getName());
						}
					}
				}else{
					for(int i=0;i<localComlunModelList.size();i++){
						if(!localComlunModelList.get(i).getName().equals(localComlunModelList.get(i).getName())) {
							System.out.println("异常1:位于表:"+locolTables.getString("TABLE_NAME")+",字段"+localComlunModelList.get(i).getName());
							continue ;
						}
						if(!localComlunModelList.get(i).getType().equals(localComlunModelList.get(i).getType())){

							System.out.println("异常2:位于表:"+locolTables.getString("TABLE_NAME")+",字段"+localComlunModelList.get(i).getName());
							continue;
						}
						if(!localComlunModelList.get(i).getSize().equals(localComlunModelList.get(i).getSize())){

							System.out.println("异常3:位于表:"+locolTables.getString("TABLE_NAME")+",字段"+localComlunModelList.get(i).getName());
							continue;
						}
						if(!localComlunModelList.get(i).getAbleNull().equals(localComlunModelList.get(i).getAbleNull())){

							System.out.println("异常5:位于表:"+locolTables.getString("TABLE_NAME")+",字段"+localComlunModelList.get(i).getName());
							continue;
						}
					}
				}

			}

//			Statement stmt = con.createStatement() ;
//			ResultSet result=stmt.executeQuery(sqlword);
	//		System.out.println(result.next());
		}catch(SQLException se){
			System.out.println("数据库连接失败！");
			se.printStackTrace() ;
		}
	}
	/**
	 * 获取元数据
	 */
	public static DatabaseMetaData  getMetaDate(String url,String userName,String passWord){
		try {
			return DriverManager.getConnection(url,userName,passWord).getMetaData() ;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void main(String []args) throws IOException {
		compare();
	}

}