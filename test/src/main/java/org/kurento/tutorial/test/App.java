package org.kurento.tutorial.test;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.kurento.repository.RepositoryClient;
import org.kurento.repository.RepositoryClientProvider;
import org.kurento.repository.service.pojo.RepositoryItemPlayer;
import org.kurento.repository.service.pojo.RepositoryItemRecorder;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
/**
 * Hello world!
 *
 */
public class App 
{
	//protected static final String DEFAULT_REPOSITORY_SERVER_URI = "http://localhost:7674";
	protected static final String DEFAULT_REPOSITORY_SERVER_URI = "file:///tmp/";
	protected static final String REPOSITORY_SERVER_URI = System.getProperty("repository.uri",
	        DEFAULT_REPOSITORY_SERVER_URI);
	private static final String RECORDING_EXT = ".webm";
	private final static String MONGO_HOST_IP="127.0.0.1";
	private final static String MONGO_HOST_PORT="27017";
	private final static String MONGO_DB_NAME="testdb";
	private final static String MONGO_DB_COLLECTION="user";
	private final static String DEFAULT_BEFORE_TIME_SECOND="10";
	protected static final String BEFORE_TIME_SECOND = System.getProperty("repository.uri",
			DEFAULT_BEFORE_TIME_SECOND);
	public static Boolean deleteFile(String sPath) {  
		Boolean flag = false;  
		File file = new File(sPath);  
	    // 路径为文件且不为空则进行删除  
	    if (file.isFile() && file.exists()) {  
	    	try{
	    		System.out.println("exists");
	    		flag =file.delete();
	    		if(flag)
	    			System.out.println("delete: "+sPath);
	    		else
	    			System.out.println("failed delete: "+sPath);
	    		//String cmd = "echo  1 |sudo  rm  "+ sPath+ " ";
	    		//Runtime.getRuntime().exec(cmd);
	    	}catch(SecurityException  e){
	    		System.out.println(e.getMessage());
	    	} /*catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	    }  
	    return flag;  
	}  
	public static void delete(int second){
		try{
			//RepositoryClient repositoryClient=RepositoryClientProvider.create(REPOSITORY_SERVER_URI);
			RepositoryClient repositoryClient=REPOSITORY_SERVER_URI .startsWith("file://") ? null
		            : RepositoryClientProvider.create(REPOSITORY_SERVER_URI );
		MongoClient mongo = new MongoClient(System.getProperty("mongo.ip",
				MONGO_HOST_IP), Integer.valueOf(System.getProperty("mongo.port",
				MONGO_HOST_PORT)));

		MongoDatabase db = mongo.getDatabase(System.getProperty("mongo.dbname",MONGO_DB_NAME));
		MongoCollection<Document> table = db.getCollection(System.getProperty("mongo.dbCollection",MONGO_DB_COLLECTION));
		FindIterable<Document> findIterable = table.find();
		for(Document doc:findIterable){
			String str=doc.getString("endTime");
			if("0".equals(str))
				continue;
			Date time = doc.getDate("endTime");
			long diff = (new Date()).getTime() - time.getTime();
			if(diff>1000 * second){
				String id=doc.getString("id");
				Boolean delFlag=true;
				if(null==repositoryClient){
					String filePath = REPOSITORY_SERVER_URI  + id + RECORDING_EXT;
					if(!deleteFile(filePath)){
						System.out.println( "delete fail:"+filePath);
						delFlag=false;
					}
				}else{
					repositoryClient.removeRepositoryItem(id);
				}
				if(delFlag)
					table.deleteOne(doc);
			}
		}
		mongo.close();
	    } catch (MongoException e) {
	    	e.printStackTrace();
	    }

	  }
    private static void sleepSECONDS(long minutes) {
        try {
            TimeUnit.SECONDS.sleep(minutes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static void delete(){
    	int second=Integer.valueOf(BEFORE_TIME_SECOND);
    	while(true){
    		int minutes=1;
    		System.out.println("begin delete");
    		sleepSECONDS(second);
    		delete(second);
    	}
    }
    public static void main( String[] args )
    {
    	delete();
    	//delete(60*10);//删除１０分钟以前的数据
    	//System.out.println(deleteFile("/tmp/2017-02-25_13-11-10-63.webm"));
    /*	RepositoryClient repositoryClient=RepositoryClientProvider.create(REPOSITORY_SERVER_URI);
   	RepositoryItemPlayer repositoryItemPlayer=repositoryClient.getReadEndpoint("1");
    	//repositoryClient.removeRepositoryItem("2");
    System.out.println(repositoryItemPlayer.getUrl());
    System.out.println( "Hello World!" );*/
    }
}
