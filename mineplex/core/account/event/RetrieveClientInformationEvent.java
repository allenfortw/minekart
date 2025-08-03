package mineplex.core.account.event;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RetrieveClientInformationEvent extends Event
{
  private static final HandlerList handlers = new HandlerList();
  
  private static Connection _connection;
  private String _connectionString = "jdbc:mysql://db.mineplex.com:3306/Stats?autoReconnect=true&failOverReadOnly=false&maxReconnects=10";
  private String _userName = "root";
  private String _password = "tAbechAk3wR7tuTh";
  
  private String _clientName;
  
  public RetrieveClientInformationEvent(String playerName)
  {
    this._clientName = playerName;
    
    try
    {
      if ((_connection == null) || (_connection.isClosed())) {
        _connection = DriverManager.getConnection(this._connectionString, this._userName, this._password);
      }
    }
    catch (Exception exception) {
      System.out.println("Erorr in REtrieveClientINformationEvent constructor");
    }
  }
  
  public Connection getConnection()
  {
    return _connection;
  }
  
  public HandlerList getHandlers()
  {
    return handlers;
  }
  
  public static HandlerList getHandlerList()
  {
    return handlers;
  }
  
  public String getClientName()
  {
    return this._clientName;
  }
}
