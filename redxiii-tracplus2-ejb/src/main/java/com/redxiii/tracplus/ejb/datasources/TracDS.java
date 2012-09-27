package com.redxiii.tracplus.ejb.datasources;

import com.redxiii.tracplus.ejb.entity.Attachment;
import com.redxiii.tracplus.ejb.entity.Wiki;
import com.redxiii.tracplus.ejb.util.AppConfiguration;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Filgueiras
 * @since 04/09/2012
 * TODO: Migrate to JPA
 */
@Named
public class TracDS implements Datasource {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private DBAccess access;
	
	@PostConstruct
	public void init() {
		logger.info("Loading database connection");
		
		access = new PostgreSQLDB(
				AppConfiguration.getInstance().getString("trac.database.host"),
				AppConfiguration.getInstance().getString("trac.database.name"),
				AppConfiguration.getInstance().getString("trac.database.user"),
				AppConfiguration.getInstance().getString("trac.database.pass"));
//		access = new SQLiteDB("T:\\db\\trac.db");
	}
	
	/* (non-Javadoc)
	 * @see com.redxiii.tracplus.ejb.datasources.Datasource#getLastTicketId()
	 */
	@Override
	public Number getLastTicketId() {
		return access.executeScalarQuery("SELECT max(id) as ticket FROM ticket");
	}
	
	/* (non-Javadoc)
	 * @see com.redxiii.tracplus.ejb.datasources.Datasource#getFirstTicketId()
	 */
	@Override
	public Number getFirstTicketId() {
		return access.executeScalarQuery("SELECT min(id) as ticket FROM ticket");
	}
	
	/* (non-Javadoc)
	 * @see com.redxiii.tracplus.ejb.datasources.Datasource#getFirstAttachTime()
	 */
	@Override
	public Number getFirstAttachTime() {
		return access.executeScalarQuery("SELECT min(time) as time FROM attachment");
	}
	
	/* (non-Javadoc)
	 * @see com.redxiii.tracplus.ejb.datasources.Datasource#getLastAttachTime()
	 */
	@Override
	public Number getLastAttachTime() {
		return access.executeScalarQuery("SELECT max(time) as time FROM attachment");
	}
	
	/* (non-Javadoc)
	 * @see com.redxiii.tracplus.ejb.datasources.Datasource#getTicketInfo(int, int)
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<TicketQueryResult> getTicketInfo(int min, int max) {
		return (List) access.executeListBeanQuery(
				"SELECT ticket.*, ticket_change.newvalue, ticket_change.time as modified FROM ticket left join ticket_change " +
						"on (ticket.id = ticket_change.ticket and ticket_change.field = 'comment') " +
						"where ticket.id >= ? and ticket.id < ? order by ticket.id, ticket.time, ticket_change.time", 
						TicketQueryResult.class, min, max);
	}
	
	/* (non-Javadoc)
	 * @see com.redxiii.tracplus.ejb.datasources.Datasource#getTicketInfo(java.lang.Integer)
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<TicketQueryResult> getTicketInfo(Integer id) {
		return (List) access.executeListBeanQuery(
				"SELECT ticket.*, ticket_change.newvalue, ticket_change.time as modified FROM ticket left join ticket_change " +
						"on (ticket.id = ticket_change.ticket and ticket_change.field = 'comment') " +
						"where ticket.id = ? order by ticket.id, ticket.time, ticket_change.time", 
						TicketQueryResult.class, id);
	}
	
	/* (non-Javadoc)
	 * @see com.redxiii.tracplus.ejb.datasources.Datasource#getChangeTicketsIds(long)
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Integer> getChangeTicketsIds(long changetime) {
            long datetime = changetime / 1000L;
            logger.debug("Loading changed tickets after: {} = {}", new Date(changetime), datetime);
            return (List) access.executeListScalarQuery(
            "SELECT ticket.id from ticket where changetime >= ? order by ticket.changetime", datetime);
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.redxiii.tracplus.ejb.datasources.Datasource#getLastWikiUpdate()
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<RecentWiki> getLastWikiUpdate() {
		return (List) access.executeListBeanQuery("SELECT name, max(version) as version FROM wiki GROUP BY name", RecentWiki.class);
	}
	
	/* (non-Javadoc)
	 * @see com.redxiii.tracplus.ejb.datasources.Datasource#getWiki(java.lang.String, java.lang.Number)
	 */
	@Override
	public Wiki getWiki(String name, Number version) {
		List<Object> objects = access
				.executeListBeanQuery("SELECT * FROM wiki WHERE name = ? and version = ?", 
				Wiki.class, name, version);
		
		if (objects != null && objects.size() > 0)
			return (Wiki) objects.get(0);
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.redxiii.tracplus.ejb.datasources.Datasource#getTicketAttachments(long, long)
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Attachment> getTicketAttachments(long start, long end) {
		return (List) access.executeListBeanQuery("SELECT * FROM attachment WHERE time BETWEEN ? AND ?",
				Attachment.class, start, end);
	}
}


/**
 * @author Daniel Filgueiras
 * @since 19/06/2011
 */
abstract class DBAccess {

	protected DataSource ds;
	private MapListHandler mlh = new MapListHandler();
	private QueryRunner qr;
	
	private ScalarHandler scalarHandler = new ScalarHandler();
	protected final String serverName;
	protected final String userName;
	protected final String password;
	protected final String databaseName;

	private static final Logger logger = LoggerFactory.getLogger(DBAccess.class);

	public DBAccess(String serverName, String databaseName, String userName,
			String password) {
		this.databaseName = databaseName;
		this.password = password;
		this.serverName = serverName;
		this.userName = userName;
	}

	protected abstract void makeDS();
	public abstract boolean testConnection();

	private void makeQueryRunner() {
		if (qr == null) {
			makeDS();
			qr = new QueryRunner();
		}
	}

	@SuppressWarnings("deprecation")
	public final List<Map<String, Object>> executeListMapQuery(String query,
			Object... params) {
		this.makeQueryRunner();

		Connection c = null;		
		try {
			c = ds.getConnection();
			return (List<Map<String, Object>>) qr.query(c, query, params, mlh);
			
		} catch (SQLException e) {
			logger.error("Executando query", e);
			return null;
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException sqle) {
					
				}
			}
		}
	}
	
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public final <T> List<T> executeListScalarQuery(String query,
			Object... params) {
		this.makeQueryRunner();
		
		Connection c = null;		
		try {
			c = ds.getConnection();
			return (List<T>) qr.query(c, query, params,
					new ColumnListHandler());
			
		} catch (SQLException e) {
			logger.error("Executando query", e);
			return Collections.emptyList();
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException sqle) {
					
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public final <T> T executeScalarQuery(String query,
			Object... params) {
		this.makeQueryRunner();

		Connection c = null;		
		try {
			c = ds.getConnection();
			return (T) qr.query(c, query, params, scalarHandler);
			
		} catch (SQLException e) {
			logger.error("Executando query", e);
			return null;
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException sqle) {
					
				}
			}
		}
	}
	
	public final Map<String, Object> executeMapQuery(String query,
			Object... params) {
		List<Map<String, Object>> lista = executeListMapQuery(query, params);
		
		if (lista.size() > 0)
			return lista.get(0);
		
		return Collections.emptyMap();
	}

	public final List<Map<String, Object>> executeListMapQuery(String query) {
		return this.executeListMapQuery(query, (Object[]) null);
	}
		
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final List<Object> executeListBeanQuery(String ddl, Class clazz, Object... objs) {
		
		this.makeQueryRunner();
		
		Connection c = null;		
		try {
			c = ds.getConnection();
			return (List<Object>) qr.query(c, ddl, new BeanListHandler(clazz), objs);
		} catch (SQLException e) {
			logger.error("Executando query", e);
			return Collections.emptyList();
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException sqle) {
					
				}
			}
		}
	}
	
	public final boolean executeUpdate(String ddl, Object... params) throws SQLException {
		makeQueryRunner();
		
		Connection c = null;
		try {
			c = ds.getConnection();
			qr.update(c,ddl, params);
			return true;
			
		} catch (SQLException e) {
			logger.error("Executando query", e);
			SQLException sqle = e;
			while (sqle.getNextException() != null) {
				sqle = sqle.getNextException();
			}
			
			if (e.getErrorCode() == 229) {
				throw new SQLException(sqle);
			}
			return false;
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException sqle) {
				}
			}
			;
		}
	}
	
	public final boolean executeUpdate(String ddl, Object[][] params) {
		makeQueryRunner();
		
		Connection c = null;
		try {
			c = ds.getConnection();
			qr.batch(c,ddl, params);
			return true;
		} catch (Exception e) {
			logger.error("Executando query", e);
			return false;
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException sqle) {
				}
			}
			;
		}
	}
}

/**
 * @author Daniel Filgueiras
 * @since 20/06/2011
 */
class PostgreSQLDB extends DBAccess {

	private static final Logger logger = LoggerFactory.getLogger(PostgreSQLDB.class);
	
	/**
	 * Padrão Login/Passwd == databaseName
	 * @param serverName
	 * @param databaseName
	 */
	public PostgreSQLDB(String serverName, String databaseName) {
		this(serverName, databaseName, databaseName, databaseName);
	}
	
	public PostgreSQLDB() {
		this("localhost", "postgres", "postgres", "");
	}
	
	public PostgreSQLDB(String serverName, String databaseName, String userName,
			String password) {
		super(serverName, databaseName, userName, password);
		
	}
	
	private static String getDataSourceName(String serverName, String databaseName, String userName,
			String password) {
		return "postgesql:" + serverName + databaseName + userName + password;
	}
	
	protected void makeDS() {
		if (ds == null) {
			String dsName = getDataSourceName(serverName, databaseName, userName, password);
			ds = org.postgresql.ds.PGPoolingDataSource.getDataSource(dsName);
			if (ds == null) {
				org.postgresql.ds.PGPoolingDataSource source = new org.postgresql.ds.PGPoolingDataSource();
				source.setServerName(serverName);
				source.setDatabaseName(databaseName);
				source.setUser(userName);
				source.setPassword(password);
				source.setDataSourceName(dsName);
				ds = source;
			}
		}
	}
	
	@Override
	public boolean testConnection() {
		Object obj = super.executeScalarQuery("select CURRENT_DATE");
		logger.info("Connection test: {}", obj);
		return obj != null;
	}
}
