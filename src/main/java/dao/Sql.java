package dao;
import java.sql.*;import java.util.*;import utils.DBUtils;
public final class Sql {
 public static List<Map<String,Object>> rows(Connection c,String sql,Object... args)throws Exception{
  try(PreparedStatement s=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)s.setObject(i+1,args[i]);try(ResultSet r=s.executeQuery()){List<Map<String,Object>> out=new ArrayList<>();while(r.next()){Map<String,Object> row=new LinkedHashMap<>();for(int i=1;i<=r.getMetaData().getColumnCount();i++)row.put(r.getMetaData().getColumnLabel(i),r.getObject(i));out.add(row);}return out;}}
 }
 public static List<Map<String,Object>> rows(String sql,Object...args)throws Exception{try(Connection c=DBUtils.getConnection()){return rows(c,sql,args);}}
 public static int update(Connection c,String sql,Object...args)throws Exception{try(PreparedStatement s=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)s.setObject(i+1,args[i]);return s.executeUpdate();}}
 public static int update(String sql,Object...args)throws Exception{try(Connection c=DBUtils.getConnection()){return update(c,sql,args);}}
 public static Map<String,Object> one(Connection c,String sql,Object...args)throws Exception{List<Map<String,Object>> r=rows(c,sql,args);return r.isEmpty()?null:r.get(0);}
}
