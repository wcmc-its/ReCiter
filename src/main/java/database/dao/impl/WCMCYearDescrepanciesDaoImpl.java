package database.dao.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import database.DbConnectionFactory;
import database.DbUtil;
import database.dao.WCMCYearDescrepanciesDao;

/**
 * 
 * @author htadimeti
 *
 */
public class WCMCYearDescrepanciesDaoImpl implements WCMCYearDescrepanciesDao{
	public Map<Integer,Double> getWCMCYearDescrepancies(){
		Map<Integer,Double> yrDescrList = new HashMap<Integer,Double>();
		Connection con = DbConnectionFactory.getConnection();
		Statement pst = null;
		ResultSet rs = null;
		try{
			String query = "select year,score from wcmc_year_discrepancies";
			pst=con.createStatement();
			rs=pst.executeQuery(query);
			SimpleDateFormat df = new SimpleDateFormat("yyyy");
			while(rs.next()){
				Date date = rs.getDate(1);
				BigDecimal score = rs.getBigDecimal(2);
				int year=date.getYear();
				try{
					year = Integer.parseInt(df.format(date));
				}catch(Exception ex){
					
				}
				yrDescrList.put(year,score.doubleValue());
				//new Date(rs.getDate(1).getTime())
			}
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return yrDescrList;
	}
}
